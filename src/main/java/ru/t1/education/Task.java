package ru.t1.education;

// Интерфейс задачи с результатом
public interface Task<T> {
    T execute() throws Exception;
}
