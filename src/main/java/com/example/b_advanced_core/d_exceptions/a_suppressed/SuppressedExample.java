package com.example.b_advanced_core.d_exceptions.a_suppressed;

/**
 * Когда ошибка происходит и в try, и при закрытии ресурса, при try-with-resources исключение закрытия становится
 * suppressed (подавленное)

 * Ожидаемая логика:
 * - Основное исключение: из doWork().
 * - Suppressed: из close().
 * Это важно для диагностики: suppressed исключения часто объясняют дополнительные проблемы при очистке.
 */

public class SuppressedExample {
    public static void main(String[] args) {
        try (BrokenResource r = new BrokenResource()) {
            r.doWork();
        } catch (Exception e) {
            System.out.println("Main exception: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("Suppressed: " + s.getMessage());
            }
        }
    }
}
