package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.WeatherCondition;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    // interface
    public interface EnvironmentCommand {}

    // classes or "methods" callable -> triggered by tell
    public static final class SetTemperature implements EnvironmentCommand {
        final Optional<Double> temperature;

        public SetTemperature(Optional<Double> temperature) {
            this.temperature = temperature;
        }
    }

    public static final class SetWeatherConditions implements EnvironmentCommand {
        final WeatherCondition weatherCondition;

        public SetWeatherConditions(WeatherCondition weatherCondition) {
            this.weatherCondition = weatherCondition;
        }
    }

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
    private double temperature = 20.0;
    private WeatherCondition weatherCondition = WeatherCondition.CLOUDY;
    private boolean setHighTemp = false;
    private boolean setLowTemp = false;

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
        this.temperatureTimeScheduler.startTimerAtFixedRate(new ChangedTemperature(), Duration.ofSeconds(60));
        this.weatherTimeScheduler.startTimerAtFixedRate(new ChangedWeatherConditions(), Duration.ofSeconds(60));
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ChangedTemperature.class, this::onChangeTemperature)
                .onMessage(ChangedWeatherConditions.class, this::onChangeWeatherConditions)
                .onMessage(SetTemperature.class, this::onSetTemperature)
                .onMessage(SetWeatherConditions.class, this::onSetWeatherConditions)
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

        Random random = new Random();

        // add a random double between -2.00 and +2.00 to current temperature
        temperature += random.nextDouble() * 4 - 2;
        temperature = Math.round(temperature * 100.00) / 100.00;

        if (temperature >= 25.0) {
            setHighTemp = true;
            setLowTemp = false;

        } else if (temperature <= 15.0) {
            setHighTemp = false;
            setLowTemp = true;

        } else {
            setHighTemp = false;
            setLowTemp = false;
        }

        getContext().getLog().info("Environment (Temperature) received {}", temperature);
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

        getContext().getLog().info("Environment (Weather) received {}", weatherCondition);
        this.weatherSensor.tell(new WeatherSensor.ReadWeather(weatherCondition));

        return this;
    }

    private Behavior<EnvironmentCommand> onSetTemperature(SetTemperature setTemperature) {

        if (setTemperature.temperature.isPresent() && temperature != setTemperature.temperature.get()) {
            temperature = setTemperature.temperature.get();

            getContext().getLog().info("Environment (Temperature) received {}", temperature);
            this.temperatureSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(temperature)));
        }

        return this;
    }

    private Behavior<EnvironmentCommand> onSetWeatherConditions(SetWeatherConditions setWeatherConditions) {

        if (!weatherCondition.equals(setWeatherConditions.weatherCondition)) {
            weatherCondition = setWeatherConditions.weatherCondition;

            getContext().getLog().info("Environment (Weather) received {}", weatherCondition);
            this.weatherSensor.tell(new WeatherSensor.ReadWeather(weatherCondition));
        }

        return this;
    }
}
