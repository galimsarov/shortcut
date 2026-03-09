package com.example.c_advanced_core.f_stream_api.f_tasks.g_event_bus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Event {
    private String type;
    private String payload;
    private LocalDateTime timestamp;

    public Event(String type, String payload, LocalDateTime timestamp) {
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}
