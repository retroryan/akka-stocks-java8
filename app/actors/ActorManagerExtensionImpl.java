package actors;


import akka.actor.*;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockSentimentProxy;
    private final ActorRef stockManagerProxy;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {


        stockSentimentProxy = system.actorOf(StockSentimentProxy.props(), "stockSentimentProxy");
        stockManagerProxy = system.actorOf(StockManagerProxy.props(), "stockManagerProxy");
    }

    public ActorRef getStockSentimentProxy() {
        return stockSentimentProxy;
    }

    public ActorRef getStockManagerProxy() {
        return stockManagerProxy;
    }
}
