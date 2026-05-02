package ru.t1.education;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CancelTest {
    @Test
    public void testCancelTask() throws InterruptedException {
        CallbackScheduler scheduler = new CallbackSchedulerImpl();
        try {
            AtomicBoolean executed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = scheduler.schedule(
                () -> { executed.set(true); latch.countDown(); },
                    Instant.now().plusSeconds(5));

            future.cancel(false);
            // Ожидаем не более 1 секунды – задача не должна выполниться
            latch.await(1, TimeUnit.SECONDS);
            assertFalse(executed.get(), "Task was cancelled and should not execute");
        } finally {
            scheduler.close();
        }
    }
}