package ru.t1.education;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class CallbackSchedulerTest {

    private CallbackScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CallbackSchedulerImpl();
    }

    @AfterEach
    void tearDown() {
        scheduler.close();
    }

    // 1. Простой позитивный сценарий: задача должна выполниться
    @Test
    void testSimple() {
        AtomicBoolean done = new AtomicBoolean(false);

        scheduler.schedule(() ->
            done.set(true)
        , Instant.now().plusSeconds(2));

        //ждём
        await()
                //самое большее 3 секунды
                .atMost(3, TimeUnit.SECONDS)
                //до тех пор, пока done не true
                .until(done::get);
    }

    // 2. Проверка точности времени выполнения (с некоторым допуском)
    @Test
    void testExecutionTimeAccuracy() throws InterruptedException {
        Instant start = Instant.now();
        CountDownLatch latch = new CountDownLatch(1);

        scheduler.schedule(latch::countDown, start.plusSeconds(2));

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Task should complete");
        long duration = Duration.between(start, Instant.now()).toMillis();

        // Не раньше чем через 1.8 сек (учитываем погрешности)
        assertTrue(duration >= 1800, "Task executed too early");
        // Не позже чем через 3 сек (щедрый запас)
        assertTrue(duration <= 3000, "Task took too long");
    }

    // 3. Планирование задачи в прошлом — она должна выполниться немедленно
    @Test
    void testScheduledInPast() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        scheduler.schedule(() -> {
            executed.set(true);
            latch.countDown();
        }, Instant.now().minusSeconds(10)); // 10 сек назад

        assertTrue(latch.await(1, TimeUnit.SECONDS), "Task scheduled in past must execute immediately");
        assertTrue(executed.get(), "Task should be executed");
    }

    // 4. Закрытый планировщик должен выбрасывать исключение при попытке schedule
    @Test
    void testScheduleAfterCloseThrows() {
        scheduler.close();
        assertThrows(IllegalStateException.class, () ->
                scheduler.schedule(() -> {}, Instant.now().plusSeconds(1))
        );
    }

    // 5. Закрытый планировщик не выполняет задачу, если она была запланирована до закрытия?
    //    В нашем случае schedule после close явно запрещён. Проверим идимпотентность close.
    @Test
    void testCloseIsIdempotent() {
        scheduler.close();
        assertDoesNotThrow(() -> scheduler.close());
    }

    // 6. Множественные задачи выполняются все
    @Test
    void testMultipleTasks() throws InterruptedException {
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

        assertTrue(latch.await(4, TimeUnit.SECONDS), "All tasks should complete");
        assertTrue(task1.get(), "Task 1 executed");
        assertTrue(task2.get(), "Task 2 executed");
        assertTrue(task3.get(), "Task 3 executed");
    }

    // 7. Отмена задачи до её выполнения (через ScheduledFuture)
    @Test
    void testCancelTask() throws InterruptedException {
        CallbackScheduler localScheduler = new CallbackSchedulerImpl();
        try {
            AtomicBoolean executed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = localScheduler.schedule(
                    () -> {
                        executed.set(true);
                        latch.countDown();
                    },
                    Instant.now().plusSeconds(5)
            );

            future.cancel(false);
            // Ждём 1 секунду — задача не должна успеть выполниться
            latch.await(1, TimeUnit.SECONDS);
            assertFalse(executed.get(), "Task was cancelled and should not execute");
        } finally {
            localScheduler.close();
        }
    }

    // 8. Отмена с прерыванием (cancel(true)) — задача спит, должна быть прервана
    @Test
    void testCancelWithInterrupt() throws InterruptedException {
        CallbackScheduler localScheduler = new CallbackSchedulerImpl();
        try {
            AtomicBoolean interrupted = new AtomicBoolean(false);
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch finished = new CountDownLatch(1);

            ScheduledFuture<?> future = localScheduler.schedule(
                    () -> {
                        started.countDown();
                        try {
                            Thread.sleep(10_000); // долгий сон
                        } catch (InterruptedException e) {
                            interrupted.set(true);
                        }
                        finished.countDown();
                    },
                    Instant.now().plusMillis(100) // почти сразу
            );

            // Дожидаемся, когда задача начнёт выполняться
            assertTrue(started.await(1, TimeUnit.SECONDS));
            // Отменяем с прерыванием
            future.cancel(true);

            // Задача должна завершиться с прерыванием почти сразу
            assertTrue(finished.await(1, TimeUnit.SECONDS), "Task should finish quickly after interrupt");
            assertTrue(interrupted.get(), "Thread should have been interrupted");
        } finally {
            localScheduler.close();
        }
    }

    // 9. Исключение в задаче не должно влиять на другие задачи (планировщик продолжает работать)
    @Test
    void testExceptionInTaskDoesNotAffectOthers() throws InterruptedException {
        CountDownLatch latchGood = new CountDownLatch(1);
        AtomicBoolean goodExecuted = new AtomicBoolean(false);

        // Первая задача — бросает исключение
        scheduler.schedule(() -> {
            throw new RuntimeException("Task failure");
        }, Instant.now().plusMillis(50));

        // Вторая задача — должна выполниться
        scheduler.schedule(() -> {
            goodExecuted.set(true);
            latchGood.countDown();
        }, Instant.now().plusSeconds(1));

        assertTrue(latchGood.await(2, TimeUnit.SECONDS), "Second task should still execute");
        assertTrue(goodExecuted.get(), "Good task was not executed");
    }

    // 10. Многопоточное добавление задач: несколько потоков одновременно планируют задачи
    @Test
    void testConcurrentScheduling() throws InterruptedException {
        int threadCount = 10;
        int tasksPerThread = 20;
        int totalTasks = threadCount * tasksPerThread;
        CountDownLatch allDone = new CountDownLatch(totalTasks);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable addTasks = () -> {
            for (int i = 0; i < tasksPerThread; i++) {
                scheduler.schedule(() -> {
                    successCount.incrementAndGet();
                    allDone.countDown();
                }, Instant.now().plusMillis(10));
            }
        };

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(addTasks);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }

        assertTrue(allDone.await(5, TimeUnit.SECONDS), "All tasks should complete");
        assertEquals(totalTasks, successCount.get(), "Every scheduled task must be executed");
    }
}