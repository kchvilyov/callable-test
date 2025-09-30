package ru.t1.education;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertTrue;

interface CallbackScheduler extends AutoCloseable {
    void schedule(Runnable callback, Instant when) throws InterruptedException;
}


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

    class CallbackSchedulerImpl implements CallbackScheduler {
        @Override
        public void schedule(Runnable callback, Instant when) throws InterruptedException {
            System.out.println("Scheduling " + callback.getClass().getSimpleName());
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    callback.run();
                }
            }, 2000);
        }

        @Override
        public void close() throws Exception {
            System.out.println("Closing " + this.getClass().getSimpleName());
        }
    }
}