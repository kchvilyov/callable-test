package ru.t1.education;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
interface CallbackScheduler extends AutoCloseable {
    void schedule(Runnable callback, Instant when) throws InterruptedException;
}

public class CallbackSchedulerImpl implements CallbackScheduler {
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
