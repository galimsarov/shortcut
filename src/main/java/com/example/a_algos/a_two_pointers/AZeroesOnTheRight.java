package com.example.a_algos.a_two_pointers;

import java.util.Arrays;

/**
 * дан массив чисел, нужно все ненулевые элементы сдвинуть влево, сохранив порядок, и все нули должны оказаться в конце массива.
 * Требования: О(n) по времени и O(1) по памяти
 * {1,2,0,3,0,0,4,5} -> {1,2,3,4,5,0,0,0}
 */
public class AZeroesOnTheRight {
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
        int readIndex = 0;
        int writeIndex = 0;
        while (readIndex < arr.length) {
            int temp = arr[readIndex];
            if (temp != 0) {
                arr[writeIndex] = temp;
                writeIndex++;
            }
            readIndex++;
        }
        for (int i = writeIndex; i < arr.length; i++) {
            arr[i] = 0;
        }
    }
}
