package ru.t1.education;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class CancelTest {
    @Test
    void testCancelTask() {
        try (CallbackScheduler localScheduler = new CallbackSchedulerImpl()) {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScheduledFuture<?> future = localScheduler.schedule(
                    () -> executed.set(true),
                    Instant.now().plusSeconds(5)
            );

            future.cancel(false);

            // Условие "executed == false" должно выполняться непрерывно в течение 2 секунд
            await()
                    .during(2, TimeUnit.SECONDS)
                    .until(() -> !executed.get());
        }
    }

    @Test
    void testAutoCloseable() {
        AtomicBoolean closed = new AtomicBoolean(false);

        try (CallbackScheduler ignored = new CallbackSchedulerImpl() {
            @Override
            public void close() {
                closed.set(true);
                super.close();
            }
        }) {
            // просто используем блок
        }
        assertTrue(closed.get(), "close() must be called implicitly by try-with-resources");
    }
}