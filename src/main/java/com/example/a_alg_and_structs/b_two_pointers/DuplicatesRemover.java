package com.example.a_alg_and_structs.b_two_pointers;

import java.util.Arrays;

/**
 * Дан отсортированный массив. Удалить дубликаты на месте и вернуть новую длину массива без повторений.
 * 5 примеров тестовых данных (включая граничные случаи):
 * nums=[1, 1, 2] → новая длина 2, массив начинается с [1, 2]
 * nums=[0, 0, 1, 1, 1, 2, 2, 3, 3, 4] → новая длина 5, начало массива [0, 1, 2, 3, 4]
 * nums=[] → новая длина 0
 * nums=[7] → новая длина 1, массив [7]
 * nums=[2, 2, 2, 2] → новая длина 1, начало массива [2]
 */
public class DuplicatesRemover {
    public static void main(String[] args) {
        int[] nums = new int[]{2, 2, 2, 2};
        System.out.println(Arrays.toString(nums));
        int optimizedArrayLength = getOptimizedArrayLength(nums);
        System.out.println("length = " + optimizedArrayLength);
    }

    private static int getOptimizedArrayLength(int[] nums) {
        if (nums.length < 2) {
            for (int num : nums) {
                System.out.print(num + " ");
            }
            System.out.println();
            return nums.length;
        }
        int writeIndex = 0;
        int value = nums[0];
        System.out.println(value + " on index " + writeIndex);
        for (int i = 1; i < nums.length; i++) {
            int temp = nums[i];
            if (temp != value) {
                value = temp;
                writeIndex++;
                System.out.println(value + " on index " + writeIndex);
            }
        }
        return writeIndex + 1;
    }
}
