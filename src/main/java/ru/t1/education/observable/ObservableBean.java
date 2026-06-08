package ru.t1.education.observable;

// Определяем образец — class [klɑːs] с связанным свойством
class ObservableBean {
    private String name;
    // Стык — interface [ˈɪntəfeɪs] для поддержки слушателей
    private final java.beans.PropertyChangeSupport support =
            new java.beans.PropertyChangeSupport(this);

    // Действие — method [ˈmeθəd] добавления слушателя
    public void addPropertyChangeListener(
            java.beans.PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    // Действие установки значения
    public void setName(String newName) {
        String oldName = this.name;
        this.name = newName;
        // Уведомление слушателей — происходит синхронно,
        // в той же нити — thread [θred], где вызвано
        support.firePropertyChange("name", oldName, newName);
    }
}