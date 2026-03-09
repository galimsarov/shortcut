package com.example.c_advanced_core.g_input_output.b_tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Агрегатор логов по уровням
 * Задача: Прочитать лог-файл и посчитать количество записей каждого уровня (INFO, WARN, ERROR).
 * Требования:
 * 1. Читать файл построчно через BufferedReader.
 * 2. Формат строки: 2025-01-01 10:15:30 | INFO | message.
 * 3. Игнорировать некорректные строки, но считать их количество отдельно.
 * 4. Результат записать в итоговый файл через BufferedWriter.
 */
public class CLogsAggregator {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("tmp/io-demo/logs.txt");
        Files.createDirectories(src.getParent());
        Map<String, Integer> results = fillMap();
        try (BufferedReader reader = Files.newBufferedReader(src)) {
            reader.lines().forEach(line -> {
                String type = line.substring(22).trim();
                boolean wasCounted;
                wasCounted = countLog(type, results, "INFO");
                if (!wasCounted) {
                    wasCounted = countLog(type, results, "WARN");
                }
                if (!wasCounted) {
                    wasCounted = countLog(type, results, "ERROR");
                }
                if (!wasCounted) {
                    countInvalid(results);
                }
            });
        }
        Path dst = Paths.get("tmp/io-demo/logs-statistics.txt");
        Files.createDirectories(dst.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(dst)) {
            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        }
    }

    private static Map<String, Integer> fillMap() {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("INFO", 0);
        result.put("WARN", 0);
        result.put("ERROR", 0);
        result.put("INVALID", 0);
        return result;
    }

    private static void countInvalid(Map<String, Integer> results) {
        if (!results.containsKey("INVALID")) {
            results.put("INVALID", 1);
        } else {
            results.put("INVALID", results.get("INVALID") + 1);
        }
    }

    private static boolean countLog(String type, Map<String, Integer> results, String key) {
        if (type.startsWith(key)) {
            if (!results.containsKey(type)) {
                results.put(key, 1);
            } else {
                results.put(key, results.get(type) + 1);
            }
            return true;
        }
        return false;
    }
}
