package actors;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Optional;

public class StockManagerProxy extends AbstractActorWithStash {

    Cluster cluster = Cluster.get(getContext().system());

    private PartialFunction<Object, BoxedUnit> ready;

    private Optional<ActorSelection> stockManagerRef = Optional.empty();

    public static Props props() {
        return Props.create(StockManagerProxy.class, StockManagerProxy::new);
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    public StockManagerProxy() {

        ready = ReceiveBuilder
                .match(Stock.Watch.class, watch -> {
                    stockManagerRef.get().forward(watch, context());
                })
                .match(Stock.Unwatch.class, unwatch -> {
                    stockManagerRef.get().forward(unwatch, context());
                }).build();

        receive(ReceiveBuilder
                .match(ClusterEvent.MemberUp.class, memberUp -> {
                            Address address = memberUp.member().address();
                            if (memberUp.member().hasRole("backend")) {
                                String addStr = address.toString() + "/user/stockManager";
                                System.out.println("### addStr = " + addStr);
                                ActorSelection actorSelection = context().actorSelection(addStr);
                                stockManagerRef = Optional.of(actorSelection);

                                cluster.unsubscribe(self());

                                context().become(ready);
                                unstashAll();
                            }
                        }
                ).matchAny(msg -> stash())
                .build());
    }

}
