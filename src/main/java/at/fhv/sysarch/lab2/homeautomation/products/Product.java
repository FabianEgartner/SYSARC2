package at.fhv.sysarch.lab2.homeautomation.products;

import java.util.Objects;

public abstract class Product {

    protected String name;
    protected double price;
    protected int weight;
    protected int space;

    public Product(String name, int weight, int space, double price) {
        this.name = name;
        this.weight = weight;
        this.space = space;
        this.price = price;
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

    @Override
    public String toString() {return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(this.name, product.name);
    }
}