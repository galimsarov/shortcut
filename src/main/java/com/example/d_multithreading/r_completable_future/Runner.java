package com.example.d_multithreading.r_completable_future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.util.concurrent.CompletableFuture.*;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Runner {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final CompletableFuture<Integer> future = supplyAsync(Runner::generateInt);
        future.thenAcceptAsync(n -> out.println("Number = " + n));
        future.thenApply(n -> n * 2).thenAccept(r -> out.println("N * 2 = " + r));
        future.thenApply(n -> n + 2).thenAccept(r -> out.println("N + 2 = " + r));
        future.get();
    }

    private static void allOfThenRunAndJoinExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> firstFuture = supplyAsync(() -> {
            int result = generateInt();
            out.println("First result: " + result);
            return result;
        });
        CompletableFuture<Integer> secondFuture = supplyAsync(() -> {
            int result = generateInt();
            out.println("Second result: " + result);
            return result;
        });
        CompletableFuture<Integer> thirdFuture = supplyAsync(() -> {
            int result = generateInt();
            out.println("Third result: " + result);
            return result;
        });
        CompletableFuture<Void> fourthFuture = allOf(firstFuture, secondFuture, thirdFuture);
        CompletableFuture<Void> fifthFuture = fourthFuture.thenRun(
                () -> {
                    int sum = firstFuture.join() + secondFuture.join() + thirdFuture.join();
                    out.println("Fifth result: " + sum);
                    out.println(sum);
                }
        );
        fifthFuture.get();
    }

    private static void tnenApplyExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> firstFuture = supplyAsync(Runner::generateInt);
        CompletableFuture<Integer> secondFuture = firstFuture.thenApply(number -> number + 1);
        out.println(secondFuture.get());
    }

    private static void runAsyncExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = runAsync(() -> out.println("Test message"));
        future.get();
    }

    private static void processUsingCompletableFuture() throws InterruptedException, ExecutionException {
        final CompletableFuture<Integer> firstFuture = supplyAsync(Runner::generateInt);
        final CompletableFuture<Integer> secondFuture = supplyAsync(Runner::generateInt);
        final CompletableFuture<Integer> thirdFuture = supplyAsync(Runner::generateInt);
        final CompletableFuture<Void> voidCompletableFuture = firstFuture
                .thenCombine(secondFuture, Integer::sum)
                .thenCombine(thirdFuture, Integer::sum)
                .thenAccept(out::println);
        voidCompletableFuture.get();
    }

    private static void processUsingFuture() throws InterruptedException, ExecutionException {
        try (final ExecutorService executor = newCachedThreadPool()) {
            final Future<Integer> firstFuture = executor.submit(Runner::generateInt);
            final Future<Integer> secondFuture = executor.submit(Runner::generateInt);
            final Future<Integer> thirdFuture = executor.submit(Runner::generateInt);
            final int sum = firstFuture.get() + secondFuture.get() + thirdFuture.get();
            out.println(sum);
        }
    }

    private static int generateInt() {
        try {
            SECONDS.sleep(3);
            return current().nextInt(0, 10);
        } catch (InterruptedException cause) {
            throw new RuntimeException(cause);
        }
    }
}
