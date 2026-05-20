import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 Создайте многопоточное приложение для поиска максимального значения в массиве.
 Разделите массив на части и выполните поиск максимума в каждой части в отдельных потоках.
 Объедините результаты для получения общего максимума.
 */
public class MaxCalculate {
    static final int[] array = new int[100_000]; // достаточно большой массив
    static final int parts = 10;                // количество потоков
    static final int partLength = array.length / parts;

    public static void main(String[] args) throws InterruptedException {
        List<MaxThread> threads = new ArrayList<>(); // для хранения результатов

        // Заполняем массив случайными числами
        Random rand = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = rand.nextInt(100_000);
        }


        for (int part = 0; part < parts; part++) {
            int from = part * partLength;
            int to = (part == parts - 1) ? array.length : from + partLength;

            MaxThread thread = new MaxThread(from, to, array);
            thread.start();
            threads.add(thread);
        }

        // Ожидаем завершения всех потоков
        for (Thread tread : threads) {
            tread.join();
        }

        // Собираем результат
        int max = Integer.MIN_VALUE;
        for (MaxThread tread : threads) {
            max = Math.max(tread.getMax(), max);
        }

        System.out.println("Наибольшее значение в массиве: " + max);
    }
}

/**
 * Поток, ищущий максимум на заданном отрезке массива.
 */
class MaxThread extends Thread {
    public int getMax() {
        return max;
    }

    private int max = Integer.MIN_VALUE;
    private final int from;
    private final int to;
    private final int[] array;

    /**
     * Построитель
     * @param from - от включительно
     * @param to - до исключительно
     * @param array - полный массив
     */
    public MaxThread(int from, int to, int[] array) {
        this.from = from;
        this.to = to;
        this.array = array;
    }

    @Override
    public void run() {
        System.out.println("Начинаю от:" + from + " до:" + to);
        for (int i = from; i < to; i++) {
            max = Math.max(array[i], max);
        }
        System.out.println("Закончил от:" + from + " до:" + to + " наибольшее:" + max);
    }
}
