package com.example.d_multithreading.j_count_down_latch_example;

import java.util.concurrent.CountDownLatch;

import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(final String[] args) {
        final int resourcesCount = 3;
        final CountDownLatch latch = new CountDownLatch(resourcesCount);

        final ResourceLoaderFactory loaderFactory = new ResourceLoaderFactory();
        final Thread[] loadingThreads = createResourceThreads(loaderFactory, resourcesCount, latch);

        final ResourceHandlerFactory handlerFactory = new ResourceHandlerFactory();
        final int handlerThreadsCount = 4;
        final Thread[] handlerThreads = createResourceThreads(handlerFactory, handlerThreadsCount, latch);

        ThreadUtil.startThreads(loadingThreads);
        ThreadUtil.startThreads(handlerThreads);
    }

    private static Thread[] createResourceThreads(
            final ResourceTaskFactory factory,
            final int threadsCount,
            final CountDownLatch latch
    ) {
        return range(0, threadsCount)
                .mapToObj(i -> factory.create(latch))
                .map(Thread::new)
                .toArray(Thread[]::new);
    }
}
