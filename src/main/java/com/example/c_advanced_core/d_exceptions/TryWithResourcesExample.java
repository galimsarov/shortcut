package com.example.c_advanced_core.d_exceptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Рекомендуемый способ работы с ресурсами (Closeable/AutoCloseable).
 * Плюс: ресурс закрывается автоматически, даже если в блоке try возникло исключение.
 */

public class TryWithResourcesExample {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("app.properties"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.out.println("Error reading file: The file specified cannot be found.");
        }
    }
}
