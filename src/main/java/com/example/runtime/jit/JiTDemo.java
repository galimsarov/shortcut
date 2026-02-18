package com.example.runtime.jit;

public class JiTDemo {
    public static void main(String[] args) {
        long sum = 0;
        for (int i = 0; i < 100_000_000; i++) {
            sum += i;
        }
        System.out.println(sum);
    }
}
