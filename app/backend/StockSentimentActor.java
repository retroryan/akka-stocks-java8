package backend;

import actors.Settings;
import actors.SettingsImpl;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import scala.concurrent.Future;
import utils.StockWS;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;
import static utils.Streams.stream;

public class StockSentimentActor extends AbstractLoggingActor {

    SettingsImpl settings = Settings.SettingsProvider.get(getContext().system());

    final HashMap<String, JsonNode> sentimentCache = new HashMap<>();

    public static final class GetSentiment implements Serializable {

        static final long serialVersionUID = 42L;

        public final String symbol;

        public GetSentiment(String symbol) {
            this.symbol = symbol;
        }
    }

    public static final class SendSentiment implements Serializable {

        static final long serialVersionUID = 42L;

        public transient JsonNode sentiment;

        public SendSentiment(JsonNode sentiment) {
            this.sentiment = sentiment;
        }

        public JsonNode getSentiment() {
            return sentiment;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            System.out.println("writing sentiment = " + sentiment);
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(sentiment);
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            int byteLen = in.readInt();
            byte[] bytes = new byte[byteLen];
            int read = in.read(bytes);
            ObjectMapper mapper = new ObjectMapper();
            sentiment = mapper.readTree(bytes);
            System.out.println("read sentiment = " + sentiment);
        }
    }

    public static Props props() {
        return Props.create(StockSentimentActor.class, StockSentimentActor::new);
    }

    public StockSentimentActor() {
        receive(ReceiveBuilder
                .match(GetSentiment.class, getSentiment -> {
                    String symbol = getSentiment.symbol;
                    System.out.println("### StockSentimentActor.StockSentimentActor - fetching sentiment for " + symbol);
                    F.Promise<SendSentiment> sendSentimentPromise;

                    if (sentimentCache.containsKey(symbol)) {
                        JsonNode jsonNode = sentimentCache.get(symbol);
                        sendSentimentPromise = F.Promise.pure(new SendSentiment(jsonNode));
                    } else {
                        F.Promise<ObjectNode> promiseStockSentiments = getStockSentiments(symbol);
                        sendSentimentPromise = promiseStockSentiments.map(node -> new SendSentiment(node));
                    }

                    Future<SendSentiment> wrapped = sendSentimentPromise.wrapped();
                    Patterns.pipe(wrapped, context().dispatcher()).to(sender());

                }).build());
    }


    public F.Promise<ObjectNode> getStockSentiments(String symbol) {
        return fetchTweets(symbol)
                .flatMap(tweets -> fetchSentiments(tweets))
                .map(sentiments -> averageSentiment(sentiments));
    }

    public F.Promise<List<String>> fetchTweets(String symbol) {
        F.Promise<WSResponse> promiseResponse = StockWS.url(settings.TWEET_URL)
                .setQueryParameter("q", "$" + symbol).get();

        return promiseResponse
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> stream(response.asJson().findPath("statuses"))
                        .map(s -> s.findValue("text").asText())
                        .collect(toList()));
    }

    public F.Promise<List<JsonNode>> fetchSentiments(List<String> tweets) {
        Stream<F.Promise<WSResponse>> sentiments = tweets.stream().map(text -> StockWS.url(settings.SENTIMENT_URL).post("text=" + text));
        return F.Promise
                .sequence(sentiments::iterator)
                .map(responses -> responsesAsJson(responses));
    }

    public List<JsonNode> responsesAsJson(List<WSResponse> responses) {
        return responses.stream().map(WSResponse::asJson).collect(toList());
    }

    public ObjectNode averageSentiment(List<JsonNode> sentiments) {
        double neg = collectAverage(sentiments, "neg");
        double neutral = collectAverage(sentiments, "neutral");
        double pos = collectAverage(sentiments, "pos");

        String label = (neutral > 0.5) ? "neutral" : (neg > pos) ? "neg" : "pos";

        ObjectNode objectNode = Json.newObject();

        objectNode.put("label", label)
                .set("probability", Json.newObject()
                        .put("neg", neg)
                        .put("neutral", neutral)
                        .put("pos", pos));
        return objectNode;
    }

    public double collectAverage(List<JsonNode> jsons, String label) {
        return jsons.stream().collect(averagingDouble(json -> json.findValue(label).asDouble()));
    }


}
