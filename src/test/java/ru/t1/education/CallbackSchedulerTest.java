package ru.t1.education;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.assertTrue;

public class CallbackSchedulerTest {
    private volatile CallbackScheduler scheduler;

    @Before
    public void setUp() {
        scheduler = new CallbackSchedulerImpl();
    }

    @After
    public void tearDown() throws Exception {
        scheduler.close();
    }

    @Test
    public void testSimple() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] isDone = {false}; // используем массив для доступа из лямбды

        scheduler.schedule(
                () -> {
                    isDone[0] = true;
                    latch.countDown(); // сигнал о завершении
                },
                Instant.now().plusSeconds(2)
        );

        // Ждём выполнения задачи (максимум 3 секунды)
        boolean waited = latch.await(3, java.util.concurrent.TimeUnit.SECONDS);

        assertTrue("Task did not complete in time", waited);
        assertTrue("Callback was not executed", isDone[0]);
    }

    @Test
    public void testExecutionTimeAccuracy() throws InterruptedException {
        Instant start = Instant.now();
        CountDownLatch latch = new CountDownLatch(1);

        scheduler.schedule(
                latch::countDown,
                start.plusSeconds(2)
        );

        boolean completed = latch.await(3, java.util.concurrent.TimeUnit.SECONDS);
        long duration = java.time.Duration.between(start, Instant.now()).toMillis();

        assertTrue("Task should complete", completed);
        assertTrue("Task executed too early", duration >= 1800); // >= 1.8s
        assertTrue("Task took too long", duration <= 3000);     // <= 3s
    }
}