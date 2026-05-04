package ru.t1.education;

// Коллбек
public interface TaskCallback<T> {
    void onSuccess(T result);
    void onError(Throwable error);
}
