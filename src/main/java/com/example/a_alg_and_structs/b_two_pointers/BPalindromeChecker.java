package com.example.a_alg_and_structs.b_two_pointers;

/**
 * Проверить, является ли она палиндромом, игнорируя пробелы, знаки препинания и регистр.
 * 5 примеров тестовых данных (включая граничные случаи):
 * "A man, a plan, a canal: Panama" → true
 * "race a car" → false
 * " " → true (строка только из пробелов)
 * "0P" → false
 * "" → true (пустая строка)
 */

public class BPalindromeChecker {
    public static void main(String[] args) {
        String s = "";
        System.out.println(s);
        boolean isPalindrome = isPalindrome(s);
        System.out.println(isPalindrome);
    }

    public static boolean isPalindrome(String str) {
        String s = str
                .trim()
                .replace(" ", "")
                .replace(":", "")
                .replace(",", "")
                .toLowerCase();
        if (s.isEmpty() || s.length() == 1) {
            return true;
        }
        int start = 0;
        int end = s.length() - 1;
        while (start < end) {
            if (s.charAt(start) != s.charAt(end)) {
                return false;
            }
            start++;
            end--;
        }
        return true;
    }
}
