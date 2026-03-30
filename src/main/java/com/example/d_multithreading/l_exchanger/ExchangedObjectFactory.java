package com.example.d_multithreading.l_exchanger;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class ExchangedObjectFactory {
    private long nextId;

    public ExchangedObject create() {
        try {
            SECONDS.sleep(2);
            return new ExchangedObject(this.nextId++);
        } catch (InterruptedException cause) {
            throw new RuntimeException(cause);
        }
    }
}
