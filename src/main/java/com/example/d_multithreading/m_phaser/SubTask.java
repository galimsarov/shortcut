package com.example.d_multithreading.m_phaser;

import java.util.List;
import java.util.stream.Stream;

public final class SubTask extends CompositeTask<AbstractLeafTask> {
    public SubTask(final long id, final List<LeafTask> leafTasks, final LastLeafTask lastLeafTask) {
        super(id, concat(leafTasks, lastLeafTask));
    }

    @Override
    public void perform(final AbstractLeafTask leafTask) {
        leafTask.perform();
    }

    private static List<AbstractLeafTask> concat(final List<LeafTask> leafTasks, final LastLeafTask lastLeafTask) {
        return Stream.concat(
                leafTasks.stream(),
                Stream.of(lastLeafTask)
        ).toList();
    }
}
