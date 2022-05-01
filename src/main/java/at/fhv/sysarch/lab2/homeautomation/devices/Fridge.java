package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.products.Product;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public static final class AddedProduct implements FridgeCommand {
        final Product product;

        public AddedProduct(Product product) {
            this.product = product;
        }
    }

    public static final class ConsumedProduct implements FridgeCommand {
        final Product product;

        public ConsumedProduct(Product product) {
            this.product = product;
        }
    }

    public static final class UpdateSpace implements FridgeCommand {
        final boolean isEnoughSpace;
        final Product productToAdd;

        public UpdateSpace(boolean isEnoughSpace, Product productToAdd) {
            this.isEnoughSpace = isEnoughSpace;
            this.productToAdd = productToAdd;
        }
    }

    public static final class UpdateWeight implements FridgeCommand {
        final boolean isNotTooHeavy;
        final Product productToAdd;

        public UpdateWeight(boolean isNotTooHeavy, Product productToAdd) {
            this.isNotTooHeavy = isNotTooHeavy;
            this.productToAdd = productToAdd;
        }
    }

    public static final class LogProducts implements FridgeCommand {
        public LogProducts() {}
    }

    public static final class LogHistory implements FridgeCommand {
        public LogHistory() {}
    }

    public static final class LogStatus implements FridgeCommand {
        public LogStatus() {}
    }

    // initializing (called by HomeAutomationController)
    public static Behavior<FridgeCommand> create(List<Product> products, String groupId, String deviceId) {
        return Behaviors.setup(context -> new Fridge(context, products, groupId, deviceId));
    }

    // class attributes
    private final String groupId;
    private final String deviceId;
    private final ActorRef<FridgeSpaceSensor.SpaceCommand> spaceSensor;
    private final ActorRef<FridgeWeightSensor.WeightCommand> weightSensor;
    private List<Product> products;
    private List<String> historyOfAddedProducts;
    private int occupiedSpace;
    private int occupiedWeight;
    private boolean poweredOn = true;

    // constructor
    public Fridge(ActorContext<FridgeCommand> context, List<Product> products, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.spaceSensor = context.spawn(FridgeSpaceSensor.create(getContext().getSelf()), "SpaceSensor");
        this.weightSensor = context.spawn(FridgeWeightSensor.create(getContext().getSelf()), "WeightSensor");
        this.products = products;
        this.historyOfAddedProducts = new ArrayList<>();

        for (Product product : this.products) {
            occupiedSpace += product.getSpace();
            occupiedWeight += product.getWeight();
            addToHistory(product);
        }

        getContext().getLog().info("Fridge started");
    }

    private void addToHistory(Product product) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = timeFormatter.format(LocalTime.now()) + " Uhr";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String currentDate = dateFormatter.format(LocalDate.now());

        historyOfAddedProducts.add(product.getName() + " added to fridge at " + currentTime + " on " + currentDate);
    }

    // behavior of Fridge class -> determines which method gets called after tell has been called from outside
    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PowerFridge.class, this::onPowerFridgeOff)
                .onMessage(LogProducts.class, this::onLogProducts)
                .onMessage(LogHistory.class, this::onLogHistory)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onMessage(AddedProduct.class, this::onAddProduct)
                .onMessage(ConsumedProduct.class, this::onConsumeProduct)
                .onMessage(UpdateSpace.class, this::onUpdateSpace)
                .onMessage(UpdateWeight.class, this::onUpdateWeight)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    // concrete implementation -> reaction to tell calls
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

        return newReceiveBuilder()
                .onMessage(PowerFridge.class, this::onPowerFridgeOff)
                .onMessage(LogProducts.class, this::onLogProducts)
                .onMessage(LogHistory.class, this::onLogHistory)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onMessage(AddedProduct.class, this::onAddProduct)
                .onMessage(ConsumedProduct.class, this::onConsumeProduct)
                .onMessage(UpdateSpace.class, this::onUpdateSpace)
                .onMessage(UpdateWeight.class, this::onUpdateWeight)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeCommand> powerOff() {
        this.poweredOn = false;

        return newReceiveBuilder()
                .onMessage(PowerFridge.class, this::onPowerFridgeOn)
                .onMessage(LogStatus.class, this::onLogStatus)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeCommand> onAddProduct(AddedProduct addedProduct) {

        // 1) check space, 2) check weight if enough space, 3) add product if enough space and not too heavy
        this.spaceSensor.tell(new FridgeSpaceSensor.ReadSpace(addedProduct.product, this.occupiedSpace));

        return this;
    }

    private Behavior<FridgeCommand> onConsumeProduct(ConsumedProduct consumedProduct) {

        Product productToConsume = consumedProduct.product;

        if (this.products.contains(productToConsume)) {
            products.remove(productToConsume);
            this.occupiedSpace -= productToConsume.getSpace();
            this.occupiedWeight -= productToConsume.getWeight();

            getContext().getLog().info(productToConsume.getName() +  " consumed from fridge");

            // add/order new product if all products are consumed
            if (!this.products.contains(productToConsume)) {
                getContext().getSelf().tell(new Fridge.AddedProduct(productToConsume));
                getContext().getLog().info("All " + productToConsume.getName() +  "s consumed. Adding another " + productToConsume.getName() + " to fridge.");
            }
        }
        else {
            getContext().getLog().info(productToConsume.getName() +  " not in fridge");
        }

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> onUpdateSpace(UpdateSpace updateSpace) {

        Product productToAdd = updateSpace.productToAdd;
        boolean isEnoughSpace = updateSpace.isEnoughSpace;

        if (isEnoughSpace) {
            this.weightSensor.tell(new FridgeWeightSensor.ReadWeight(productToAdd, this.occupiedWeight));
        }
        else {
            getContext().getLog().info(productToAdd.getName() +  " not added to fridge - not enough space available");
        }

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> onUpdateWeight(UpdateWeight updateWeight) {

        Product productToAdd = updateWeight.productToAdd;
        boolean isNotTooHeavy = updateWeight.isNotTooHeavy;

        if (isNotTooHeavy) {
            this.occupiedSpace += productToAdd.getSpace();
            this.occupiedWeight += productToAdd.getWeight();
            this.products.add(productToAdd);
            addToHistory(productToAdd);
            getContext().getLog().info(productToAdd.getName() + " added to fridge");
        }
        else {
            getContext().getLog().info(productToAdd.getName() + " not added to fridge - too much weight");
        }

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> onLogStatus(LogStatus logStatus) {
        getContext().getLog().info("groupId: " + this.groupId);
        getContext().getLog().info("deviceId: " + this.deviceId);
        getContext().getLog().info("products: " + this.products);
        getContext().getLog().info("poweredOn: " + this.poweredOn);
        getContext().getLog().info("occupiedSpace: " + this.occupiedSpace);
        getContext().getLog().info("occupiedWeight: " + this.occupiedWeight);

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> onLogProducts(LogProducts logProducts) {
        getContext().getLog().info("products: " + this.products);

        return Behaviors.same();
    }

    private Behavior<FridgeCommand> onLogHistory(LogHistory logHistory) {

        for (String entry : historyOfAddedProducts) {
            getContext().getLog().info(entry);
        }

        return Behaviors.same();
    }

    private Fridge onPostStop() {
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
