package com.example.a_alg_and_structs.c_streams.d_orders_chain;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Дан список Order и набор условий фильтрации: минимальная сумма, допустимые статусы, период дат. С помощью Predicate<Order> собрать составной фильтр и вернуть подходящие заказы.
 * 5 примеров тестовых данных:
 *
 * Заказы: [(id=1, 7000, PAID, 2026-01-10), (id=2, 3000, PAID, 2026-01-10)],
 * фильтр: min=5000, statuses=[PAID], период=январь → [1]
 *
 * Заказы: [(1, 6000, NEW, 2026-01-11), (2, 9000, SHIPPED, 2026-01-12)],
 * фильтр: min=5000, statuses=[NEW,SHIPPED], период=январь → [1,2]
 *
 * Заказы: [(1, 12000, CANCELLED, 2026-01-08), (2, 8000, PAID, 2025-12-30)],
 * фильтр: min=5000, statuses=[PAID], период=январь → []
 *
 * Заказы: [(1, 5000, PAID, 2026-01-05), (2, 4999, PAID, 2026-01-06)],
 * фильтр: min=5000, statuses=[PAID], период=январь → [1]
 *
 * Заказы: [(1, 7000, PAID, 2026-02-01), (2, 7000, PAID, 2026-01-31)],
 * фильтр: min=5000, statuses=[PAID], период=январь → [2]
 */
public class ChainBuilder {
    public static void main(String[] args) {
//        firstTest();
//        secondTest();
//        thirdTest();
//        fourthTest();
        fifthTest();
    }

    private static Predicate<Order> getBigOrdersWithStatuses(Status... statuses) {
        return order ->
                (order.getPrice() >= 5000)
                && (Arrays.stream(statuses).toList().contains(order.getStatus()))
                && (order.getDate().getMonth() == Month.JANUARY);
    }

    private static void fifthTest() {
        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, 7000, Status.PAID, LocalDate.of(2026, Month.FEBRUARY, 1)),
                new Order(2, 7000, Status.PAID, LocalDate.of(2026, Month.JANUARY, 31))
        ));
        orders.forEach(System.out::println);
        List<Order> filteredOrders = orders.stream()
                .filter(getBigOrdersWithStatuses(Status.PAID))
                .toList();
        System.out.println("Filtered orders:");
        filteredOrders.forEach(System.out::println);
    }

    private static void fourthTest() {
        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, 5000, Status.PAID, LocalDate.of(2026, Month.JANUARY, 5)),
                new Order(2, 4999, Status.PAID, LocalDate.of(2026, Month.JANUARY, 6))
        ));
        orders.forEach(System.out::println);
        List<Order> filteredOrders = orders.stream()
                .filter(getBigOrdersWithStatuses(Status.PAID))
                .toList();
        System.out.println("Filtered orders:");
        filteredOrders.forEach(System.out::println);
    }

    private static void thirdTest() {
        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, 12000, Status.CANCELLED, LocalDate.of(2026, Month.JANUARY, 8)),
                new Order(2, 8000, Status.PAID, LocalDate.of(2025, Month.DECEMBER, 30))
        ));
        orders.forEach(System.out::println);
        List<Order> filteredOrders = orders.stream()
                .filter(getBigOrdersWithStatuses(Status.PAID))
                .toList();
        System.out.println("Filtered orders:");
        filteredOrders.forEach(System.out::println);
    }

    private static void secondTest() {
        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, 6000, Status.NEW, LocalDate.of(2026, Month.JANUARY, 11)),
                new Order(2, 9000, Status.SHIPPED, LocalDate.of(2026, Month.JANUARY, 12))
        ));
        orders.forEach(System.out::println);
        List<Order> filteredOrders = orders.stream()
                .filter(getBigOrdersWithStatuses(Status.NEW, Status.SHIPPED))
                .toList();
        System.out.println("Filtered orders:");
        filteredOrders.forEach(System.out::println);
    }

    private static void firstTest() {
        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, 7000, Status.PAID, LocalDate.of(2026, Month.JANUARY, 10)),
                new Order(2, 3000, Status.PAID, LocalDate.of(2026, Month.JANUARY, 10))
        ));
        orders.forEach(System.out::println);
        List<Order> filteredOrders = orders.stream()
                .filter(getBigOrdersWithStatuses(Status.PAID))
                .toList();
        System.out.println("Filtered orders:");
        filteredOrders.forEach(System.out::println);
    }


}
