package com.example.c_advanced_core.g_input_output.a_examples;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * В классическом java.io есть 2 базовые иерархии:
 * - InputStream/OutputStream — работа с байтами.
 * - Reader/Writer — работа с символами (текст, кодировки).
 */
public class ABasicStreamDemo {
    public static void main(String[] args) throws Exception {
        inputOutputStreamDemo();
        readerWriterDemo();
    }

    /**
     * Это низкоуровневая работа с байтами.
     * Использовать, если:
     * - читаешь/пишешь файлы любого типа
     * - работаешь с изображениями
     * - передаёшь PDF
     * - читаешь zip
     * - работаешь с сетью (сокеты)
     * - сериализация
     * - бинарные протоколы
     * Здесь данные — просто последовательность байтов. JVM не пытается их интерпретировать.
     */
    private static void inputOutputStreamDemo() throws IOException {
        // Байт-уровень: в памяти
        byte[] src = "Hello IO".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(src);
             OutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            String result = out.toString();
            System.out.println(result); // Hello IO
        }
    }

    /**
     * Это работа с текстом.
     * Использовать, если:
     * - читаешь .txt
     * - работаешь с JSON
     * - читаешь XML
     * - читаешь CSV
     * - лог-файлы
     * - конфигурации
     * Здесь уже происходит:
     * 👉 преобразование байтов в символы
     * 👉 учёт кодировки
     */
    private static void readerWriterDemo() throws IOException {
        // Символьный уровень: Reader/Writer
        try (Reader reader = new StringReader("Привет, Reader/Writer");
             StringWriter writer = new StringWriter()) {

            char[] cbuf = new char[8];
            int read;
            while ((read = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, read);
            }

            System.out.println(writer.toString());
        }
    }
}