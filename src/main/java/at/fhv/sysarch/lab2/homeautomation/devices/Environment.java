package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.WeatherCondition;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    // interface
    public interface EnvironmentCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class ChangedTemperature implements EnvironmentCommand {

    }

    public static final class ChangedWeatherConditions implements EnvironmentCommand {

    }

    public static final class LogStatus implements Environment.EnvironmentCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<EnvironmentCommand> create(ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Environment(context, temperatureSensor, weatherSensor, timers, timers)));
    }

    // class attributes
    private double temperature = 15.0;
    private WeatherCondition weatherCondition = WeatherCondition.CLOUDY;
    private boolean setHighTemp = false;
    private boolean setLowTemp = true;

    private final ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor;
    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;

    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

    // constructor
    public Environment(ActorContext<EnvironmentCommand> context, ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor, TimerScheduler<EnvironmentCommand> temperatureTimeScheduler, TimerScheduler<EnvironmentCommand> weatherTimeScheduler) {
        super(context);

        this.temperatureSensor = temperatureSensor;
        this.weatherSensor = weatherSensor;

        this.temperatureTimeScheduler = temperatureTimeScheduler;
        this.weatherTimeScheduler = weatherTimeScheduler;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new ChangedTemperature(), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new ChangedWeatherConditions(), Duration.ofSeconds(30));
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ChangedTemperature.class, this::onChangeTemperature)
                .onMessage(ChangedWeatherConditions.class, this::onChangeWeatherConditions)
                .onMessage(LogStatus.class, this::onLogStatus)
                .build();
    }

    private Behavior<EnvironmentCommand> onLogStatus(LogStatus logStatus) {
        getContext().getLog().info("temperature: " + this.temperature);
        getContext().getLog().info("weatherCondition: " + this.weatherCondition);
        getContext().getLog().info("setHighTemp: " + this.setHighTemp);
        getContext().getLog().info("setLowTemp: " + this.setLowTemp);

        return Behaviors.same();
    }

    private Behavior<EnvironmentCommand> onChangeTemperature(ChangedTemperature changedTemperature) {

        if (setLowTemp && temperature < 25.0) {
            temperature = temperature + 1.0;
        }

        if (setHighTemp && temperature > 15.0) {
            temperature = temperature - 1.0;
        }

        if (temperature == 25.0) {
            setHighTemp = true;
            setLowTemp = false;
        }

        if (temperature == 15.0) {
            setHighTemp = false;
            setLowTemp = true;
        }

        getContext().getLog().info("Environment received {}", temperature);
        this.temperatureSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(temperature)));

        return this;
    }

    private Behavior<EnvironmentCommand> onChangeWeatherConditions(ChangedWeatherConditions changedWeatherConditions) {

        Random random = new Random();

        if (random.nextBoolean()) {
            weatherCondition = WeatherCondition.SUNNY;
        } else {
            weatherCondition = WeatherCondition.CLOUDY;
        }

        getContext().getLog().info("Environment received {}", weatherCondition);
        this.weatherSensor.tell(new WeatherSensor.ReadWeather(weatherCondition));

        return this;
    }
}
