package com.example.d_multithreading.m_phaser;

import java.util.List;

public final class MainTask extends CompositeTask<SubTask> {
    public MainTask(final long id, final List<SubTask> subtasks) {
        super(id, subtasks);
    }

    @Override
    public void perform(final SubTask subtask) {
        new Thread(subtask::perform).start();
    }
}
