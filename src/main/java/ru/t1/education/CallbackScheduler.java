package ru.t1.education;

import java.time.Instant;

interface CallbackScheduler extends AutoCloseable {
    void schedule(Runnable callback, Instant when) throws InterruptedException;
}
