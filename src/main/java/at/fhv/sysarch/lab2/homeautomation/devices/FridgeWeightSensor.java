package at.fhv.sysarch.lab2.homeautomation.devices;

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
        public ReadWeight() {}

        // TODO:
    }

    public static final class LogStatus implements FridgeWeightSensor.WeightCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeWeightSensor.WeightCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeWeightSensor(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;

    // constructor
    public FridgeWeightSensor(ActorContext<FridgeWeightSensor.WeightCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("FridgeWeightSensor started");
    }

    @Override
    public Receive<FridgeWeightSensor.WeightCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(FridgeWeightSensor.LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<FridgeWeightSensor.WeightCommand> onLogStatus(FridgeWeightSensor.LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);

        return Behaviors.same();
    }

    private FridgeWeightSensor onPostStop() {
        getContext().getLog().info("FridgeWeightSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
