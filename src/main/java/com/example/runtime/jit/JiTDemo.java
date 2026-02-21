package com.example.runtime.jit;

import lombok.extern.java.Log;

@Log
public class JiTDemo {
    public static void main(String[] args) {
        long sum = 0;
        for (int i = 0; i < 100_000_000; i++) {
            sum += i;
        }
        log.info(String.valueOf(sum));
    }
}
