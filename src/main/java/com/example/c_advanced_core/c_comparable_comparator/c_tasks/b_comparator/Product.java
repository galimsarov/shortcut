package com.example.c_advanced_core.c_comparable_comparator.c_tasks.b_comparator;

import lombok.Getter;

@Getter
public class Product {
    private String name;
    private double price;
    private double rating;
    private Category category;

    public Product(String name, double price, double rating, Category category) {
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", category=" + category +
                '}';
    }
}
