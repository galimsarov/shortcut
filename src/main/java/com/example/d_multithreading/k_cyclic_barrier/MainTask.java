package com.example.d_multithreading.k_cyclic_barrier;

import java.util.List;

public final class MainTask extends ComositeTask<Subtask> {
    public MainTask(final long id, final List<Subtask> subtasks) {
        super(id, subtasks);
    }

    @Override
    protected void perform(final Subtask subtask) {
        new Thread(subtask::perform).start();
    }
}
