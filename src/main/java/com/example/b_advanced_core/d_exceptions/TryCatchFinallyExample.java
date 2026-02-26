package com.example.b_advanced_core.d_exceptions;

/**
 * finally обычно используют для освобождения ресурсов, если не применили try-with-resources
 */

public class TryCatchFinallyExample {
    public static void main(String[] args) {
        try {
            System.out.println("Opening the operation");
            int x = 10 / 0;
            System.out.println(x);
        } catch (ArithmeticException e) {
            System.out.println("Caught an arithmetic error: " + e.getMessage());
        } finally {
            System.out.println("finally will almost always be executed (cleaning up resources)");
        }
    }
}
