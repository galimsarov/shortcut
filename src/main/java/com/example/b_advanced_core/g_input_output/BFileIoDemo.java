package com.example.b_advanced_core.g_input_output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * Старый API: File (метаданные, путь, базовые операции).
 * Современный API: Path/Files (удобнее и богаче по функционалу).
 */
public class BFileIoDemo {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("tmp/io-demo");
        Path file = dir.resolve("notes.txt");

        Files.createDirectories(dir);

        // Запись строки в файл
        Files.writeString(
                file,
                "line-1\nline-2\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        // Дозапись
        Files.writeString(file, "line-3\n", StandardOpenOption.APPEND);

        // Чтение
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        lines.forEach(System.out::println);

        // Копирование
        Path copy = dir.resolve("notes-copy.txt");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
    }
}
