package com.rarchives.ripme.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DebouncedRunnable implements Runnable, AutoCloseable {
    private final ScheduledExecutorService scheduler;
    private final Runnable task;
    private final long maxDelayMs;
    private final AtomicLong lastInvoke = new AtomicLong();
    private final AtomicLong lastRun = new AtomicLong();
    private volatile ScheduledFuture<?> future;
    private final AtomicLong counter = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final Thread shutdownHook;
    private volatile boolean closed = false;

    /**
     * Debounce a task.
     * @param task The task to run
     * @param maxDelayMs Maximum delay to wait between calls in milliseconds
     */
    public DebouncedRunnable(Runnable task, long maxDelayMs) {
        this.task = task;
        this.maxDelayMs = maxDelayMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        this.shutdownHook = new Thread(this::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void run() {
        long now = System.currentTimeMillis();
        long timeSinceLastRun = now - lastRun.getAndSet(now);
        boolean overMaxDelaySinceLastRun = timeSinceLastRun >= maxDelayMs;
        if (overMaxDelaySinceLastRun) {
            invoke();
            return;
        }
        long timeSinceLastInvoke = now - lastInvoke.get();
        boolean underMaxDelaySinceLastInvoke = timeSinceLastInvoke < maxDelayMs;
        if (underMaxDelaySinceLastInvoke && future != null) {
            future.cancel(false);
        }
        future = scheduler.schedule(this::invoke, maxDelayMs - timeSinceLastInvoke, TimeUnit.MILLISECONDS);

    }

    private void invoke() {
        try {
            active.set(true);
            task.run();
        } finally {
            lastInvoke.set(System.currentTimeMillis());
            active.set(false);
        }
    }

    public void shutdown() {
        closed = true;
        scheduler.shutdown();
        if (future != null && !future.isDone() && !active.get()) {
            // Shutting down, but we still have a scheduled thread
            future.cancel(false);
            invoke();
        }
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            // The shutdown has already begun
        }
    }

    public void close() throws Exception {
        if (!closed) {
            shutdown();
        }
    }
}
