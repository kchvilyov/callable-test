package ru.t1.education;

import java.util.ArrayList;
import java.util.List;

// Обёртка, превращающая задачу с коллбеками в Runnable
public class TaskWithCallbacks<T> implements Runnable {
    private final Task<T> task;
    private final List<TaskCallback<T>> callbacks;

    public TaskWithCallbacks(Task<T> task, List<TaskCallback<T>> callbacks) {
        this.task = task;
        this.callbacks = new ArrayList<>(callbacks); // защитная копия
    }

    @Override
    public void run() {
        T result = null;
        Throwable error = null;
        try {
            result = task.execute();
        } catch (Throwable t) {
            error = t;
        }

        for (TaskCallback<T> cb : callbacks) {
            try {
                if (error == null) {
                    cb.onSuccess(result);
                } else {
                    cb.onError(error);
                }
            } catch (Exception e) {
                // логируем сбой в коллбеке, но не даём ему остановить остальные
                System.out.println("Сбой обратного вызова:" + e.getMessage());
            }
        }
    }
}