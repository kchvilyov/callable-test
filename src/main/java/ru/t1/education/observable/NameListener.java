package ru.t1.education.observable;

// Образец слушателя
class NameListener implements java.beans.PropertyChangeListener {
    @Override
    public void propertyChange(
            java.beans.PropertyChangeEvent evt) {
        // Это действие выполняется синхронно в вызывающей нити
        System.out.println(
                Thread.currentThread().getName() +
                        ": свойство '" + evt.getPropertyName() +
                        "' изменилось с '" + evt.getOldValue() +
                        "' на '" + evt.getNewValue() + "'"
        );
    }
}