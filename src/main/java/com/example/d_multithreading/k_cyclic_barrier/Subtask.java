package com.example.d_multithreading.k_cyclic_barrier;

import java.util.List;

public final class Subtask extends ComositeTask<LeafTask> {
    public Subtask(final long id, final List<LeafTask> leafTasks) {
        super(id, leafTasks);
    }

    @Override
    protected void perform(LeafTask leafTask) {
        leafTask.perform();
    }
}
