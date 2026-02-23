package com.example.core.e_abstractions.a_interface;

import lombok.extern.java.Log;

@Log
public class Bird implements Flyable, Swimmable {
    @Override
    public void fly() {
        log.info("Bird fly");
    }

    @Override
    public void swim() {
        log.info("Bird swim");
    }
}
