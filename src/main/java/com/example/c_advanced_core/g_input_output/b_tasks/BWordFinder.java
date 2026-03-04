package com.example.c_advanced_core.g_input_output.b_tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Прочитать большой текстовый файл и найти самое длинное слово.
 * Требования:
 * 1. Читать файл построчно (без загрузки целиком в память).
 * 2. Учитывать пунктуацию (очищать слово от знаков).
 * 3. Вывести:
 *  - слово,
 *  - длину,
 *  - номер строки, где оно впервые встретилось.
 * 4. Добавить обработку ошибок ввода-вывода.
 */
public class BWordFinder {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("tmp/io-demo/lorem-ipsum.txt");
        List<String> lines = Files.readAllLines(src);
        String word = "";
        int lineNumber = 0;
        for (int i = 0; i < lines.size(); i++) {
            String[] words = lines.get(i).split(" ");
            Optional<String> optional = Arrays.stream(words)
                    .map(str -> str.replace(",",""))
                    .map(str -> str.replace(".",""))
                    .max(Comparator.naturalOrder());
            if (optional.isPresent()) {
                String lineLongestWord = optional.get();
                if (lineLongestWord.length() > word.length()) {
                    word = lineLongestWord;
                    lineNumber = i + 1;
                }
            }
        }
        System.out.println("word: " + word);
        System.out.println("length: " + word.length());
        System.out.println("lineNumber: " + lineNumber);
    }
}
