package com.example.c_advanced_core.d_exceptions;

public class ErrorNote {
    public static void main(String[] args) {
        // Не делайте так в прод-коде:
        // try { ... } catch (OutOfMemoryError e) { ... }
        // Обычно правильнее предотвратить причину и мониторить JVM.
    }
}
