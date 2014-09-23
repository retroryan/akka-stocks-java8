package actors;


import akka.actor.*;
import akka.routing.FromConfig;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockSentimentProxy;
    //private final ActorRef stockManagerProxy;
    private final ActorRef stockManagerRouter;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {


        stockSentimentProxy = system.actorOf(StockSentimentProxy.props(), "stockSentimentProxy");
        //stockManagerProxy = system.actorOf(StockManagerProxy.props(), "stockManagerProxy");

        stockManagerRouter =
                system.actorOf(Props.empty().withRouter(FromConfig.getInstance()), "stockManagerRouter");

    }

    public ActorRef getStockSentimentProxy() {
        return stockSentimentProxy;
    }

    public ActorRef getStockManagerRouter() {
        return stockManagerRouter;
    }
}
