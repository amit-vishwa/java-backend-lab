package com.learning.concurrency.thread_safety;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NotificationCounterDemo {

    private static final Object LOCK = new Object();

    private static int processedCount = 0;

    public static void main(String[] args) {
        System.out.println("Processed count before: " + processedCount);
        for (int i = 0; i < 5; i++) {
            if (!incrementCounterWithSynchronization()) {
                break;
            }
        }
    }

    private static boolean incrementCounter() {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        System.out.print("Expected: 100000 || ");
        processedCount = 0;
        for (int i = 0; i < 1_00_000; i++) {
            executorService.submit(() -> processedCount++);
        }
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!isTerminated) {
                System.out.println("TIMEOUT: Experiment incomplete.");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("WARNING: Workers may still be running.");
                }
                return false;
            }
            System.out.println("Actual: " + processedCount);
            return true;
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("INTERRUPTED: Experiment incomplete.");
            return false;
        }
    }

    private static boolean incrementCounterWithSynchronization() {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        System.out.print("Expected: 100000 || ");
        processedCount = 0;
        for (int i = 0; i < 1_00_000; i++) {
            executorService.submit(() -> {
                synchronized(LOCK){
                    processedCount++;
                }
            });
        }
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!isTerminated) {
                System.out.println("TIMEOUT: Experiment incomplete.");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("WARNING: Workers may still be running.");
                }
                return false;
            }
            System.out.println("Actual: " + processedCount);
            return true;
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("INTERRUPTED: Experiment incomplete.");
            return false;
        }
    }

}
