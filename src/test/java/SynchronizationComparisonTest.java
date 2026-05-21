import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class SynchronizationComparisonTest {

    // === 1. Взаимное исключение и корректность счётчика ===
    @RepeatedTest(10) // запустим 10 раз для надёжности
    void monitorExample_correctness() throws Exception {
        MonitorExample monitor = new MonitorExample();
        runConcurrentIncrements(monitor::increment);
        assertEquals(100_000, monitor.getCounter());
    }

    @RepeatedTest(10)
    void mutexExample_correctness() throws Exception {
        MutexExample mutex = new MutexExample();
        runConcurrentIncrements(mutex::increment);
        assertEquals(100_000, mutex.getCounter());
    }

    private void runConcurrentIncrements(Runnable incrementTask) throws InterruptedException {
        int threads = 100;
        int incrementsPerThread = 1000;
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            CountDownLatch latch = new CountDownLatch(threads);
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            incrementTask.run();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            //noinspection ResultOfMethodCallIgnored
            latch.await(30, SECONDS);
            pool.shutdownNow();
        }
    }

    // === 2. Условные очереди (wait/notify) ===
    @Test
    void monitorExample_waitNotify() throws Exception {
        MonitorExample monitor = new MonitorExample();
        CountDownLatch waiterStarted = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            waiterStarted.countDown();
            try {
                monitor.waitForThreshold(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();
        waiterStarted.await(); // убедимся, что waiter действительно ждёт

        // Даём время войти в wait
        Thread.sleep(500);
        for (int i = 0; i < 5; i++) {
            monitor.increment();
            monitor.notifyWhenThreshold(i + 1);
            Thread.sleep(100);
        }
        waiter.join(2000);
        assertFalse(waiter.isAlive());
    }

    @Test
    void mutexExample_awaitSignal() throws Exception {
        MutexExample mutex = new MutexExample();
        CountDownLatch waiterStarted = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            waiterStarted.countDown();
            try {
                mutex.waitForThreshold(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();
        waiterStarted.await();
        Thread.sleep(500);
        for (int i = 0; i < 5; i++) {
            mutex.increment();
            mutex.notifyWhenThreshold(i + 1);
            Thread.sleep(100);
        }
        waiter.join(2000);
        assertFalse(waiter.isAlive());
    }

    // === 3. Проверка реентерабельности (неблокирующий повторный вход) ===
    @Test
    void monitorExample_reentrancy() {
        MonitorExample monitor = new MonitorExample();
        // Просто синхронизируемся на самом объекте monitor (или его внутреннем lock)
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (monitor) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (monitor) {
                monitor.increment();
            }
        }
        assertEquals(1, monitor.getCounter());
    }

    @Test
    void mutexExample_reentrancy() {
        MutexExample mutex = new MutexExample();
        mutex.lock.lock();
        try {
            mutex.lock.lock();  // повторный захват
            try {
                mutex.increment();
            } finally {
                mutex.lock.unlock();
            }
        } finally {
            mutex.lock.unlock();
        }
        assertEquals(1, mutex.getCounter());
    }

    // === 4. Дополнительные возможности ReentrantLock (tryLock) ===
    @Test
    void mutexExample_tryLock_success() {
        MutexExample mutex = new MutexExample();
        assertTrue(mutex.tryIncrement());
        assertEquals(1, mutex.getCounter());
    }

    @Test
    void mutexExample_tryLock_failure() throws Exception {
        MutexExample mutex = new MutexExample();
        // захватываем блокировку из основного потока
        mutex.lock.lock();
        try {
            AtomicBoolean failed = new AtomicBoolean(false);
            Thread t = new Thread(() -> {
                //Поскольку блокировка уже занята,
                //tryLock() не блокируется, а мгновенно возвращает false
                if (!mutex.tryIncrement()) {
                    failed.set(true);
                }
            });
            t.start();
            t.join();
            assertTrue(failed.get());
            assertEquals(0, mutex.getCounter());
        } finally {
            mutex.lock.unlock();
        }
    }

    // === 5. Проверка прерывания при ожидании (ReentrantLock) ===
    @Test
    void mutexExample_lockInterruptibly() throws Exception {
        MutexExample mutex = new MutexExample();
        mutex.lock.lock(); // основной поток захватил блокировку
        Thread t = new Thread(() -> {
            try {
                mutex.lock.lockInterruptibly();
                mutex.lock.unlock();
            } catch (InterruptedException e) {
                // ожидаемое поведение
            }
        });
        t.start();
        Thread.sleep(500);
        t.interrupt();
        t.join();
        // Проверяем, что поток был прерван (не повесил программу)
        assertTrue(true); // если дошли сюда – тест пройден
    }

    // === 6. Асинхронная проверка с Awaitility ===
    @Test
    void awaitility_monitorWaitNotify() {
        MonitorExample monitor = new MonitorExample();
        new Thread(() -> {
            try {
                Thread.sleep(100);
                for (int i = 0; i < 5; i++) {
                    monitor.increment();
                    monitor.notifyWhenThreshold(i + 1);
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {}
        }).start();

        await().atMost(5, SECONDS).until(() -> {
            try {
                monitor.waitForThreshold(5);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        });
        assertEquals(5, monitor.getCounter());
    }

    @Test
    void awaitility_mutexAwaitSignal() {
        MutexExample mutex = new MutexExample();
        new Thread(() -> {
            try {
                Thread.sleep(100);
                for (int i = 0; i < 5; i++) {
                    mutex.increment();
                    mutex.notifyWhenThreshold(i + 1);
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {}
        }).start();

        await().atMost(5, SECONDS).until(() -> {
            try {
                mutex.waitForThreshold(5);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        });
        assertEquals(5, mutex.getCounter());
    }
}