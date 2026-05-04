package ru.t1.education;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TaskWithCallbacksTest {

    private CallbackScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new CallbackSchedulerImpl();
    }

    @AfterEach
    void tearDown() {
        scheduler.close();
    }

    @Test
    void testExplicitCallbacks() {
        Task<String> task = () -> "result";
        TaskCallback<String> cb1 = mock(TaskCallback.class);
        TaskCallback<String> cb2 = mock(TaskCallback.class);

        Runnable runnable = new TaskWithCallbacks<>(task, List.of(cb1, cb2));
        scheduler.schedule(runnable, Instant.now().plusSeconds(1));

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(cb1).onSuccess("result");
            verify(cb2).onSuccess("result");
        });
    }
}