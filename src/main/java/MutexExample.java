import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class MutexExample {
    private int counter = 0;
    //явное взаимоисключение для блокировки
    final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();      // условная очередь, привязанная к взаимоисключение

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
                condition.await();   // освобождает взаимоисключение и ждёт сигнала
            }
            System.out.println("Threshold reached: " + counter);
        } finally {
            lock.unlock();
        }
    }

    public void notifyWhenThreshold(int target) {
        lock.lock();
        try {
            if (counter >= target) {
                //будит один поток, ждущий на этой condition
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