package com.example.a_alg_and_structs.c_streams.a_txs_analyzer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Дан список транзакций Transaction с полями String category, BigDecimal amount, LocalDateTime date.
 * Необходимо написать метод, который вернет Map, где ключ — категория, а значение — сумма всех транзакций в этой категории за последний месяц (относительно текущей даты).
 * В результирующую мапу должны попасть только те категории, сумма транзакций в которых превышает заданный порог (например, 5000).
 * <p>
 * public class Transaction {
 * private String category;
 * private BigDecimal amount;
 * private LocalDateTime date;
 * <p>
 * // Конструктор, геттеры, сеттеры, toString
 * }
 * Пример данных:
 * Сегодня: 2026-01-20
 * Транзакции:
 * <p>
 * "Products", 1500, 2026-01-10
 * "Restaurants", 3200, 2026-01-05
 * "Products", 2500, 2025-12-15
 * "Transport", 800, 2026-01-01
 * "Restaurants", 2500, 2026-01-18
 * "Products", 3000, 2026-01-19
 */
public class TxsAnalyzer {
    public static void main(String[] args) {
        Map<String, BigDecimal> map = getTxsByCategory(getTransactions(), new BigDecimal("5000"));
        System.out.println(map);
    }

    private static Map<String, BigDecimal> getTxsByCategory(List<Transaction> transactions, BigDecimal minCostLimit) {
        Map<String, BigDecimal> result = new HashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getDate().isAfter(LocalDateTime.now().minusMonths(3)))
                .collect(Collectors.groupingBy(Transaction::getCategory))
                .forEach((category, list) -> {
                    double totalCost = list.stream()
                            .map(Transaction::getAmount)
                            .map(BigDecimal::doubleValue)
                            .reduce(0.0, Double::sum);
                    if (totalCost >= minCostLimit.doubleValue()) {
                        result.put(category, new BigDecimal(totalCost));
                    }
                });
        return result;
    }

    private static List<Transaction> getTransactions() {
        return new ArrayList<>(List.of(
                new Transaction("Products", new BigDecimal("1500"), LocalDateTime.of(2026, Month.JANUARY, 10, 23, 59)),
                new Transaction("Restaurants", new BigDecimal("3200"), LocalDateTime.of(2026, Month.JANUARY, 5, 23, 59)),
                new Transaction("Products", new BigDecimal("2500"), LocalDateTime.of(2025, Month.DECEMBER, 15, 23, 59)),
                new Transaction("Transport", new BigDecimal("800"), LocalDateTime.of(2026, Month.JANUARY, 1, 23, 59)),
                new Transaction("Restaurants", new BigDecimal("2500"), LocalDateTime.of(2026, Month.JANUARY, 18, 23, 59)),
                new Transaction("Products", new BigDecimal("3000"), LocalDateTime.of(2026, Month.JANUARY, 19, 23, 59))
        ));
    }
}
