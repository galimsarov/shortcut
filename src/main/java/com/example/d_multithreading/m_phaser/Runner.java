package com.example.d_multithreading.m_phaser;

import java.util.List;
import java.util.concurrent.Phaser;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class Runner {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(3) {
            @Override
            protected boolean onAdvance(final int phase, final int parties) {
                out.println();
                out.printf("Thread: %s%n", currentThread().getName());
                out.printf("Current phase: %d%n", phase);
                out.printf("Current parties: %d%n", parties);
                out.println();
                return super.onAdvance(phase, parties);
            }
        };

        final LeafTask firstLeafTask = new LeafTask(0, 5, phaser);
        final LeafTask secondLeafTask = new LeafTask(1, 3, phaser);
        final LastLeafTask firstLastLeafTask = new LastLeafTask(0, 1, phaser);
        final SubTask firstSubTask = new SubTask(0, List.of(firstLeafTask, secondLeafTask), firstLastLeafTask);

        final LeafTask thirdLeafTask = new LeafTask(2, 6, phaser);
        final LastLeafTask secondLastLeafTask = new LastLeafTask(1, 4, phaser);
        final SubTask secondSubTask = new SubTask(1, List.of(thirdLeafTask), secondLastLeafTask);

        final LastLeafTask thirdLastLeafTask = new LastLeafTask(2, 7, phaser);
        final SubTask thirdSubTask = new SubTask(2, List.of(), thirdLastLeafTask);

        final MainTask mainTask = new MainTask(0, List.of(firstSubTask, secondSubTask, thirdSubTask));
        mainTask.perform();
    }
}
