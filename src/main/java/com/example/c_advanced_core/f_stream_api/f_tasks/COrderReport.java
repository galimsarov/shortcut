package com.example.c_advanced_core.f_stream_api.f_tasks;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сформировать отчёт по коллекции заказов Order.
 * Требования:
 * - Поля заказа: id, customer, amount, status, createdAt.
 * - Через stream получить:
 * - сумму завершённых заказов,
 * - топ-3 клиентов по сумме,
 * - группировку по статусам,
 * - средний чек.
 * Использовать groupingBy, mapping, reducing или summarizingDouble.
 * Сравнить один из шагов с императивной реализацией (2–3 строки комментария).
 */
public class COrderReport {
    public static void main(String[] args) {
        List<BOrder> orders = new ArrayList<>(List.of(
                new BOrder(1, "Alex", 5.0, "Finished", LocalDateTime.of(2025, Month.DECEMBER, 1, 14, 00)),
                new BOrder(2, "Bill", 10.0, "In progress", LocalDateTime.of(2026, Month.JANUARY, 15, 14, 00)),
                new BOrder(3, "Charlie", 4.0, "New", LocalDateTime.of(2026, Month.MARCH, 3, 14, 00)),
                new BOrder(4, "David", 8.0, "Finished", LocalDateTime.of(2025, Month.DECEMBER, 15, 14, 00))
        ));
        double finishedOrdersSum = orders.stream()
                .filter(order -> order.getStatus().equals("Finished"))
                .map(order -> order.getAmount())
                .reduce(0.0, Double::sum);
        System.out.println(finishedOrdersSum);
        List<String> topClients = orders.stream()
                .sorted(Comparator.comparing(BOrder::getAmount).reversed())
                .limit(3)
                .map(BOrder::getCustomer)
                .toList();
        System.out.println(topClients);
        Map<String, List<BOrder>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(BOrder::getStatus));
        System.out.println(ordersByStatus);
    }
}
