package actors;


import akka.actor.*;
import backend.StockManagerProxy;
import backend.StockSentimentActor;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockSentimentActor;
    private final ActorRef stockManagerProxy;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {


        stockSentimentActor = system.actorOf(StockSentimentActor.props(), "stockSentimentActor");
        stockManagerProxy = system.actorOf(StockManagerProxy.props(), "stockManagerProxy");
    }

    public ActorRef getStockSentimentActor() {
        return stockSentimentActor;
    }

    public ActorRef getStockManagerProxy() {
        return stockManagerProxy;
    }
}
