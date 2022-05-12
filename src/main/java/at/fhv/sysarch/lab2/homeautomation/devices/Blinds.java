package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.BlindsState;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    // interface
    public interface BlindsCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class StartMovie implements Blinds.BlindsCommand {
        public StartMovie() {}
    }

    public static final class MoveBlindsWeatherSensor implements Blinds.BlindsCommand {
        final BlindsState blindsState;

        public MoveBlindsWeatherSensor(BlindsState blindsState) {
            this.blindsState = blindsState;
        }
    }

    public static final class StopMovie implements Blinds.BlindsCommand {

        public StopMovie() {}
    }

    public static final class LogStatus implements Blinds.BlindsCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private boolean blindsAreUp = true;
    private boolean movieRunning = false;
    private boolean isSunny = false;

    // constructor
    public Blinds(ActorContext<BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("Blinds started");
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Blinds.MoveBlindsWeatherSensor.class, this::onMoveBlindsWeatherSensor)
                .onMessage(Blinds.StartMovie.class, this::onStartMovie)
                .onMessage(Blinds.StopMovie.class, this::onStopMovie)
                .onMessage(Blinds.LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<Blinds.BlindsCommand> onMoveBlindsWeatherSensor (MoveBlindsWeatherSensor moveBlindsWeatherSensor) {
        BlindsState blindsState = moveBlindsWeatherSensor.blindsState;

        getContext().getLog().info("Blinds received {}", blindsState);

        if (blindsState.equals(BlindsState.OPEN) && !this.movieRunning) {
            getContext().getLog().info("Blinds are up");
            this.blindsAreUp = true;
            this.isSunny = false;

        }
        else if (blindsState.equals(BlindsState.CLOSED)) {
            getContext().getLog().info("Blinds are down");
            this.blindsAreUp = false;
            this.isSunny = true;
        }

        return this;
    }

    private Behavior<Blinds.BlindsCommand> onStartMovie (StartMovie moveBlindsMediaStation) {
        getContext().getLog().info("Blinds are down");
        this.blindsAreUp = false;
        this.movieRunning = true;

        return this;
    }

    private Behavior<Blinds.BlindsCommand> onStopMovie (StopMovie stopMovie) {
        getContext().getLog().info("Blinds received stop movie");
        this.movieRunning = false;
        return this;
    }

    private Behavior<Blinds.BlindsCommand> onLogStatus(Blinds.LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("blindsAreUp: " + this.blindsAreUp);
        getContext().getLog().info("movieRunning: " + this.movieRunning);
        getContext().getLog().info("isSunny: " + this.isSunny);

        return Behaviors.same();
    }

    private Blinds onPostStop() {
        getContext().getLog().info("Blinds actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
