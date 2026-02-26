package com.example.b_advanced_core.d_exceptions;

import java.io.IOException;

/**
 * Checked exceptions
 * Это исключения, которые обязаны быть либо пойманы (catch), либо объявлены (throws).
 * Когда полезны: для recoverable-сценариев (например, I/O, сеть, интеграции).
 */

public class CheckedExample {
    static String readConfig() throws IOException {
        throw new IOException("config file not found");
    }

    public static void main(String[] args) {
        try {
            String config = readConfig();
            System.out.println(config);
        } catch (IOException e) {
            System.out.println("Failed to read config: " + e.getMessage());
        }
    }
}
