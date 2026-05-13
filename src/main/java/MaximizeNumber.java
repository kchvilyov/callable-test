public class MaximizeNumber {
    /**
     Задача 2.
     Даны два строковых представления чисел A и B. Нужно максимизировать A, заменив в нём любую
     цифру на цифру из B. Каждую цифру B можно использовать только один раз.
     */
    public static String maximize(String A, String B) {
        if (A == null) {
            return null;
        }
        if (B == null) {
            return A;
        }
        // Подсчёт цифр в B
        int[] count = new int[10];
        for (char c : B.toCharArray()) {
            //вычитание двух символов даёт целое число (расстояние между ними в таблице символов, обычно ASCII или Unicode)
            int distance = c - '0';
            //TODO Это поведение должно быть согласовано. Возможно, надо сделать прерывание с ошибкой если символ - не цифра?
            if (distance < 0 || distance > 9) {
                //Переставляем только цифры
                distance = 0;
            }
            count[distance]++;
        }
        char[] chars = A.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int current = chars[i] - '0';
            // Ищем максимальную цифру d > current, которая есть в наличии
            for (int d = 9; d > current; d--) {
                if (count[d] > 0) {
                    chars[i] = (char) (d + '0');
                    count[d]--;
                    break;
                }
            }
        }
        return new String(chars);
    }
}