package com.example.d_multithreading.k_cyclic_barrier;

public abstract class Task {
    private final long id;

    protected Task(final long id) {
        this.id = id;
    }

    public abstract void perform();

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "[id = " + id
                + "]";
    }
}
