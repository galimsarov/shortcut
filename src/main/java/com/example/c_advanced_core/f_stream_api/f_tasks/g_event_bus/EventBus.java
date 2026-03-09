package com.example.c_advanced_core.f_stream_api.f_tasks.g_event_bus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Сделать простой in-memory EventBus, где обработчики событий подписываются через лямбды.
 * Требования:
 * 1. Базовый класс/record Event (тип, payload, timestamp).
 * 2. EventBus поддерживает:
 * - subscribe(String eventType, Consumer<Event> handler),
 * - publish(Event event).
 * 3. Для одного типа события поддержать несколько подписчиков.
 * В main показать минимум 2 типа событий и 3 обработчика-лямбды.
 */
public class EventBus {
    private static final Map<String, List<Consumer<Event>>> EVENTS = new HashMap<>();

    public static void main(String[] args) {
        subscribe("USER_CREATED", event -> System.out.println("Event created: " + event));
        subscribe("USER_CREATED", event -> System.out.println("Printed from event type: USER_CREATED"));
        subscribe("USER_UPDATED", event -> System.out.println("Event updated: " + event));
        subscribe("USER_UPDATED", event -> System.out.println("Printed from event type: USER_UPDATED"));

        publish(new Event("USER_CREATED", "Basic user", LocalDateTime.now()));
        System.out.println("---");
        publish(new Event("USER_UPDATED", "Super user", LocalDateTime.now()));
    }

    private static void subscribe(String eventType, Consumer<Event> consumer) {
        EVENTS.computeIfAbsent(eventType, k -> new ArrayList<>()).add(consumer);
    }

    private static void publish(Event event) {
        EVENTS.get(event.getType()).forEach(consumer -> consumer.accept(event));

    }
}
