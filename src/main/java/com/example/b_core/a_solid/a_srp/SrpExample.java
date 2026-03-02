package com.example.b_core.a_solid.a_srp;

/**
 * SRP: один класс — одна ответственность.
 */
public class SrpExample {

    // ❌ Нарушение SRP: один класс и рассчитывает сумму, и печатает чек, и сохраняет данные.
    static class BadOrderService {
        double calculateTotal(double[] prices) {
            double total = 0;
            for (double price : prices) {
                total += price;
            }
            return total;
        }

        void printReceipt(double total) {
            System.out.println("Receipt total: " + total);
        }

        void saveToDatabase(double total) {
            System.out.println("Saved to DB: " + total);
        }
    }

    // ✅ SRP: отдельные классы для отдельных задач.
    static class OrderCalculator {
        double calculateTotal(double[] prices) {
            double total = 0;
            for (double price : prices) {
                total += price;
            }
            return total;
        }
    }

    static class ReceiptPrinter {
        void print(double total) {
            System.out.println("Receipt total: " + total);
        }
    }

    static class OrderRepository {
        void save(double total) {
            System.out.println("Saved to DB: " + total);
        }
    }

    public static void demo() {
        double[] prices = {100.0, 250.0, 50.0};

        OrderCalculator calculator = new OrderCalculator();
        ReceiptPrinter printer = new ReceiptPrinter();
        OrderRepository repository = new OrderRepository();

        double total = calculator.calculateTotal(prices);
        printer.print(total);
        repository.save(total);
    }
}
