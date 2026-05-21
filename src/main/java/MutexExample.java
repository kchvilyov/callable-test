import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class MutexExample {
    private int counter = 0;
    //явное взаимоисключение для блокировки
    final ReentrantLock lock = new ReentrantLock();
    //управляемое состояние, привязанное к взаимоисключению
    private final Condition condition = lock.newCondition();

    public void increment() {
        //захват взаимоисключения
        lock.lock();
        try {
            //Безопасное приращение
            counter++;
        } finally {
            //обязательное освобождение
            lock.unlock();
        }
    }

    public void waitForThreshold(int target) throws InterruptedException {
        lock.lock();
        try {
            while (counter < target) {
                //освобождает взаимоисключение и ждёт сигнала на этом состоянии
                condition.await();
            }
            System.out.println("Threshold reached: " + counter);
        } finally {
            lock.unlock();
        }
    }

    //извещает ожидающие потоки о том, что условие (счётчик >= порог) выполнено, и те могут продолжить работу
    public void notifyWhenThreshold(int target) {
        lock.lock();
        try {
            if (counter >= target) {
                //будит один поток, ждущий на состоянии взаимоисключения
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public int getCounter() {
        return counter;
    }

    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                counter++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
}