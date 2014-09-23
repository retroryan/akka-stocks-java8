package actors;


import akka.actor.*;
import akka.routing.FromConfig;
import backend.journal.SharedJournalSetter;

public class ActorManagerExtensionImpl implements Extension {

    private final ActorRef stockSentimentProxy;
    //private final ActorRef stockManagerProxy;
    private final ActorRef stockManagerRouter;

    public ActorManagerExtensionImpl(ExtendedActorSystem system) {


        stockSentimentProxy = system.actorOf(StockSentimentProxy.props(), "stockSentimentProxy");
        //stockManagerProxy = system.actorOf(StockManagerProxy.props(), "stockManagerProxy");

        stockManagerRouter =
                system.actorOf(Props.empty().withRouter(FromConfig.getInstance()), "stockManagerRouter");

        system.actorOf(SharedJournalSetter.props(), "shared-journal-setter");

    }

    public ActorRef getStockSentimentProxy() {
        return stockSentimentProxy;
    }

    public ActorRef getStockManagerRouter() {
        return stockManagerRouter;
    }
}
