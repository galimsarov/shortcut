package com.example.b_core.d_gc.g1;

import java.util.ArrayList;
import java.util.List;

public class G1BehaviorDemo {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<>();

        for (int i = 1; i <= 10_000; i++) {
            // короткоживущие объекты
            byte[] tmp = new byte[1024 * 50];

            // часть объектов делаем долгоживущими
            if (i % 100 == 0) {
                list.add(new byte[1024 * 200]);
            }

            if (i % 500 == 0) {
                System.out.println("step=" + i + ", retained=" + list.size());
            }
        }
    }
}
