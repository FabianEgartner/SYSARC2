package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.products.Apple;
import at.fhv.sysarch.lab2.homeautomation.products.Banana;
import at.fhv.sysarch.lab2.homeautomation.products.Product;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

import java.util.LinkedList;
import java.util.List;

public class HomeAutomationController extends AbstractBehavior<Void>{

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private final ActorRef<Blinds.BlindsCommand> blinds;
    private final ActorRef<MediaStation.MediaStationCommand> mediaStation;
    private final ActorRef<Fridge.FridgeCommand> fridge;
    private final ActorRef<Environment.EnvironmentCommand> environment;

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    // initialize devices
    // group 1: sensors, group 2: airCondition, group 3: blinds, group 4: MediaStation
    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);

        this.airCondition = getContext().spawn(AirCondition.create("2", "1"), "AirCondition");
        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.airCondition, "1", "1"), "TemperatureSensor");
        this.blinds = getContext().spawn(Blinds.create("3", "1"), "Blinds");
        this.weatherSensor = getContext().spawn(WeatherSensor.create(this.blinds, "1", "2"), "WeatherSensor");
        this.mediaStation = getContext().spawn(MediaStation.create(this.blinds, "4", "1"), "MediaStation");

        List<Product> products = new LinkedList(List.of(new Apple(), new Banana()));
        this.fridge = getContext().spawn(Fridge.create(products, "5", "1"), "Fridge");

        this.environment = getContext().spawn(Environment.create(this.tempSensor, this.weatherSensor), "Environment");

        ActorRef<Void> ui = getContext().spawn(UI.create(this.tempSensor, this.airCondition, this.weatherSensor, this.blinds, this.mediaStation, this.fridge, this.environment), "UI");

        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
