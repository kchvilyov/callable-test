public class MonitorExample {
    private int counter = 0;
    // наблюдатель этого объекта для блокировки
    private final Object lock = new Object();

    public void increment() {
        // использует наблюдатель для захвата взаимоисключения
        synchronized (lock) {
            //Безопасное приращение
            counter++;
        }
    }

    public void waitForThreshold(int target) throws InterruptedException {
        //использует наблюдатель и его условную очередь
        synchronized (lock) {
            while (counter < target) {
                //освобождает взаимоисключение и встаёт в очередь ожидания наблюдателя
                lock.wait();
            }
            System.out.println("Threshold reached: " + counter);
        }
    }

    //извещает ожидающие потоки о том, что условие (счётчик >= порог) выполнено, и те могут продолжить работу
    public void notifyWhenThreshold(int target) {
        synchronized (lock) {
            if (counter >= target) {
                //будит один поток из очереди ожидания этого наблюдателя
                lock.notify();
            }
        }
    }

    public int getCounter() {
        return counter;
    }
}