package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.SpaceCommand> {

    // interface
    public interface SpaceCommand {}

    public static final class ReadSpace implements FridgeSpaceSensor.SpaceCommand {
        final int newProductSpace;
        final int actFridgeSpace;

        public ReadSpace(int newProductSpace, int actFridgeSpace) {
            this.newProductSpace = newProductSpace;
            this.actFridgeSpace = actFridgeSpace;
        }
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeSpaceSensor.SpaceCommand> create(ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new FridgeSpaceSensor(context, fridge));
    }

    // class attributes
    private final ActorRef<Fridge.FridgeCommand> fridge;
    private final int maxSpace;

    // constructor
    public FridgeSpaceSensor(ActorContext<FridgeSpaceSensor.SpaceCommand> context, ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);
        this.fridge = fridge;
        this.maxSpace = 100;

        getContext().getLog().info("FridgeSpaceSensor started");
    }

    @Override
    public Receive<FridgeSpaceSensor.SpaceCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadSpace.class, this::onReadSpace)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<FridgeSpaceSensor.SpaceCommand> onReadSpace(FridgeSpaceSensor.ReadSpace readSpace) {
        if ((readSpace.actFridgeSpace + readSpace.newProductSpace) > this.maxSpace) {

        }
        else {

        }

        return this;
    }

    private FridgeSpaceSensor onPostStop() {
        getContext().getLog().info("FridgeSpaceSensor actor stopped");
        return this;
    }
}
