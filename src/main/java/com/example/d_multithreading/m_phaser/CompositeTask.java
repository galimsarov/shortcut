package com.example.d_multithreading.m_phaser;

import java.util.List;

public abstract class CompositeTask<S extends Task> extends Task {
    private final List<S> subtasks;

    protected CompositeTask(final long id, final List<S> subtasks) {
        super(id);
        this.subtasks = subtasks;
    }

    @Override
    public final void perform() {
        subtasks.forEach(this::perform);
    }

    protected abstract void perform(final S task);
}
