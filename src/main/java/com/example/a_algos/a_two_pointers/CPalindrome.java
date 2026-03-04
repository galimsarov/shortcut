package com.example.a_algos.a_two_pointers;

/**
 * Нужно проверить, является ли она палиндромом.
 * Игнорируем пробелы и регистр.
 * Пример
 * "A man a plan a canal Panama" → true
 * Ограничения
 * - O(n)
 * - O(1) памяти
 * - без создания новой строки
 * 📌 Подсказка: два указателя слева и справа.
 */
public class CPalindrome {
    public static void main(String[] args) {
//        String str = "A man a plan a canal Panama";
        String str = "Deed";
        boolean isPalindrome = isPalindrome(str);
        System.out.println(str);
        System.out.println(isPalindrome);
    }

    private static boolean isPalindrome(String str) {
        int left = 0;
        int right = str.length() - 1;
        while (left < right) {
            char leftChar;
            while (true) {
                leftChar = str.charAt(left);
                if (leftChar == ' ') {
                    left++;
                } else {
                    break;
                }
            }
            leftChar = Character.toLowerCase(leftChar);

            char rightChar;
            while (true) {
                rightChar = str.charAt(right);
                if (rightChar == ' ') {
                    right--;
                } else {
                    break;
                }
            }
            rightChar = Character.toLowerCase(rightChar);
            if (leftChar != rightChar) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
