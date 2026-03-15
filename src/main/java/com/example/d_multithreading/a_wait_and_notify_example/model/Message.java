package com.example.d_multithreading.a_wait_and_notify_example.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Message {
    private final String data;

    public Message(String data) {
        this.data = data;
    }
}
