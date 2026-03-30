package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.l_livelock;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Eater {
    @Getter
    private final String name;
    private boolean isHungry;

    public Eater(final String name) {
        this.name = name;
        this.isHungry = true;
    }

    public boolean isHungry() {
        return isHungry;
    }

    public void eatWith(final Spoon spoon, final Eater spouse) {
        while (this.isHungry) {
            // Don't have the spoon, so wait patiently for spouse
            if (spoon.getOwner() != this) {
                try {
                    SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    continue;
                }
            }
            // If spouse is hungry, insist upon passing the spoon
            if (spouse.isHungry()) {
                out.printf("%s: You eat first my darling %s!%n", name, spouse.getName());
                spoon.setOwner(spouse);
                continue;
            }
            // Spouse wasn't hungry, so finally eat
            spoon.use();
            isHungry = false;
            out.printf("%s: I am stuffed, my darling %s!%n", name, spouse.getName());
            spoon.setOwner(spouse);
        }
    }
}
