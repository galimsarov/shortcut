package com.example.d_multithreading.k_cyclic_barrier;

import java.util.List;

public abstract class ComositeTask<S extends Task> extends Task {
    private final List<S> subtasks;

    protected ComositeTask(final long id, final List<S> subtasks) {
        super(id);
        this.subtasks = subtasks;
    }

    @Override
    public final void perform() {
        this.subtasks.forEach(this::perform);
    }

    protected abstract void perform(final S subtask);
}
