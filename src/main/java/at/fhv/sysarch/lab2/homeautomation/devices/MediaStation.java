package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.BlindsState;

public class MediaStation extends AbstractBehavior<MediaStation.MediaStationCommand> {

    // interface
    public interface MediaStationCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class WatchMovie implements MediaStation.MediaStationCommand {
        final boolean movieRunning;

        public WatchMovie(boolean movieRunning) {this.movieRunning = movieRunning; }
    }

    public static final class LogStatus implements MediaStation.MediaStationCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<MediaStationCommand> create(ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        return Behaviors.setup(context -> new MediaStation(context, blinds, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private final ActorRef<Blinds.BlindsCommand> blinds;
    private boolean movieRunning = false;

    // constructor
    public MediaStation(ActorContext<MediaStationCommand> context, ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.blinds = blinds;

        getContext().getLog().info("MediaStation started");
    }

    @Override
    public Receive<MediaStationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MediaStation.WatchMovie.class, this::onWatchMovie)
                .onMessage(MediaStation.LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<MediaStation.MediaStationCommand> onWatchMovie (WatchMovie watchMovie) {
        boolean startMovie = watchMovie.movieRunning;

        getContext().getLog().info("MediaStation received {}", startMovie);

        if (startMovie && !this.movieRunning) {
            getContext().getLog().info("Movie is running");
            this.movieRunning = true;
            this.blinds.tell(new Blinds.StartMovie());

        }
        else if (!startMovie) {
            getContext().getLog().info("Movie is not running");
            this.movieRunning = false;
            this.blinds.tell(new Blinds.StopMovie());
        }

        return this;
    }

    private Behavior<MediaStation.MediaStationCommand> onLogStatus(MediaStation.LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("movieRunning: " + this.movieRunning);

        return Behaviors.same();
    }

    private MediaStation onPostStop() {
        getContext().getLog().info("MediaStation actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}