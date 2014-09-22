package actors;


import akka.actor.ActorRef;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import backend.StockSentimentActor;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockManager;
    private final ActorRef stockSentimentActor;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {
        stockManager = system.actorOf(StockManagerActor.props(), "stockManagerClient");
        stockSentimentActor = system.actorOf(StockSentimentActor.props(), "stockSentimentActor");
    }

    public ActorRef getStockManager() {
        return stockManager;
    }

    public ActorRef getStockSentimentActor() {
        return stockSentimentActor;
    }
}
