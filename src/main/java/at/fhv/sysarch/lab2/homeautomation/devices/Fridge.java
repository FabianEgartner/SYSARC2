package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

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

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    // interface
    public interface FridgeCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class PowerFridge implements FridgeCommand {
        final boolean powerOn;

        public PowerFridge(boolean powerOn) {
            this.powerOn = powerOn;
        }
    }

    public static final class EnrichedTemperature implements FridgeCommand {
        final double temperature;
        final String unit;

        public EnrichedTemperature(double temperature, String unit) {
            this.temperature = temperature;
            this.unit = unit;
        }
    }

    public static final class LogStatus implements FridgeCommand {
        public LogStatus() {}
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private List<Product> products;
    private int weightCapacity;
    private int spaceCapacity;
    private boolean poweredOn = true;

    // constructor
    public Fridge(ActorContext<FridgeCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("Fridge started");
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Fridge(context, groupId, deviceId));
    }

    // behavior of Fridge class -> determines which method gets called after tell has been called from outside
    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PowerFridge.class, this::onPowerFridgeOff)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<FridgeCommand> onLogStatus(LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("products: " + this.products);
        getContext().getLog().info("weightCapacity: " + this.weightCapacity);
        getContext().getLog().info("spaceCapacity: " + this.spaceCapacity);
        getContext().getLog().info("poweredOn: " + this.poweredOn);

        return Behaviors.same();
    }

    
    // TODO: methods here
    

    private Behavior<FridgeCommand> onPowerFridgeOff(PowerFridge powerFridge) {
        boolean powerOn = powerFridge.powerOn;

        getContext().getLog().info("Turning Fridge to {}", powerOn);

        if(!powerOn) {
            return this.powerOff();
        }

        return this;
    }

    private Behavior<FridgeCommand> onPowerFridgeOn(PowerFridge powerFridge) {
        boolean powerOn = powerFridge.powerOn;

        getContext().getLog().info("Turning Fridge to {}", powerOn);

        if (powerOn) {
            return this.powerOn();
        }

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> powerOn() {
        this.poweredOn = true;

        // change behavior -> when turned on: reaction to temperature changes
        return Behaviors.receive(FridgeCommand.class)
                .onMessage(PowerFridge.class, this::onPowerFridgeOff)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeCommand> powerOff() {
        this.poweredOn = false;

        // change behavior -> when turned off: no reaction to temperature changes anymore
        return Behaviors.receive(FridgeCommand.class)
                .onMessage(PowerFridge.class, this::onPowerFridgeOn)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Fridge onPostStop() {
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
