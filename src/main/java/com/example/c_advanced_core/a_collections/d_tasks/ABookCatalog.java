package com.example.c_advanced_core.a_collections.d_tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 1.1 Простой: Каталог книг по жанрам
 * Задача: Реализовать консольную программу, которая хранит книги по жанрам.

 * Требования:

 * Использовать Map<String, List<String>> (ключ — жанр, значение — список книг).
 * Добавить методы:
 * addBook(String genre, String title)
 * printBooksByGenre(String genre)
 * printAllGenres()
 * Если жанр отсутствует — создать его автоматически.
 * Не допускать дубликатов книг внутри одного жанра.
 * Подсказка: можно перейти на Map<String, Set<String>>, если хотите проще решать задачу с дубликатами.
 */

public class ABookCatalog {
    private static final Map<String, Set<String>> GENRES_BY_BOOKS = new HashMap<>();

    private static void addBook(String genre, String title) {
        Set<String> books = GENRES_BY_BOOKS.get(genre);
        if (books == null) {
            books = new HashSet<>();
        }
        books.add(title);
        GENRES_BY_BOOKS.put(genre, books);
    }

    private static void printBooksByGenre(String genre) {
        Set<String> books = GENRES_BY_BOOKS.get(genre);
        System.out.println("Books by genre " + genre + ":");
        for (String book : books) {
            System.out.println(book);
        }
        System.out.println();
    }

    private static void printAllGenres() {
        System.out.println("All Genres:");
        for (String genre : GENRES_BY_BOOKS.keySet()) {
            System.out.println(genre);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        addBook("Children's book", "Harry Potter");
        addBook("Fantasy", "Harry Potter");
        addBook("Children's book", "Thumbelina");
        addBook("Fantasy", "Lord of the Rings");
        addBook("Detective", "Ten little Indians");

        printBooksByGenre("Children's book");
        printBooksByGenre("Fantasy");
        printBooksByGenre("Detective");

        printAllGenres();
    }
}
