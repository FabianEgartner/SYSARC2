package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.products.Product;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.SpaceCommand> {

    // interface
    public interface SpaceCommand {}

    public static final class ReadSpace implements FridgeSpaceSensor.SpaceCommand {
        final Product productToAdd;
        final int occupiedSpace;

        public ReadSpace(Product productToAdd, int occupiedSpace) {
            this.productToAdd = productToAdd;
            this.occupiedSpace = occupiedSpace;
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
        this.maxSpace = 10;

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
    private Behavior<FridgeSpaceSensor.SpaceCommand> onReadSpace(ReadSpace readSpace) {
        Product productToAdd = readSpace.productToAdd;
        int spaceOfProductToAdd = productToAdd.getSpace();
        int alreadyOccupiedSpace = readSpace.occupiedSpace;

        if ((alreadyOccupiedSpace + spaceOfProductToAdd) <= this.maxSpace)
            this.fridge.tell(new Fridge.UpdateSpace(true, productToAdd));
        else
            this.fridge.tell(new Fridge.UpdateSpace(false, productToAdd));

        return this;
    }

    private FridgeSpaceSensor onPostStop() {
        getContext().getLog().info("FridgeSpaceSensor actor stopped");
        return this;
    }
}
