package com.example.d_multithreading.a_wait_and_notify_example.producer;

import com.example.d_multithreading.a_wait_and_notify_example.model.Message;

import static java.lang.String.format;

public final class MessageFactory {
    private static final int INITIAL_NEXT_MESSAGE_INDEX = 1;
    private static final String TEMPLATE_CREATED_MESSAGE_DATA = "Message#%d";

    private int nextMessageIndex;

    public MessageFactory() {
        this.nextMessageIndex = INITIAL_NEXT_MESSAGE_INDEX;
    }

    public Message create() {
        return new Message(format(TEMPLATE_CREATED_MESSAGE_DATA, this.findAndIncrementMessageIndex()));
    }

    private synchronized int findAndIncrementMessageIndex() {
        return this.nextMessageIndex++;
    }
}
