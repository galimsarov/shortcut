package com.example.a_algos.a_two_pointers;

import java.util.Arrays;

/**
 * дан массив чисел, нужно все ненулевые элементы сдвинуть влево, сохранив порядок, и все нули должны оказаться в конце массива.
 * Требования: О(n) по времени и O(1) по памяти
 * {1,2,0,3,0,0,4,5} -> {1,2,3,4,5,0,0,0}
 */
public class HeroesOnTheLeft {
    public static void main(String[] args) {
//        int[] arr = new int[]{1, 2, 0, 3, 0, 0, 4, 5};
//        int[] arr = new int[]{1, 2, 3, 4, 5, 0, 0, 0};
//        int[] arr = new int[]{0, 0, 0, 1, 2, 3, 4, 5};
        int[] arr = new int[]{0, 1, 0, 2, 0, 3, 0, 4};
        System.out.println(Arrays.toString(arr));
        sortArray(arr);
        System.out.println(Arrays.toString(arr));
    }

    private static void sortArray(int[] arr) {
        int index = 0;
        int leftZero = -1;
        int rightNotZero = -1;
        while (index < arr.length) {
            int value = arr[index];
            int tmp = leftZero;
            if (value == 0) {
                leftZero = index;
            } else {
                rightNotZero = index;
            }
            if ((rightNotZero > leftZero) && (leftZero != -1)) {
                arr[leftZero] = arr[rightNotZero];
                arr[rightNotZero] = 0;
                leftZero++;
                rightNotZero = leftZero - 1;
            } else if (tmp != -1) {
                leftZero = tmp;
            }
            index++;
        }
    }
}
