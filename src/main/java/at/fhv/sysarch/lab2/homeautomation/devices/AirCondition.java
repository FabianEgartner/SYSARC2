package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * This class shows ONE way to switch behaviors in object-oriented style. Another approach is the use of static
 * methods for each behavior.
 *
 * The switching of behaviors is not strictly necessary for this example, but is rather used for demonstration
 * purpose only.
 *
 * For an example with functional-style please refer to: {@link https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style}
 *
 */
public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {

    // interface
    public interface AirConditionCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class PowerAirCondition implements AirConditionCommand {
        final boolean powerOn;

        public PowerAirCondition(boolean powerOn) {
            this.powerOn = powerOn;
        }
    }

    public static final class EnrichedTemperature implements AirConditionCommand {
        final double temperature;
        final String unit;

        public EnrichedTemperature(double temperature, String unit) {
            this.temperature = temperature;
            this.unit = unit;
        }
    }

    public static final class LogStatus implements AirConditionCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<AirConditionCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new AirCondition(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private boolean active = false;
    private boolean poweredOn = true;

    // constructor
    public AirCondition(ActorContext<AirConditionCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("AirCondition started");
    }

    // behavior of AirCondition class -> determines which method gets called after tell has been called from outside
    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(EnrichedTemperature.class, this::onReadTemperature)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<AirConditionCommand> onLogStatus(LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("active: " + this.active);
        getContext().getLog().info("poweredOn: " + this.poweredOn);

        return Behaviors.same();
    }

    private Behavior<AirConditionCommand> onReadTemperature(EnrichedTemperature enrichedTemperature) {
        double temperature = enrichedTemperature.temperature;
        String unit = enrichedTemperature.unit;

        getContext().getLog().info("AirCondition reading {}", temperature + " " + unit);

        if (temperature > 20) {
            getContext().getLog().info("AirCondition cools");
            this.active = true;

        } else {
            getContext().getLog().info("AirCondition does not cool");
            this.active = false;
        }

        return this;
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOff(PowerAirCondition powerAirCondition) {
        boolean powerOn = powerAirCondition.powerOn;

        getContext().getLog().info("Turning AirCondition to {}", powerOn);

        if(!powerOn) {
            return this.powerOff();
        }

        return this;
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOn(PowerAirCondition powerAirCondition) {
        boolean powerOn = powerAirCondition.powerOn;

        getContext().getLog().info("Turning AirCondition to {}", powerOn);

        if (powerOn) {
            return this.powerOn();
        }

        return Behaviors.same();
    }


    private Behavior<AirConditionCommand> powerOn() {
        this.poweredOn = true;
        this.active = false;

        // change behavior -> when turned on: reaction to temperature changes
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(EnrichedTemperature.class, this::onReadTemperature)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> powerOff() {
        this.poweredOn = false;
        this.active = false;

        // change behavior -> when turned off: no reaction to temperature changes anymore
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOn)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private AirCondition onPostStop() {
        getContext().getLog().info("AirCondition actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
