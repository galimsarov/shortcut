package com.example.c_advanced_core.c_comparable_comparator.c_tasks.b_comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Для класса Product реализовать несколько стратегий сортировки.
 * <p>
 * Требования:
 * <p>
 * Поля: name, price, rating, category.
 * Создать компараторы:
 * по цене,
 * по рейтингу,
 * по категории + цене,
 * по имени без учёта регистра.
 * Добавить составной компаратор с thenComparing.
 * Сделать утилитный метод sortProducts(List<Product>, Comparator<Product>).
 */
public class ProductComparators {
    public static Comparator<Product> priceComparator() {
        return Comparator.comparingDouble(Product::getPrice);
    }

    public static Comparator<Product> ratingComparator() {
        return Comparator.comparingDouble(Product::getRating);
    }

    public static Comparator<Product> categoryAndPriceComparator() {
        return Comparator.comparing(Product::getCategory).reversed()
                .thenComparing(Product::getPrice, Comparator.reverseOrder());
    }

    public static Comparator<Product> nameComparator() {
        return Comparator.comparing(o -> o.getName().toUpperCase());
    }

    public static void sortProduct(List<Product> products, Comparator<Product> comparator) {
        products.sort(comparator);
    }

    public static void main(String[] args) {
        List<Product> products = new ArrayList<>(List.of(
                new Product("Chery", 1.5, 5.0, Category.A),
                new Product("Banana", 0.9, 4.5, Category.B),
                new Product("Apple", 1.0, 5.0, Category.A),
                new Product("Donut", 0.5, 4.75, Category.C),
                new Product("APPLE", 100, 5.0, Category.A)
        ));
        System.out.println("Before sorting: " + products);
        sortProduct(products, nameComparator());
        System.out.println("After sorting: " + products);
    }
}
