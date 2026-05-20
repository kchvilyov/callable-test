import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 Создайте многопоточное приложение для поиска максимального значения в массиве.
 Разделите массив на части и выполните поиск максимума в каждой части в отдельных потоках.
 Объедините результаты для получения общего максимума.
 */
public class Main {
    static final int[] array = new int[];
    static int parts = 10;
    static final int partLength = array.length / parts;
    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<Thread>();
        for (int part = 0; part < parts; part++) {
            final int from = part * partLength;
            final int to = ((part + 1) == parts) ? array.length : (part + 1) * partLength;
            Thread thread = new MaxThread(from, to, array);
            thread.start();
            threads.add(thread);
        }
        for (Thread tread: threads) {
            tread.join();
        }
        int max = Integer.MIN_VALUE;
        for (Thread tread: threads) {
            max = Math.max(tread.getMax(), max);
        }
    }

    class MaxCalculate {
        public static int calculate(int[] array) throws InterruptedException {
            List<Thread> threads = new ArrayList<Thread>();
            for (int part = 0; part < parts; part++) {
                final int from = part * partLength;
                final int to = ((part + 1) == parts) ? array.length : (part + 1) * partLength;
                Thread thread = new MaxThread(from, to, array);
                thread.start();
                threads.add(thread);
            }
            for (Thread tread : threads) {
                tread.join();
            }
            int max = Integer.MIN_VALUE;
            for (Thread tread : threads) {
                max = Math.max(tread.getMax(), max);
            }
        }
    }

    class MaxThread extends Thread {
        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int max = Integer.MIN_VALUE;
        final int from;
        final int to;
        int[] array;
        MaxThread(int from, int to, int[] array) {
            this.from = from;
            this.to = to;
            this.array = array;
        }

        public void run() {
            for (int i = from; i < to; i++) {
                max = Math.max(array[i], max);
            }
        }
    };

}
