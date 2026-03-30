package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.l_livelock;

import lombok.Getter;

import static java.lang.System.out;

@Getter
public class Spoon {
    private Eater owner;

    public Spoon(Eater owner) {
        this.owner = owner;
    }

    public synchronized void setOwner(Eater owner) {
        this.owner = owner;
    }

    public synchronized void use() {
        out.printf("%s has eaten!%n", this.owner.getName());
    }
}
