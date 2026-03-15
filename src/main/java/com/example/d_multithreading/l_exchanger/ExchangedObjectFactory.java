package com.example.d_multithreading.l_exchanger;

import java.util.concurrent.TimeUnit;

public final class ExchangedObjectFactory {
    private long nextId;

    public ExchangedObject create() {
        try {
            TimeUnit.SECONDS.sleep(2);
            return new ExchangedObject(this.nextId++);
        } catch (InterruptedException cause) {
            throw new RuntimeException(cause);
        }
    }
}
