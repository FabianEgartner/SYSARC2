package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.WeightCommand> {

    // interface
    public interface WeightCommand {}

    public static final class ReadWeight implements FridgeWeightSensor.WeightCommand {
        final int newProductWeight;
        final int actFridgeWeight;

        public ReadWeight(int newProductWeight, int actFridgeWeight) {
            this.newProductWeight = newProductWeight;
            this.actFridgeWeight = actFridgeWeight;
        }
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeWeightSensor.WeightCommand> create(ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new FridgeWeightSensor(context, fridge));
    }

    // class attributes
    private final ActorRef<Fridge.FridgeCommand> fridge;
    private final int maxWeight;

    // constructor
    public FridgeWeightSensor(ActorContext<FridgeWeightSensor.WeightCommand> context, ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);
        this.fridge = fridge;
        this.maxWeight = 100;

        getContext().getLog().info("FridgeWeightSensor started");
    }

    @Override
    public Receive<FridgeWeightSensor.WeightCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeight.class, this::onReadWeight)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<WeightCommand> onReadWeight(ReadWeight readWeight) {
        if ((readWeight.actFridgeWeight + readWeight.newProductWeight) > this.maxWeight) {

        }

        else {

        }

        return this;
    }

    private FridgeWeightSensor onPostStop() {
        getContext().getLog().info("FridgeWeightSensor actor stopped");
        return this;
    }
}
