package com.example.d_multithreading.m_phaser;

public abstract class Task {
    private final long id;

    protected Task(long id) {
        this.id = id;
    }

    public abstract void perform();

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[id = " + id + "]";
    }
}
