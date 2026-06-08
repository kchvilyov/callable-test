package ru.t1.education.observable;

// Использование
public class SyncDemo {
    public static void main(String... args) {
        ObservableBean bean = new ObservableBean();
        bean.addPropertyChangeListener(new NameListener());

        System.out.println("Вызов setName из нити: " +
                Thread.currentThread().getName());
        bean.setName("новое"); // здесь же отрабатывает слушатель
        System.out.println("После завершения setName");
    }
}