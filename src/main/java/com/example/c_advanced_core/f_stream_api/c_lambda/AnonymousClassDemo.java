package com.example.c_advanced_core.f_stream_api.c_lambda;

import java.util.Comparator;

/**
 * До Java 8 поведение часто передавали через анонимные классы:
 */

public class AnonymousClassDemo {
    public static void main(String[] args) {
        Comparator<String> byLength = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        };

        System.out.println(byLength.compare("cat", "elephant")); // < 0
    }
}
