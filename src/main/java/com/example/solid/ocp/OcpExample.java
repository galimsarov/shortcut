package com.example.solid.ocp;

/**
 * OCP: расширяем поведение без правки стабильного кода.
 */
public class OcpExample {

    // ❌ Нарушение OCP: при добавлении нового типа скидки надо менять applyDiscount.
    static class BadDiscountService {
        double applyDiscount(String type, double price) {
            if ("NEW_YEAR".equals(type)) {
                return price * 0.90;
            } else if ("VIP".equals(type)) {
                return price * 0.80;
            }
            return price;
        }
    }

    interface DiscountStrategy {
        double apply(double price);
    }

    static class NewYearDiscount implements DiscountStrategy {
        @Override
        public double apply(double price) {
            return price * 0.90;
        }
    }

    static class VipDiscount implements DiscountStrategy {
        @Override
        public double apply(double price) {
            return price * 0.80;
        }
    }

    // ✅ OCP: сервис не меняется, когда добавляются новые стратегии.
    static class DiscountService {
        double applyDiscount(DiscountStrategy strategy, double price) {
            return strategy.apply(price);
        }
    }

    public static void demo() {
        DiscountService service = new DiscountService();
        System.out.println(service.applyDiscount(new NewYearDiscount(), 1000));
        System.out.println(service.applyDiscount(new VipDiscount(), 1000));
    }
}
