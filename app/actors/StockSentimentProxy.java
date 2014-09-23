package actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.japi.pf.ReceiveBuilder;
import backend.StockSentimentActor;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Optional;

public class StockSentimentProxy extends AbstractActorWithStash {

    Cluster cluster = Cluster.get(getContext().system());

    private PartialFunction<Object, BoxedUnit> ready;

    private Optional<ActorSelection> stockSentimentRef = Optional.empty();

    public static Props props() {
        return Props.create(StockSentimentProxy.class, StockSentimentProxy::new);
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    public StockSentimentProxy() {

        ready = ReceiveBuilder
                .match(StockSentimentActor.GetSentiment.class, getSentiment -> {
                    System.out.println("### StockSentimentProxy - forwarding get sentiment to: " + stockSentimentRef.get().toString());
                    stockSentimentRef.get().forward(getSentiment, context());
                }).build();

        receive(ReceiveBuilder
                .match(ClusterEvent.MemberUp.class, memberUp -> {
                            Address address = memberUp.member().address();
                            if (memberUp.member().hasRole("backend")) {
                                String addStr = address.toString() + "/user/stockSentiment";
                                System.out.println("### addStr = " + addStr);
                                ActorSelection actorSelection = context().actorSelection(addStr);
                                stockSentimentRef = Optional.of(actorSelection);

                                cluster.unsubscribe(self());

                                context().become(ready);
                                unstashAll();
                            }
                        }
                ).matchAny(msg -> stash())
                .build());
    }

}