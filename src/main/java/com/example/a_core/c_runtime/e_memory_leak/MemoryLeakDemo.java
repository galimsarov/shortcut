package com.example.a_core.c_runtime.e_memory_leak;

import java.util.ArrayList;
import java.util.List;

public class MemoryLeakDemo {
    // Статическая коллекция удерживает ссылки на объекты до конца жизни приложения
    private static final List<byte[]> CACHE = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            CACHE.add(new byte[1024 * 1024]); // +1 MB
            System.out.println("Allocated MB: " + CACHE.size());
        }
    }
}
