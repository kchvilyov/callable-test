package ru.t1.education;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CancelTest {
    @Test
    public void testCancelTask() throws InterruptedException {
        CallbackScheduler scheduler = new CallbackSchedulerImpl();
        AtomicBoolean executed = new AtomicBoolean(false);

        // Планируем задачу на 5 секунд
        ScheduledFuture<?> future = scheduler.schedule(
            () -> executed.set(true),
            Instant.now().plusSeconds(5)
        );

        // Отменяем задачу
        future.cancel(false);

        // Ждём 6 секунд — если задача не выполнится, всё ок
        Thread.sleep(6000);

        assertFalse(executed.get(), "Task was cancelled and should not execute");
        scheduler.close();
    }
}