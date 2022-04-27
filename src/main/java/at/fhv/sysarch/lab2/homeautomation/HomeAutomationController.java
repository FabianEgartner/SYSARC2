package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.TypedActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

public class HomeAutomationController extends AbstractBehavior<Void>{

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    // initialize devices
    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?
        this.airCondition = getContext().spawn(AirCondition.create("2", "1"), "AirCondition");
        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.airCondition, "1", "1"), "TemperatureSensor");
        this.weatherSensor = getContext().spawn(WeatherSensor.create("1", "2"), "WeatherSensor");

        ActorRef<Void> ui = getContext().spawn(UI.create(this.tempSensor, this.airCondition, this.weatherSensor), "UI");

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
