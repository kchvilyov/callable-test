package ru.t1.education;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallbackSchedulerImpl implements CallbackScheduler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "scheduler-worker");
        t.setDaemon(false);
        return t;
    });
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void schedule(Runnable callback, Instant when) {
        if (callback == null) throw new IllegalArgumentException("Callback cannot be null");
        if (when == null) throw new IllegalArgumentException("When cannot be null");
        if (closed.get()) throw new IllegalStateException("Scheduler is closed");

        long delay = Math.max(0, when.toEpochMilli() - System.currentTimeMillis());
        executor.schedule(callback, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}