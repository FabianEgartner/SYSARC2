package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.BlindsState;
import at.fhv.sysarch.lab2.homeautomation.devices.enums.WeatherCondition;

import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private ActorRef<Blinds.BlindsCommand> blinds;
    private ActorRef<MediaStation.MediaStationCommand> mediaStation;

    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
                                        ActorRef<AirCondition.AirConditionCommand> airCondition,
                                        ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
                                        ActorRef<Blinds.BlindsCommand> blinds,
                                        ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, weatherSensor, blinds, mediaStation));
    }

    private UI(ActorContext<Void> context,
               ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
               ActorRef<AirCondition.AirConditionCommand> airCondition,
               ActorRef<WeatherSensor.WeatherCommand> weatherSensor,
               ActorRef<Blinds.BlindsCommand> blinds,
               ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        super(context);
        // TODO: implement actor and behavior as needed
        // TODO: move UI initialization to appropriate place
        this.airCondition = airCondition;
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
        this.blinds = blinds;
        this.mediaStation = mediaStation;
        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");

            // TemperatureSensor
            if(command[0].equals("t")) {
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(Double.valueOf(command[1]))));
            }

            // AirCondition
            if(command[0].equals("a")) {
                String booleanInput = command[1].toLowerCase();

                if (booleanInput.equals("true")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(true));
                }
                else if (booleanInput.equals("false")) {
                    this.airCondition.tell(new AirCondition.PowerAirCondition(false));
                }
            }

            if(command[0].equals("a_status")) {
                this.airCondition.tell(new AirCondition.LogStatus());
            }

            // WeatherSensor
            if(command[0].equals("w")) {
                String weatherInput = command[1].toUpperCase();

                if (weatherInput.equals(WeatherCondition.SUNNY.toString())) {
                    this.weatherSensor.tell(new WeatherSensor.ReadWeather(WeatherCondition.SUNNY));
                }
                else if (weatherInput.equals(WeatherCondition.CLOUDY.toString())) {
                    this.weatherSensor.tell(new WeatherSensor.ReadWeather(WeatherCondition.CLOUDY));
                }
            }

            if(command[0].equals("w_status")) {
                this.weatherSensor.tell(new WeatherSensor.LogStatus());
            }

            // Blinds
            if(command[0].equals("b")) {
                String blindsStateInput = command[1].toUpperCase();

                if (blindsStateInput.equals(BlindsState.OPEN.toString())) {
                    this.blinds.tell(new Blinds.MoveBlindsWeatherSensor(BlindsState.OPEN));
                }
                else if (blindsStateInput.equals(BlindsState.CLOSED.toString())) {
                    this.blinds.tell(new Blinds.MoveBlindsWeatherSensor(BlindsState.CLOSED));
                }
            }

            if(command[0].equals("b_status")) {
                this.blinds.tell(new Blinds.LogStatus());
            }

            // MediaStation
            if(command[0].equals("m")) {
                String mediaStationStateInput = command[1].toLowerCase();

                if (mediaStationStateInput.equals("true")) {
                    this.mediaStation.tell(new MediaStation.WatchMovie(true));
                }
                else if (mediaStationStateInput.equals("false")) {
                    this.mediaStation.tell(new MediaStation.WatchMovie(false));
                }
            }

            if(command[0].equals("m_status")) {
                this.mediaStation.tell(new MediaStation.LogStatus());
            }

            // TODO: process Input
        }

        getContext().getLog().info("UI done");
    }
}
