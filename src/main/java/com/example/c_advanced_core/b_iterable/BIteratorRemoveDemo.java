package com.example.c_advanced_core.b_iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BIteratorRemoveDemo {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = nums.iterator();

        while (it.hasNext()) {
            Integer n = it.next();
            if (n % 2 == 0) {
                it.remove(); // безопасно удаляем текущий элемент
            }
        }

        System.out.println(nums); // [1, 3, 5]
    }
}
