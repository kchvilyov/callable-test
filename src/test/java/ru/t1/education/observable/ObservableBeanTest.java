package ru.t1.education.observable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservableBeanTest {
    @Test
    void shouldInvokeListenerSynchronously() {
        ObservableBean bean = new ObservableBean();
        // Слушатель фиксирует вызов в атомарной переменной
        final boolean[] called = {false};
        bean.addPropertyChangeListener(evt -> called[0] = true);

        // Вызов setName, который должен синхронно уведомить слушателя
        bean.setName("тест");

        // Сразу после завершения метода слушатель уже должен быть вызван
        assertTrue(called[0], "Слушатель должен быть вызван синхронно");
    }

    @Test
    void shouldInvokeListenerInSameThread() {
        ObservableBean bean = new ObservableBean();
        final String[] listenerThreadName = new String[1];
        bean.addPropertyChangeListener(evt ->
                listenerThreadName[0] = Thread.currentThread().getName()
        );

        String callingThreadName = Thread.currentThread().getName();
        bean.setName("значение");

        assertEquals(callingThreadName, listenerThreadName[0],
                "Слушатель должен выполняться в том же потоке");
    }

    @Test
    void shouldInvokeListenerSynchronouslyInSameThread() {
        ObservableBean bean = new ObservableBean();

        // Переменная для захвата имени нити внутри слушателя
        final String[] listenerThreadName = new String[1];
        final boolean[] called = new boolean[1];

        bean.addPropertyChangeListener(evt -> {
            listenerThreadName[0] = Thread.currentThread().getName();
            called[0] = true;
        });

        String callingThreadName = Thread.currentThread().getName();
        bean.setName("новое");

        // Проверка: слушатель сработал немедленно, не дожидаясь возврата управления
        assertTrue(called[0], "Слушатель должен быть вызван синхронно");
        assertEquals(callingThreadName, listenerThreadName[0],
                "Слушатель должен выполняться в той же нити, что и вызов setName");
    }
}