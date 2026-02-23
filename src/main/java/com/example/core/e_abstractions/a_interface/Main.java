package com.example.core.e_abstractions.a_interface;

import lombok.extern.java.Log;

@Log
public class Main {
    public static void main(String[] args) {
        Swimmable.staticMethod(); // static interface method using

        log.info(String.valueOf(Swimmable.INT_CONST)); // interface field using

        Bird bird = new Bird();
        bird.defaultMethod();

    }
}
