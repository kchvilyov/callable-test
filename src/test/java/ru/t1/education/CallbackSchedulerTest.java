package ru.t1.education;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CallbackSchedulerTest {
    private volatile CallbackScheduler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new CallbackSchedulerImpl();
    }

    @AfterEach
    public void tearDown() {
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
        boolean waited = latch.await(3, TimeUnit.SECONDS);

        assertTrue(waited, "Task did not complete in time");
        assertTrue(isDone[0], "Callback was not executed");
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

        assertTrue(completed, "Task should complete");
        assertTrue(duration >= 1800, "Task executed too early"); // >= 1.8s
        assertTrue(duration <= 3000,"Task took too long");     // <= 3s
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
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(!completed && !executed.get(), "Task executed after scheduler was closed");
    }

    @Test
    public void testCloseIsIdempotent() {
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

        boolean completed = latch.await(4, TimeUnit.SECONDS);

        assertTrue(completed, "All tasks should complete");
        assertTrue(task1.get(), "Task 1 executed");
        assertTrue(task2.get(), "Task 2 executed");
        assertTrue(task3.get(), "Task 3 executed");
    }
}