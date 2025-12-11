package ru.t1.education;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

/**
 * Интерфейс для планировщика обратных вызовов.
 * Позволяет запланировать выполнение задачи (Runnable) в определённый момент времени.
 */
public interface CallbackScheduler extends AutoCloseable {
    /**
     * Запланировать выполнение обратного вызова в указанное время.
     *
     * @param callback задача, которую нужно выполнить
     * @param when     момент времени, когда нужно выполнить задачу
     * @return объект ScheduledFuture, позволяющий отменить задачу
     */
    ScheduledFuture<?> schedule(Runnable callback, Instant when);

    /**
     * Закрыть планировщик обратных вызовов.
     */
    @Override
    void close();
}