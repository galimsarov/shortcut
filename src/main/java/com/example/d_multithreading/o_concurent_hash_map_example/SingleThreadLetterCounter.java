package com.example.d_multithreading.o_concurent_hash_map_example;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class SingleThreadLetterCounter extends LetterCounter {
    public SingleThreadLetterCounter() {
        super(1);
    }

    @Override
    protected Map<Character, Integer> createAccumulator() {
        return new HashMap<>();
    }

    @Override
    protected void execute(Stream<Subtask> subtasks) {
        subtasks.forEach(Subtask::execute);
    }
}
