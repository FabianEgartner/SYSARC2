package at.fhv.sysarch.lab2;

import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;

public class HomeAutomationSystem {

    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(), "HomeAutomation");

        // private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
        // private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

        // public static Behavior<EnvironementCommand> create() {
        //  return Behaviors.setup(context -> Behaviors.withTimers(timers -> nwe Environment(context, timers, timers)));
        // }

        // private Environment(ActorContext<EnvironmentCommand>
        // this.temperatureTimeScheduler.startTimerAtFixedRate( new TemperatureChanger(Optional.of(temperature)). Duration.ofSeconds(
    }
}
