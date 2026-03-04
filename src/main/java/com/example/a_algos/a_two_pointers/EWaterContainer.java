package com.example.a_algos.a_two_pointers;

import java.util.Arrays;

/**
 * Container With Most Water
 * Дано: массив высот.
 * Нужно найти две линии, которые вместе образуют контейнер максимальной площади.
 * Пример
 * [1,8,6,2,5,4,8,3,7]
 * Ответ: 49
 * 📌 Это классика двух указателей навстречу друг другу.
 * Сложность: O(n)
 */
public class EWaterContainer {
    public static void main(String[] args) {
        int[] arr = new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7};
        int[] containerIndexes = getContainerIndexes(arr);
        System.out.println(Arrays.toString(arr));
        System.out.println(Arrays.toString(containerIndexes));
        System.out.println(getArea(containerIndexes));
    }

    private static int[] getContainerIndexes(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        int maxArea = 0;
        int maxLeft = -1;
        int maxRight = -1;
        while (left < right) {
            // Находим минимальный столбик
            int minHeight = Math.min(arr[left], arr[right]);
            int width = right - left;
            int currentArea = minHeight * width;
            if (maxArea < currentArea) {
                maxArea = currentArea;
                maxLeft = left;
                maxRight = right;
            }
            if (left + 1 < right) {
                minHeight = Math.min(arr[left + 1], arr[right]);
                width = right - (left + 1);
                currentArea = minHeight * width;
                if (maxArea < currentArea) {
                    maxArea = currentArea;
                    maxLeft = left + 1;
                    maxRight = right;
                }
            }
            if (left < right - 1) {
                minHeight = Math.min(arr[left], arr[right - 1]);
                width = (right - 1) - left;
                currentArea = minHeight * width;
                if (maxArea < currentArea) {
                    maxArea = currentArea;
                    maxLeft = left;
                    maxRight = right - 1;
                }
            }
            left++;
            right--;
        }
        return new int[]{maxLeft, maxRight};
    }

    private static int getArea(int[] containerIndexes) {
        int height = Math.min(containerIndexes[0], containerIndexes[1]);
        int width = containerIndexes[1] - containerIndexes[0];
        return height * width;
    }
}
