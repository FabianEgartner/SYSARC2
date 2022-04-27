package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherCommand> {

    // interface
    public interface WeatherCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class ReadWeather implements WeatherCommand {
        final WeatherCondition weatherCondition;

        public ReadWeather(WeatherCondition weatherCondition) { this.weatherCondition = weatherCondition; }
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<WeatherSensor.WeatherCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new WeatherSensor(context, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;

    public WeatherSensor(ActorContext<WeatherCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("WeatherSensor started");
    }

    // behavior of WeatherSensor class -> determines which method gets called after tell has been called from outside
    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeatherSensor.ReadWeather.class, this::onReadWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
    private Behavior<WeatherCommand> onReadWeather(ReadWeather readWeather) {
        getContext().getLog().info("WeatherSensor received {}", readWeather.weatherCondition);
        return this;
    }

    private WeatherSensor onPostStop() {
        getContext().getLog().info("WeatherSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
