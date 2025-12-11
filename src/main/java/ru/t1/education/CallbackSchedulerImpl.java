package ru.t1.education;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallbackSchedulerImpl implements CallbackScheduler {
    private final Timer timer = new Timer("callback-timer", true); // daemon потоки
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void schedule(Runnable callback, Instant when) {
        if (callback == null) throw new IllegalArgumentException("Callback cannot be null");
        if (when == null) throw new IllegalArgumentException("When cannot be null");
        if (closed.get()) throw new IllegalStateException("Scheduler is closed");

        long delay = Math.max(0, when.toEpochMilli() - System.currentTimeMillis());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callback.run();
            }
        }, delay);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            timer.cancel(); // останавливает таймер и его поток
        }
    }
}