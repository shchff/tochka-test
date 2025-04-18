import java.util.*;

public class Main {

    // Будем вести записи изменения числа людей в отеле за каждый день в отсортированной по ключам (датам) мапе,
    // после пройдёмся по мапе по порядку дат и будем отслеживать число людей. Если в какой-то день их число превысило
    // maxCapacity - возвращаем false. Если цикл завершился полностью, то true.

    // Временная сложность: O(nlogn), где n - количество дат (максимум в 2 раза больше, чем гостей).
    // Считается по первому циклу, размера не более 2n, где на каждой итерации добавляем изменение в дату в мапу (внутри
    // бинарное дерево, значит добавление элемента - O(logn).
    // Второй цикл - O(n).
    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {

        Map<String, Integer> loggingBook = new TreeMap<>();

        for (Map<String, String> guest : guests) {
            String timeIn = guest.get("check-in");
            String timeOut = guest.get("check-out");

            loggingBook.put(timeIn, loggingBook.getOrDefault(timeIn, 0) + 1);
            loggingBook.put(timeOut, loggingBook.getOrDefault(timeOut, 0) - 1);
        }

        int currCountGuests = 0;

        for (String key : loggingBook.keySet()) {
            currCountGuests += loggingBook.get(key);
            if (currCountGuests > maxCapacity) {
                return false;
            }
        }

        return true;
    }


    // Вспомогательный метод для парсинга JSON строки в Map
    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        // Удаляем фигурные скобки
        json = json.substring(1, json.length() - 1);


        // Разбиваем на пары ключ-значение
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }


    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);


        // Первая строка - вместимость гостиницы
        int maxCapacity = Integer.parseInt(scanner.nextLine());


        // Вторая строка - количество записей о гостях
        int n = Integer.parseInt(scanner.nextLine());


        List<Map<String, String>> guests = new ArrayList<>();


        // Читаем n строк, json-данные о посещении
        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();
            // Простой парсер JSON строки в Map
            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }


        // Вызов функции
        boolean result = checkCapacity(maxCapacity, guests);


        // Вывод результата
        System.out.println(result ? "True" : "False");


        scanner.close();
    }
}