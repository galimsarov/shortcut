package com.example.c_advanced_core.g_input_output.b_tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Скопировать содержимое одного текстового файла в другой.
 * Требования:
 * - Вход: путь к исходному и целевому файлу.
 * - Использовать BufferedReader/BufferedWriter.
 * - Сохранять структуру строк (переносы).
 * - Использовать try-with-resources.
 */
public class AFileCopier {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("tmp/io-demo/lorem-ipsum.txt");
        Path dst = Paths.get("tmp/io-demo/copy-buffered.txt");
        Files.createDirectories(src.getParent());
        try (
                BufferedReader reader = Files.newBufferedReader(src);
                BufferedWriter writer = Files.newBufferedWriter(dst)
        ) {
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                writer.write(buf, 0, numRead);
            }

        }
    }
}
