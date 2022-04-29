package at.fhv.sysarch.lab2.homeautomation.products;

public abstract class Product {

    protected String name;
    protected int weight;
    protected int space;

    public Product(String name, int weight, int space) {
        this.name = name;
        this.weight = weight;
        this.space = space;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }
}