package at.fhv.sysarch.lab2.homeautomation.devices;

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
        public ReadSpace() {}

        // TODO:
    }

    public static final class LogStatus implements FridgeSpaceSensor.SpaceCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeSpaceSensor.SpaceCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeSpaceSensor(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    // private final ActorRef<Blinds.BlindsCommand> blinds;

    // constructor
    public FridgeSpaceSensor(ActorContext<FridgeSpaceSensor.SpaceCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("WeatherSensor started");
    }

    @Override
    public Receive<FridgeSpaceSensor.SpaceCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(FridgeSpaceSensor.LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<FridgeSpaceSensor.SpaceCommand> onLogStatus(FridgeSpaceSensor.LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);

        return Behaviors.same();
    }

    private FridgeSpaceSensor onPostStop() {
        getContext().getLog().info("FridgeSpaceSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
