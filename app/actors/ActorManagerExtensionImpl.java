package actors;


import akka.actor.*;
import backend.StockManagerActor;
import backend.StockSentimentActor;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockSentimentActor;
    //private final ActorRef stockManagerProxy;
    private final ActorRef stockManagerActor;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {
        stockSentimentActor= system.actorOf(StockSentimentActor.props(), "stockSentimentProxy");
        //stockManagerProxy = system.actorOf(StockManagerProxy.props(), "stockManagerProxy");

        stockManagerActor =
                system.actorOf(StockManagerActor.props(), "stockManager");

    }

    public ActorRef getStockSentimentProxy() {
        return stockSentimentActor;
    }


    public ActorRef getStockManager() {
        return stockManagerActor;
    }
}
