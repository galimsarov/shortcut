package com.example.a_alg_and_structs.b_two_pointers;

import java.util.Arrays;

/**
 * Дан массив высот. Найти две линии, которые вместе с осью X образуют контейнер максимальной площади.
 * 5 примеров тестовых данных (включая граничные случаи):
 * [1, 8, 6, 2, 5, 4, 8, 3, 7] → 49
 * [1, 1] → 1 (минимально допустимая длина)
 * [4, 3, 2, 1, 4] → 16
 * [1, 2, 1] → 2
 * [0, 0, 0, 0] → 0 (все высоты нулевые)
 */
public class MaxAreFinder {
    public static void main(String[] args) {
        int[] arr = new int[]{0, 0, 0, 0};
        System.out.println(Arrays.toString(arr));
        int maxArea = findMaxArea(arr);
        System.out.println("maxArea: " + maxArea);
    }

    private static int findMaxArea(int[] arr) {
        int maxArea = 0;
        int left = 0;
        int maxLeft = 0;
        int right = arr.length - 1;
        int maxRight = arr.length - 1;
        while (left < right) {
            int height = Math.min(arr[left], arr[right]);
            int currentArea = height * (right - left);
            if (currentArea > maxArea) {
                maxArea = currentArea;
                maxLeft = left;
                maxRight = right;
            }
            if (left + 1 < right) {
                height = Math.min(arr[left + 1], arr[right]);
                currentArea = height * (right - (left + 1));
                if (currentArea > maxArea) {
                    maxArea = currentArea;
                    maxLeft = left + 1;
                    maxRight = right;
                }
            }
            if (left < right - 1) {
                height = Math.min(arr[left], arr[right - 1]);
                currentArea = height * ((right - 1) - left);
                if (currentArea > maxArea) {
                    maxArea = currentArea;
                    maxLeft = left;
                    maxRight = right - 1;
                }
            }
            left++;
            right--;
        }
        System.out.println("maxLeft index: " + maxLeft);
        System.out.println("maxRight index: " + maxRight);
        return maxArea;
    }
}
