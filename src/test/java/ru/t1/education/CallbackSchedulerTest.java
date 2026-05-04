package ru.t1.education;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.time.Instant;

import static org.junit.Assert.assertTrue;

public class CallbackSchedulerTest {
    private volatile CallbackScheduler scheduler;

    private static class CallbackResult {
        boolean isDone = false;
    }

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