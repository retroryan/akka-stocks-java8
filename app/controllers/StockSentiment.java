package controllers;

import actors.ActorManagerExtension;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import backend.StockSentimentActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static play.libs.F.Promise;

public class StockSentiment extends Controller {

    static Timeout timeout =
            new Timeout(Duration.create(5, "seconds"));

    public static Promise<Result> get(String symbol) {
        ActorRef stockSentimentActor = ActorManagerExtension.ActorManagerExtensionProvider.get(Akka.system()).getStockSentimentProxy();
        Future<Object> futureStockSentiments =
                Patterns.ask(stockSentimentActor,
                        new StockSentimentActor.GetSentiment(symbol), timeout);
        Promise<Object> promiseSentiments = Promise.wrap(futureStockSentiments);

        return promiseSentiments
                .<Result>map(obj -> {
                    StockSentimentActor.SendSentiment sendSentiment = (StockSentimentActor.SendSentiment) obj;
                    return Results.ok(sendSentiment.getSentiment());
                })
                .recover(StockSentiment::errorResponse);

    }

    public static Result errorResponse(Throwable ignored) {
        System.out.println("ignored = " + ignored);
        return internalServerError(Json.newObject().put("error", "Could not fetch the tweets"));
    }
}
