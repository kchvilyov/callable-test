package ru.t1.education;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CallbackSchedulerTest {
    private volatile CallbackScheduler scheduler;

    private static class CallbackResult {
        boolean isDone = false;
    }

    @BeforeEach
    public void setUp() {
        scheduler = new CallbackSchedulerImpl();
    }

    @AfterEach
    public void tearDown() throws Exception {
        scheduler.close();
    }

    @Test
    public void testSimple() throws InterruptedException {
        final CallbackResult result = new CallbackResult();

        scheduler.schedule(
                () -> {
                    synchronized (result) {
                        System.out.println("1" + result);
                        result.isDone = true;
                        result.notify();
                        System.out.println("2" + result);
                    }
                },
                Instant.now().plusSeconds(2)
        );

        synchronized (result) {
            System.out.println("3" + result);
            result.wait();
            assertTrue(result.isDone);
        }
    }
}