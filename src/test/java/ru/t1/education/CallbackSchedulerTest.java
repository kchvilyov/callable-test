package ru.t1.education;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    public void testSchedulerClosed() throws Exception {
        scheduler.close(); // закрываем ДО планирования

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        try {
            scheduler.schedule(
                    () -> {
                        executed.set(true);
                        latch.countDown();
                    },
                    Instant.now().plusSeconds(1)
            );
            // Если реализация выбрасывает IllegalStateException, это нормально
            // Если нет — ждём и проверим, что задача НЕ выполнилась
        } catch (IllegalStateException e) {
            // Ожидаемое поведение: нельзя планировать после close()
            return; // тест прошёл
        }

        // Если исключение не выброшено — убедимся, что задача не выполнилась
        boolean completed = latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue("Task executed after scheduler was closed", !completed && !executed.get());
    }

    @Test
    public void testCloseIsIdempotent() throws Exception {
        scheduler.close(); // первый вызов
        scheduler.close(); // второй — не должен сломать
        // Если не упало — всё ок
    }

    @Test
    public void testMultipleTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean task1 = new AtomicBoolean(false);
        AtomicBoolean task2 = new AtomicBoolean(false);
        AtomicBoolean task3 = new AtomicBoolean(false);

        Instant now = Instant.now();

        scheduler.schedule(() -> {
            task1.set(true);
            latch.countDown();
        }, now.plusSeconds(1));

        scheduler.schedule(() -> {
            task2.set(true);
            latch.countDown();
        }, now.plusSeconds(2));

        scheduler.schedule(() -> {
            task3.set(true);
            latch.countDown();
        }, now.plusSeconds(3));

        boolean completed = latch.await(4, java.util.concurrent.TimeUnit.SECONDS);

        assertTrue("All tasks should complete", completed);
        assertTrue("Task 1 executed", task1.get());
        assertTrue("Task 2 executed", task2.get());
        assertTrue("Task 3 executed", task3.get());
    }
}