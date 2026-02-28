Разбираемые темы:
- Collections
- Exceptions
- Generics
- Сериализация
- I/O
- Копирование объектов

1. Коллекции (повторение)
- иерархия
- различия между основными типами коллекций
- производительность
2. Iterable/Iterator
3. Comparable/Comparator
4. Исключения
- иерархия
- checked/unchecked, errors
- try/catch/finally, try/catch с ресурсами
- исключения в разных блоках try/catch, supressed exceptions
- кастомные исключения
- обработка исключений в Spring
5. Дженерики
- Концепция
- Типизированные классы, методы
- ин-/ко-/контрвариантность, принцип PECS
6. Stream API
- функциональные интерфейсы, анонимные классы, лямбды
- стримы, операции
7. I/O
- базовые потоки ввода/вывода
- работа с файлами
- буферизированные/небуферизированные потоки
- IO/NIO
8. Сериализация
- концепция, требования
- serialVersionID
- проблемы и ограничения
9. Копирование объектов
- shallow/deep
- метод clone()
- паттерн Prototype в копировании
- другие способы сериализации


---

## 1) Иерархия Java Collections Framework

Важно: `Collection` и `Map` — это **две отдельные ветки**.

```text
Iterable
 └─ Collection
     ├─ List
     │   ├─ ArrayList
     │   ├─ LinkedList
     │   └─ CopyOnWriteArrayList (из java.util.concurrent)
     ├─ Set
     │   ├─ HashSet
     │   ├─ LinkedHashSet
     │   ├─ TreeSet
     │   └─ CopyOnWriteArraySet / ConcurrentSkipListSet (thread-safe варианты)
     └─ Queue / Deque
         ├─ PriorityQueue
         ├─ ArrayDeque
         └─ LinkedList

Map (не наследуется от Collection)
 ├─ HashMap
 ├─ LinkedHashMap
 ├─ TreeMap
 ├─ Hashtable (legacy)
 ├─ ConcurrentHashMap
 └─ ConcurrentSkipListMap
```

### Ключевая мысль
- **List** — упорядоченная последовательность, допускает дубликаты.
- **Set** — множество уникальных элементов.
- **Queue/Deque** — структуры для очередей/двусторонних очередей.
- **Map** — пары `ключ -> значение`, уникальность по ключу.

---

## 1.1) `Iterable` и `Iterator` в Java: как это работает

`Iterable<T>` — это «контейнер, по которому можно пройтись».

```java
public interface Iterable<T> {
    Iterator<T> iterator();
}
```

`Iterator<T>` — это объект-курсор для пошагового обхода.

```java
public interface Iterator<E> {
    boolean hasNext();
    E next();
    default void remove() { ... }
}
```

### Почему это важно
- `for-each` (`for (T x : collection)`) работает только для массивов и `Iterable`.
- `Collection` наследуется от `Iterable`, поэтому все стандартные коллекции можно обходить через `for-each`.
- `Iterator` даёт безопасное удаление текущего элемента во время обхода через `iterator.remove()`.

### Что делает `for-each` под капотом

Код:

```java
for (String name : names) {
    System.out.println(name);
}
```

Компилятор превращает примерно в:

```java
for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
    String name = it.next();
    System.out.println(name);
}
```

### Пример 1: ручной обход через `Iterator`

```java
import java.util.Iterator;
import java.util.List;

public class IteratorManualDemo {
    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Iterator<String> it = names.iterator();

        while (it.hasNext()) {
            String name = it.next();
            System.out.println(name);
        }
    }
}
```

### Пример 2: корректное удаление в процессе итерации

```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorRemoveDemo {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = nums.iterator();

        while (it.hasNext()) {
            Integer n = it.next();
            if (n % 2 == 0) {
                it.remove(); // безопасно удаляем текущий элемент
            }
        }

        System.out.println(nums); // [1, 3, 5]
    }
}
```

> Если удалять из коллекции напрямую (`nums.remove(n)`) во время итерации, обычно получите `ConcurrentModificationException`.

### Пример 3: собственная коллекция, реализующая `Iterable`

```java
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Range implements Iterable<Integer> {
    private final int from;
    private final int to;

    public Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = from;

            @Override
            public boolean hasNext() {
                return current <= to;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current++;
            }
        };
    }

    public static void main(String[] args) {
        for (int x : new Range(3, 7)) {
            System.out.print(x + " "); // 3 4 5 6 7
        }
    }
}
```

### `Iterable` vs `Iterator` в одном предложении
- `Iterable` отвечает на вопрос: «как получить итератор?»
- `Iterator` отвечает на вопрос: «как получить следующий элемент?»

---

## 2) Основные коллекции: различия и примеры

> Ниже примеры можно запускать как отдельные `main`-методы или интегрировать в ваши демо-классы.

### 2.1 `ArrayList`

Внутри — динамический массив.

**Плюсы**
- Быстрый доступ по индексу `get(i)`.
- Обычно хорошая cache locality.

**Минусы**
- Вставка/удаление в середине требует сдвига элементов.
- При росте может перевыделять массив.

```java
import java.util.*;

public class ArrayListDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        System.out.println(list.get(1)); // B: O(1)

        list.add(1, "X"); // сдвиг вправо: O(n)
        System.out.println(list); // [A, X, B, C]

        list.remove(2); // удаление со сдвигом: O(n)
        System.out.println(list); // [A, X, C]
    }
}
```

---

### 2.2 `LinkedList`

Двусвязный список (`prev/next` ссылки).

**Плюсы**
- Быстрые операции на концах (`addFirst/addLast/removeFirst/removeLast`) — O(1).
- Реализует и `List`, и `Deque`.

**Минусы**
- Доступ по индексу медленный (линейный обход).
- Памяти тратится больше (на узлы и ссылки).

```java
import java.util.*;

public class LinkedListDemo {
    public static void main(String[] args) {
        LinkedList<Integer> deque = new LinkedList<>();
        deque.addFirst(10);
        deque.addLast(20);
        deque.addLast(30);

        System.out.println(deque); // [10, 20, 30]
        System.out.println(deque.removeFirst()); // 10
        System.out.println(deque.get(1)); // 30, но get(i) ~ O(n)
    }
}
```

> В реальной практике для очередей чаще выбирают `ArrayDeque`, а не `LinkedList`.

---

### 2.3 `CopyOnWriteArrayList`

Потокобезопасная коллекция из `java.util.concurrent`.
При каждой модификации (`add/remove/set`) создаёт новую копию массива.

**Плюсы**
- Итерация без `ConcurrentModificationException`.
- Отлично для «много чтений, мало записей».

**Минусы**
- Запись дорогая (копирование всего массива).
- Не подходит для частых изменений.

```java
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListDemo {
    public static void main(String[] args) {
        CopyOnWriteArrayList<String> listeners = new CopyOnWriteArrayList<>();
        listeners.add("L1");
        listeners.add("L2");

        for (String listener : listeners) {
            System.out.println(listener);
            // безопасно: не ломает текущую итерацию
            listeners.add("new-" + listener);
        }

        System.out.println(listeners);
    }
}
```
---

### Что ещё важно упомянуть?
Вот что обычно хотят услышать на собеседовании 👇

🔹 1. Идеален для read-heavy сценариев

Подходит, когда:
- чтений много
- изменений мало

Примеры:
- список слушателей
- кэш конфигурации
- подписчики событий
- listeners

🔹 2. Итерация очень быстрая

Потому что:
- нет блокировок
- нет проверок на concurrent modification
- просто перебор массива

🔹 3. Модификации дорогие

Каждая модификация:

- O(n)
- создаёт новый массив
- создаёт нагрузку на GC

Если модификаций много — будет плохо.

🔹 4. Потокобезопасность

- Все модификации защищены ReentrantLock
- Чтения — без блокировок
- Память корректно видна за счёт volatile

🔹 5. Итератор не поддерживает remove()

Если вызвать:
```java
iterator.remove();
```
будет UnsupportedOperationException.

---

### 2.4 `HashSet`

Set на базе `HashMap`: хранит уникальные элементы, порядок не гарантирован.

**Требование:** корректные `equals()` и `hashCode()` у объектов.

```java
import java.util.*;

public class HashSetDemo {
    public static void main(String[] args) {
        Set<String> tags = new HashSet<>();
        tags.add("java");
        tags.add("collections");
        tags.add("java"); // дубликат не добавится

        System.out.println(tags); // порядок произвольный
        System.out.println(tags.contains("java")); // обычно O(1)
    }
}
```

---

### 2.5 `TreeSet`

Отсортированное множество (красно-чёрное дерево).

**Свойства**
- Всегда хранит элементы в отсортированном виде.
- Время операций — O(log n).

```java
import java.util.*;

public class TreeSetDemo {
    public static void main(String[] args) {
        TreeSet<Integer> scores = new TreeSet<>();
        scores.add(50);
        scores.add(10);
        scores.add(30);

        System.out.println(scores);         // [10, 30, 50]
        System.out.println(scores.ceiling(25)); // 30
        System.out.println(scores.floor(25));   // 10
    }
}
```

---

### 2.6 Потокобезопасный `Set` (варианты)

#### Вариант 1: `ConcurrentHashMap.newKeySet()`
Подходит в большинстве многопоточных задач.

```java
import java.util.Set;
import java.util.concurrent.*;

public class ConcurrentSetDemo {
    public static void main(String[] args) throws InterruptedException {
        Set<Integer> set = ConcurrentHashMap.newKeySet();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) {
            int v = i % 100; // специально дубликаты
            pool.submit(() -> set.add(v));
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println(set.size()); // ожидаемо 100
    }
}
```

#### Вариант 2: `CopyOnWriteArraySet`
Хорош для «редкие записи, частые чтения».

#### Вариант 3: `ConcurrentSkipListSet`
Потокобезопасный **отсортированный** set (обычно O(log n)).

---

### 2.7 `HashMap` (подробно)

`HashMap<K,V>` — базовая и самая важная map-структура.

#### Как работает внутри (упрощённо)
1. По `key.hashCode()` вычисляется hash.
2. Hash преобразуется в индекс корзины (bucket).
3. В корзине ищется нужный ключ:
   - сначала через сравнение hash,
   - затем через `equals()`.
4. При коллизиях элементы живут в одной корзине:
   - в новых JDK сначала список,
   - при большом числе коллизий может стать деревом (tree bin), чтобы ускорить доступ.

#### Важные свойства
- Средняя сложность `put/get/remove` — O(1), худший случай — O(n) (или O(log n) при tree bin).
- Разрешает `null` ключ (один) и `null` значения.
- Не потокобезопасен.

#### Критично: `equals/hashCode`
Если ключ — ваш класс, корректно переопределяйте оба метода.

```java
import java.util.*;

class UserId {
    private final long value;

    UserId(long value) { this.value = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return value == userId.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}

public class HashMapKeyDemo {
    public static void main(String[] args) {
        Map<UserId, String> map = new HashMap<>();
        map.put(new UserId(42), "Alice");

        System.out.println(map.get(new UserId(42))); // Alice
    }
}
```

#### Частые ошибки с `HashMap`
- Изменяемый ключ (меняете поле, участвующее в `hashCode/equals`, после вставки).
- Плохой `hashCode` (сильные коллизии).
- Использование из нескольких потоков без синхронизации.

#### Практические советы
- Если ожидаете много элементов, задайте начальную ёмкость (`new HashMap<>(expectedSize * 2)`) — меньше resize.
- Для счётчиков в многопотоке обычно лучше `ConcurrentHashMap + LongAdder`.

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class CounterDemo {
    public static void main(String[] args) {
        ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
        counters.computeIfAbsent("ok", k -> new LongAdder()).increment();
        counters.computeIfAbsent("ok", k -> new LongAdder()).increment();

        System.out.println(counters.get("ok").sum()); // 2
    }
}
```
---
### Внутреннее устройства HashMap

HashMap — один из базовых и в то же время самых хитро устроенных контейнеров в JDK. На поверхности простая структура «ключ–значение», но под капотом она сочетает массивы, списки и даже деревья, чтобы оставаться быстрой в разных сценариях нагрузки.

📦 Базовая структура

HashMap хранит данные в массиве бакетов (Node<K,V>[] table). Каждый бакет — это «корзина» для элементов, чей hashCode после хеширования и применения & (n-1) (где n — длина массива) указывает на конкретный индекс.

🔑 Хэш и распределение

1. Вызов hashCode() у ключа.
2. Дополнительное хеширование (spread), чтобы снизить коллизии из-за плохой реализации hashCode.
3. Индекс бакета = hash & (table.length - 1).

🌊 Коллизии

Если несколько ключей попали в один бакет:

— до Java 8 это всегда был связанный список (linked list),

— начиная с Java 8: при росте числа элементов в бакете больше 8 и достаточном размере таблицы он превращается в сбалансированное красно-чёрное дерево. Это резко ускоряет поиск в «плохих» случаях (с O(n) до O(log n)).

⚡️ Ресайзинг

Когда количество элементов превышает capacity * loadFactor (по умолчанию 0.75), создаётся новый массив в 2 раза больше, все элементы перехешируются и раскладываются по новым бакетам. Это дорогостоящая операция, но благодаря амортизации остаётся приемлемой.

📊 Производительность

— Поиск/вставка/удаление в среднем: O(1).

— В худшем случае (плохой hashCode + коллизии): O(log n) благодаря деревьям.

⚖️ Важные нюансы

— Ключи неупорядочены. Для упорядоченности есть LinkedHashMap.

— HashMap не потокобезопасен. Для многопоточной среды нужен ConcurrentHashMap или синхронизация.

— Хорошо реализованный hashCode и equals критичны, иначе получите «забитые» бакеты и деградацию.

🧮 loadFactor и capacity

— Capacity — размер массива бакетов. По умолчанию 16.

— LoadFactor — коэффициент заполнения. По умолчанию 0.75.

Почему именно 0.75? Это компромисс: выше → меньше памяти, но больше коллизий; ниже → быстрее доступ, но больше памяти уходит впустую. Capacity всегда степень двойки, чтобы можно было вычислять индекс через hash & (n-1) вместо затратного %.

🔄 Итераторы и fail-fast

Если во время обхода карта меняется (кроме iterator.remove()), бросается ConcurrentModificationException. Под капотом это работает через счётчик модификаций (modCount), который проверяется в каждом next().

🌳 Деревья в деталях

Коллизии превращаются в красно-чёрное дерево, если размер списка в бакете > 8 и общее количество бакетов ≥ 64. Обратно в список (untreeify) при падении количества элементов < 6. Это сделано, чтобы не тратить память и CPU на лишнюю балансировку при малых размерах.

🔗 Документация: OpenJDK — HashMap source (https://hg.openjdk.org/jdk/jdk/file/tip/src/java.base/share/classes/java/util/HashMap.java)

---
### 🤔 Почему строки так часто используют в виде ключей в HashMap?

Использование строк в качестве ключей в HashMap очень распространено, потому что строки обладают рядом свойств, которые идеально подходят для этой задачи. Вот основные причины:

🟠Строки неизменяемы
Что это значит: После создания строка не может быть изменена (все операции над строками создают новый объект).
Почему это важно: Ключ в HashMap должен быть неизменяемым, потому что, если ключ изменится после его добавления, это нарушит работу хэш-таблицы. Например, HashMap больше не сможет найти объект по этому ключу.
```java
HashMap<String, Integer> map = new HashMap<>();
String key = "hello";
map.put(key, 1);
// key остается "hello", ничего не ломается
```

### 🟠Эффективный `hashCode` и `equals`
Что это значит: Класс String в Java имеет качественно реализованные методы hashCode() и equals(), которые оптимизированы для работы с большими наборами данных.
Почему это важно: Эти методы определяют, куда ключ попадет в HashMap (по хэш-коду) и сравнивают ключи (по equals), чтобы избежать коллизий.
Особенность: Алгоритм hashCode() у строки быстро вычисляет хэш-код на основе её символов.
```java
String str1 = "hello";
String str2 = "hello";
System.out.println(str1.hashCode() == str2.hashCode()); // true
```

🟠Простота использования
Что это значит: Строки легко создавать, читать и понимать. Они часто используются для идентификаторов (например, имён, адресов, кодов).
Почему это важно: Программистам удобно использовать строки в качестве ключей, потому что их легко интерпретировать.

🟠Универсальность
Что это значит* Строки могут представлять самые разные данные — от имён и кодов до сложных текстовых идентификаторов.
Почему это важно: Почти любой объект или данные можно однозначно представить в виде строки, что делает её универсальным кандидатом на роль ключа.

🟠Широкая поддержка
Что это значит: Почти все приложения и API Java оперируют строками.
Почему это важно: Это упрощает интеграцию строк как ключей в сложных системах.

🚩Пример использования строки в качестве ключа
```java
import java.util.HashMap;

public class Main {
public static void main(String[] args) {
HashMap<String, Integer> ageMap = new HashMap<>();
ageMap.put("Alice", 30);
ageMap.put("Bob", 25);
ageMap.put("Charlie", 35);

        // Получаем значение по строковому ключу
        System.out.println("Возраст Боба: " + ageMap.get("Bob")); // 25
    }
}
```
Ставь 👍 (https://t.me/eo_test_task_bot) и забирай 📚  (https://t.me/eo_test_task_bot)Базу знаний (https://t.me/easy_java_ru/548)

---

### 2.8 `LinkedHashMap`

Сохраняет порядок (вставки или доступа).

- По умолчанию — порядок вставки.
- Можно включить `accessOrder=true` для LRU-подобного поведения.

```java
import java.util.*;

public class LinkedHashMapLruDemo {
    public static void main(String[] args) {
        Map<Integer, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 3;
            }
        };

        lru.put(1, "A");
        lru.put(2, "B");
        lru.put(3, "C");
        lru.get(1);      // делаем 1 "самым свежим"
        lru.put(4, "D"); // вытеснит 2

        System.out.println(lru); // {3=C, 1=A, 4=D}
    }
}
```
---
### Как устроен под капотом LinkedHashMap?

LinkedHashMap — это реализация интерфейса Map, которая сохраняет порядок добавления элементов. В отличие от обычного HashMap, где элементы могут быть расположены случайным образом, LinkedHashMap поддерживает последовательность вставки или порядок доступа. Это достигается благодаря использованию двусвязного списка, который связывает все элементы карты.

🔹 Структура LinkedHashMap

Основой LinkedHashMap является та же хэш-таблица, что и в HashMap, но с дополнительной структурой двусвязного списка для сохранения порядка элементов:

- Каждая запись (entry) в LinkedHashMap содержит ссылки на предыдущий и следующий элементы. Это позволяет поддерживать порядок добавления или порядок последнего доступа.
- Сначала выполняется хэширование ключей для быстрой вставки и поиска, как в HashMap, а уже потом запись связывается в список.

🔹 Производительность

- Вставка: Добавление новых элементов выполняется за O(1), поскольку элементы добавляются в конец двусвязного списка, а хэш-таблица используется для поиска свободной позиции.
- Удаление: Удаление элемента требует корректировки ссылок в двусвязном списке, что увеличивает накладные расходы, но также выполняется за O(1).
- Поиск: Операция поиска по ключу происходит с использованием хэш-таблицы и выполняется за O(1), как и в HashMap.
- Множественные коллизии: в худшем случае все операции будут выполняться с O(n), если допустить множественные коллизии.

🔹 Использование памяти

Каждая запись LinkedHashMap содержит дополнительные ссылки на предыдущий и следующий элементы, что увеличивает потребление памяти по сравнению с HashMap. Однако это оправдано, если важен порядок элементов.

🔹 Преимущества и недостатки

▪️ Преимущества:

- Сохранение порядка вставки: LinkedHashMap гарантирует, что элементы будут извлекаться в том порядке, в котором они были добавлены.
- Порядок доступа: Можно настроить LinkedHashMap на удаление самых старых элементов, что полезно для кэшей, где используется политика LRU (Least Recently Used).
- Предсказуемость итераций: В отличие от HashMap, где порядок элементов может изменяться, LinkedHashMap всегда сохраняет стабильный порядок.

▪️ Недостатки:

- Более высокое потребление памяти: Дополнительные ссылки на предыдущие и следующие элементы увеличивают память на каждую запись.
- Скорость: LinkedHashMap немного медленнее HashMap из-за поддержания порядка элементов.

✅ Java библиотека (https://t.me/javalib) #java

---

### 2.9 `ConcurrentHashMap`

Потокобезопасная map для конкурентного доступа.

**Особенности**
- Нет глобальной блокировки на всю map (как в старых подходах).
- Высокий throughput на чтении/записи под нагрузкой.
- Не допускает `null` ключи/значения.
- Итераторы weakly consistent (видят состояние «примерно во времени», без `ConcurrentModificationException`).

```java
import java.util.concurrent.*;

public class ConcurrentHashMapDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10_000; i++) {
            int key = i % 100;
            pool.submit(() -> map.merge(key, 1, Integer::sum));
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println(map.size());      // 100
        System.out.println(map.get(42));     // примерно 100
    }
}
```
---
### Как работает TreeMap и когда использовать?

Если есть необходимость не просто хранить пары ключ-значение, но получать их в отсортированном порядке и выполнять диапазонные запросы, то TreeMap может быть именно тем, что нужно.

▶️ Основные характеристики

TreeMap<K, V> реализует интерфейс NavigableMap и хранит записи в отсортированном порядке ключей(натуральный порядок, natural order) ключей или по Comparator, если его задать при создании

-  В основе TreeMap лежит красно-чёрное дерево (Red-Black Tree), которое является самобалансирующемся бинарным деревом поиска, обеспечивающее логарифмическую сложность операций

- Каждая запись хранится как узел Entry<K, V>, у которого есть ссылки на left, right и parent, плюс флаг цвета (красный / чёрный)

▶️ Основные операции и их сложность

- get, containsKey - O(log n) путём спуска по дереву до нужного ключа

- put, remove - O(log n) где после вставки или удаления выполняется балансировка

- Навигационные методы (firstKey, ceilingKey, subMap и др.) - O(log n) и затраты на итерации. Обходят дерево продвигаясь через в соседние узлы

- Итерация по entrySet() или keySet() - O(n), посещает все элементы в порядке возрастания ключей

▶️ Балансировка и инвариантность

При вставке и удалении TreeMap выполняет операции балансировки (повороты, перекраску узлов) так, чтобы дерево сохраняло свойства красно-чёрного дерева:

- Узлы имеют цвет, красный или чёрный
- Корень дерева окрашен черным цветом
- Если узел красный, его потомки не могут быть красными
- Любой путь от корня к “NULL-листу” содержит одинаковое число черных узлов
- Листовые узлы (NIL, “пустые”) считаются черными

Балансировка поддерживает высоту дерева порядка O(log n), что гарантирует логарифмическое поведение операций

✅ Когда использовать TreeMap?

- Нужно хранить записи в отсортированном порядке ключей без дополнительной сортировки

- Требуются диапазонные операции: взять все ключи между lowKey и highKey, найти ближайший “сверху” / “снизу” ключ (ceilingKey, floorKey), subMap().

- Нужно быстро получать минимальный / максимальный ключ / запись (firstKey(), lastKey()) или навигационные методы (higherKey, lowerKey, headMap, tailMap)

- Используете Comparator для пользовательской сортировки вместо естественного порядка ключей.

Примеры использования:
- Индексирование по дате, когда нужно быстро получить записи в диапазоне дат
- Подсчет статистики по диапазонам (например, “заказы между датами A и B”)
- Хранение таймера, расписания, событий, где важно естественное упорядочивание

▶️ Пример кода
```java
import java.util.*;

public class TreeMapExample {
   public static void main(String[] args) {
      TreeMap<String, Integer> map = new TreeMap<>();
      map.put("banana", 3);
      map.put("apple", 5);
      map.put("orange", 2);
// Ключи в байтовом порядке: apple, banana, orange
      System.out.println("Sorted keys: " + map.keySet());
// Навигация
      System.out.println("First key: " + map.firstKey());
      System.out.println("Ceiling key of \"ball\": " + map.ceilingKey("ball"));  // banana
// Диапазон
      SortedMap<String, Integer> sub = map.subMap("banana", true, "orange", false);
      System.out.println("Submap banana..orange (exclusive): " + sub);
// Обход
      for (Map.Entry<String, Integer> e : map.entrySet()) {
         System.out.println(e.getKey() + " -> " + e.getValue());
      }
   }
}
```
Вывод:
```
Sorted keys: [apple, banana, orange]
First key: apple
Ceiling key of "ball": banana
Submap banana..orange (exclusive): {banana=3}
```
⬇️ Какие структуры данных ты используешь чаще всего? Пиши свою историю в комментариях
👍 Оставляй комментарий и делись своим опытом разработки!


---

### 2.10 Что ещё важно знать

- `TreeMap` — отсортированная map (O(log n)).
- `EnumMap` / `EnumSet` — очень быстрые и компактные структуры для enum.
- `ArrayDeque` — почти всегда лучший выбор для стека/очереди вместо `Stack`/`LinkedList`.
- `Collections.synchronizedXxx(...)` — обёртки с общей блокировкой; часто медленнее и грубее, чем concurrent-коллекции.

---

## 3) Производительность: шпаргалка

> Big-O — ориентир. Реальная скорость зависит от JVM, данных, GC, CPU cache, распределения hash и конкуренции потоков.

### 3.1 Таблица операций

| Коллекция | get/contains | add | remove | Комментарий |
|---|---:|---:|---:|---|
| `ArrayList` | `get(i)` O(1) | в конец амортиз. O(1), в середину O(n) | O(n) | Лучший универсальный List для чтения по индексу |
| `LinkedList` | O(n) | на концах O(1), в середину O(n) | O(n), на концах O(1) | Хорош как Deque, слаб как random-access list |
| `CopyOnWriteArrayList` | O(1) чтение | O(n) запись | O(n) | Для read-mostly сценариев |
| `HashSet` | обычно O(1) | обычно O(1) | обычно O(1) | Нужны хорошие `hashCode/equals` |
| `TreeSet` | O(log n) | O(log n) | O(log n) | Отсортированные данные |
| `HashMap` | обычно O(1) | обычно O(1) | обычно O(1) | Может деградировать при коллизиях |
| `LinkedHashMap` | обычно O(1) | обычно O(1) | обычно O(1) | + предсказуемый порядок |
| `ConcurrentHashMap` | обычно O(1) | обычно O(1) | обычно O(1) | Для конкурентного доступа |

### 3.2 Мини-бенчмарк (упрощённый)

Для серьёзных измерений используйте **JMH**, но для ощущения разницы можно так:

```java
import java.util.*;

public class ListPerfSketch {
    public static void main(String[] args) {
        int n = 200_000;

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) arrayList.add(i);
        long t2 = System.nanoTime();

        for (int i = 0; i < n; i++) linkedList.add(i);
        long t3 = System.nanoTime();

        // чтение по индексу
        long sum = 0;
        for (int i = 0; i < n; i++) sum += arrayList.get(i);
        long t4 = System.nanoTime();

        for (int i = 0; i < n; i++) sum += linkedList.get(i); // существенно медленнее
        long t5 = System.nanoTime();

        System.out.printf("ArrayList add:  %.2f ms%n", (t2 - t1) / 1_000_000.0);
        System.out.printf("LinkedList add: %.2f ms%n", (t3 - t2) / 1_000_000.0);
        System.out.printf("ArrayList get:  %.2f ms%n", (t4 - t3) / 1_000_000.0);
        System.out.printf("LinkedList get: %.2f ms%n", (t5 - t4) / 1_000_000.0);
        System.out.println(sum);
    }
}
```

---

## 4) Практический выбор коллекции (короткий алгоритм)

1. Нужны пары `key -> value`? → `Map`.
   - Обычный случай: `HashMap`.
   - Нужен порядок: `LinkedHashMap`.
   - Нужна сортировка ключей: `TreeMap`.
   - Многопоточка: `ConcurrentHashMap`.

2. Нужна последовательность? → `List`.
   - По умолчанию: `ArrayList`.
   - Read-mostly + потоки: `CopyOnWriteArrayList`.
   - Дек/очередь: лучше `ArrayDeque`.

3. Нужна уникальность? → `Set`.
   - По умолчанию: `HashSet`.
   - Сортировка: `TreeSet`.
   - Многопоточка: `ConcurrentHashMap.newKeySet()` / `ConcurrentSkipListSet`.

---

## 5) Частые вопросы с собеседований

### Почему `HashMap` не гарантирует порядок?
Потому что хранит элементы по корзинам, зависящим от hash и внутренней структуры, а не по вставке.

### Почему `ConcurrentHashMap` запрещает `null`?
Чтобы не было неоднозначности в конкурентной среде: `null` как «нет значения» vs «значение есть, но null».

### Почему `LinkedList` часто проигрывает `ArrayList`, хотя у него O(1) вставка?
Из-за затрат на переходы по ссылкам, плохой локальности кэша и того, что вставка в середину всё равно требует поиск позиции O(n).

---

Если хочешь, следующим шагом могу добавить к этому гайду:
- отдельный блок про **fail-fast / fail-safe** итераторы;
- блок про **immutable коллекции** (`List.of`, `Map.of`, `Collectors.toUnmodifiableList`);
- готовые «шаблоны выбора» для типовых задач (кэш, дедупликация, leaderboard, event listeners).

---

## 6) `Comparable` и `Comparator` в Java: в чём разница и как применять

Оба интерфейса отвечают за сравнение объектов, но используются в разных сценариях:

- `Comparable<T>` — **естественный порядок** внутри самого класса.
- `Comparator<T>` — **внешняя стратегия сортировки**, когда порядков может быть несколько.

### 6.1 `Comparable`: «класс умеет сравнивать сам себя»

```java
import java.util.*;

class Student implements Comparable<Student> {
    private final String name;
    private final int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Student other) {
        // естественный порядок: по score (по возрастанию)
        return Integer.compare(this.score, other.score);
    }

    @Override
    public String toString() {
        return name + "(" + score + ")";
    }
}

public class ComparableDemo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
                new Student("Mila", 92),
                new Student("Artem", 81),
                new Student("Ira", 92)
        ));

        Collections.sort(students); // использует compareTo
        System.out.println(students); // [Artem(81), Mila(92), Ira(92)]
    }
}
```

Когда подходит `Comparable`:
- у сущности есть один «дефолтный» порядок (например, `LocalDate`, `String`, `Integer`);
- этот порядок логично сделать частью модели.

### 6.2 `Comparator`: «сортируем по-разному в зависимости от задачи»

```java
import java.util.*;

class Student {
    private final String name;
    private final int score;
    private final int age;

    Student(String name, int score, int age) {
        this.name = name;
        this.score = score;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return name + "(" + score + ", " + age + ")";
    }
}

public class ComparatorDemo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
                new Student("Mila", 92, 20),
                new Student("Artem", 81, 19),
                new Student("Ira", 92, 18)
        ));

        // 1) По score по убыванию
        students.sort(Comparator.comparingInt(Student::getScore).reversed());
        System.out.println("By score desc: " + students);

        // 2) По name по возрастанию
        students.sort(Comparator.comparing(Student::getName));
        System.out.println("By name asc: " + students);

        // 3) Сложный порядок: score desc, затем age asc
        students.sort(
                Comparator.comparingInt(Student::getScore).reversed()
                        .thenComparingInt(Student::getAge)
        );
        System.out.println("By score desc, age asc: " + students);
    }
}
```

Когда подходит `Comparator`:
- нужно несколько вариантов сортировки;
- не хотите менять исходный класс;
- сортируете внешний тип (например, из библиотеки).

### 6.3 Важные правила для корректного сравнения

1. **Антисимметрия**: знак `a.compareTo(b)` должен быть противоположен знаку `b.compareTo(a)`.
2. **Транзитивность**: если `a > b` и `b > c`, то `a > c`.
3. **Согласованность с equals** (особенно важно для `TreeSet/TreeMap`):
   - если `compare(...) == 0`, объекты считаются одинаковыми с точки зрения сортированной структуры.

Пример проблемы:
- если в `TreeSet` сравнивать `Student` только по `score`, то два разных студента с одинаковым `score` будут считаться «дубликатом», и один из них не добавится.

### 6.4 Мини-шпаргалка

- Нужен **один естественный порядок** → `Comparable`.
- Нужны **разные варианты сортировки** → `Comparator`.
- Часто используют оба подхода: `Comparable` как базовый порядок + `Comparator` под конкретные кейсы.

---

## 4) Исключения в Java: иерархия, обработка и практические примеры

Исключения в Java — это механизм для обработки аномальных ситуаций во время выполнения программы.

Базовая идея:
- «Нормальный путь» кода остаётся читаемым.
- «Ошибочный путь» можно централизованно обработать.

### 4.1 Иерархия исключений

Корень иерархии — `Throwable`.

```text
Throwable
 ├─ Error
 │   ├─ OutOfMemoryError
 │   ├─ StackOverflowError
 │   └─ ...
 └─ Exception
     ├─ RuntimeException
     │   ├─ NullPointerException
     │   ├─ IllegalArgumentException
     │   ├─ IllegalStateException
     │   ├─ IndexOutOfBoundsException
     │   └─ ...
     └─ (checked exceptions)
         ├─ IOException
         ├─ SQLException
         ├─ ParseException
         └─ ...
```

Ключевой вывод:
- `Exception` — то, что в большинстве случаев ожидаемо обрабатывается приложением.
- `Error` — серьёзные проблемы JVM/окружения, обычно не предназначены для «бизнес-обработки».

---

### 4.2 Checked / Unchecked / Error

#### Checked exceptions
Это исключения, которые **обязаны быть либо пойманы (`catch`), либо объявлены (`throws`)**.

```java
import java.io.IOException;

public class CheckedExample {
    static String readConfig() throws IOException {
        throw new IOException("config file not found");
    }

    public static void main(String[] args) {
        try {
            String config = readConfig();
            System.out.println(config);
        } catch (IOException e) {
            System.out.println("Не удалось прочитать конфиг: " + e.getMessage());
        }
    }
}
```

Когда полезны: для recoverable-сценариев (например, I/O, сеть, интеграции).

#### Unchecked exceptions (`RuntimeException`)
Компилятор не заставляет их обрабатывать.

```java
public class UncheckedExample {
    static int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("b must not be 0");
        }
        return a / b;
    }

    public static void main(String[] args) {
        System.out.println(divide(10, 2));
        // System.out.println(divide(10, 0)); // IllegalArgumentException
    }
}
```

Когда полезны: ошибки валидации, ошибки контракта API, неверное состояние объекта.

#### `Error`
`Error` обычно не ловят в прикладной логике.

```java
public class ErrorNote {
    public static void main(String[] args) {
        // Не делайте так в прод-коде:
        // try { ... } catch (OutOfMemoryError e) { ... }
        // Обычно правильнее предотвратить причину и мониторить JVM.
    }
}
```

---

### 4.3 `try/catch/finally` и `try-with-resources`

#### Базовый `try/catch/finally`

```java
public class TryCatchFinallyExample {
    public static void main(String[] args) {
        try {
            System.out.println("Открываем операцию");
            int x = 10 / 0;
            System.out.println(x);
        } catch (ArithmeticException e) {
            System.out.println("Поймали арифметическую ошибку: " + e.getMessage());
        } finally {
            System.out.println("finally выполнится почти всегда (очистка ресурсов)");
        }
    }
}
```

`finally` обычно используют для освобождения ресурсов, если не применили `try-with-resources`.

#### `try-with-resources`
Рекомендуемый способ работы с ресурсами (`Closeable`/`AutoCloseable`).

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TryWithResourcesExample {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("app.properties"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }
    }
}
```

Плюс: ресурс закрывается автоматически, даже если в блоке `try` возникло исключение.

---

### 4.4 Исключения в разных блоках и suppressed exceptions

Когда ошибка происходит и в `try`, и при закрытии ресурса, при `try-with-resources` исключение закрытия становится **suppressed**.

```java
class BrokenResource implements AutoCloseable {
    @Override
    public void close() {
        throw new RuntimeException("Ошибка в close()");
    }

    void doWork() {
        throw new RuntimeException("Ошибка в doWork()");
    }
}

public class SuppressedExample {
    public static void main(String[] args) {
        try (BrokenResource r = new BrokenResource()) {
            r.doWork();
        } catch (Exception e) {
            System.out.println("Main exception: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("Suppressed: " + s.getMessage());
            }
        }
    }
}
```

Ожидаемая логика:
- Основное исключение: из `doWork()`.
- Suppressed: из `close()`.

Это важно для диагностики: suppressed исключения часто объясняют дополнительные проблемы при очистке.

---

### 4.5 Кастомные исключения

Хорошая практика — вводить доменные исключения с понятным смыслом.

```java
public class InsufficientFundsException extends RuntimeException {
    private final String accountId;

    public InsufficientFundsException(String accountId, String message) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
```

Использование:

```java
public class PaymentService {
    public void withdraw(String accountId, int balance, int amount) {
        if (amount > balance) {
            throw new InsufficientFundsException(
                    accountId,
                    "Недостаточно средств: balance=" + balance + ", amount=" + amount
            );
        }
        // списание
    }
}
```

Советы:
- Называйте исключения по бизнес-смыслу (`UserNotFoundException`, `OrderAlreadyPaidException`).
- Не теряйте причину: используйте конструкторы с `cause`, если заворачиваете чужое исключение.

---

### 4.6 Обработка исключений в Spring (REST API)

Обычно в Spring используют глобальный обработчик через `@RestControllerAdvice`.

#### 1) Доменное исключение

```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Пользователь с id=" + id + " не найден");
    }
}
```

#### 2) Сервис

```java
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public String findUserName(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id должен быть положительным");
        }
        if (id == 404L) {
            throw new UserNotFoundException(id);
        }
        return "Alice";
    }
}
```

#### 3) Контроллер

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService BUserService;

    public UserController(UserService BUserService) {
        this.BUserService = BUserService;
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id) {
        return BUserService.findUserName(id);
    }
}
```

#### 4) Глобальный exception handler

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "USER_NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "INTERNAL_ERROR",
                "message", "Внутренняя ошибка сервера"
        ));
    }
}
```

Что это даёт:
- единый формат ошибок;
- понятные HTTP-статусы;
- меньше `try/catch` в контроллерах.

---

### 4.7 Практические рекомендации

- Ловите максимально узкие типы исключений, а не сразу `Exception`.
- Не «глотайте» исключение (`catch` без логирования/реакции).
- Добавляйте контекст в сообщение (id сущности, входные параметры).
- Для API возвращайте предсказуемую структуру ошибки.
- Для инфраструктурных ошибок учитывайте retry/backoff, но с ограничениями.

Если хочешь, следующим шагом могу добавить мини-практику: 5 задач по исключениям (с решениями) — от базовых до уровня Spring API.

---

## 5) Дженерики (Generics) в Java: концепция, типизированные классы/методы и PECS

### 5.1 Концепция Generics

`Generics` позволяют параметризовать типы и писать переиспользуемый, но при этом типобезопасный код.

Без дженериков (до Java 5) приходилось работать через `Object` и делать приведения вручную:

```java
List items = new ArrayList();
items.add("hello");
String s = (String) items.get(0); // ручной cast
```

С дженериками:

```java
List<String> items = new ArrayList<>();
items.add("hello");
String s = items.get(0); // cast не нужен
```

Что это даёт:
- проверки типов на этапе компиляции;
- меньше `ClassCastException` в рантайме;
- более читаемые API (`List<User>` понятнее, чем просто `List`).

> Важно: в Java дженерики реализованы через **type erasure** — информация о параметрах типа в рантайме стирается.

---

### 5.2 Типизированные (generic) классы

Простой обобщённый контейнер:

```java
public class Box<T> {
    private T value;

    public Box(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
```

Использование:

```java
Box<String> stringBox = new Box<>("Java");
String text = stringBox.getValue();

Box<Integer> intBox = new Box<>(42);
Integer number = intBox.getValue();
```

Можно использовать несколько параметров типа:

```java
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
}
```

---

### 5.3 Типизированные (generic) методы

Generic-метод объявляет свои параметры типа перед возвращаемым типом:

```java
public class GenericMethods {

    public static <T> void printArray(T[] arr) {
        for (T el : arr) {
            System.out.print(el + " ");
        }
        System.out.println();
    }

    public static <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }
}
```

Вызов:

```java
GenericMethods.printArray(new String[]{"A", "B"});
GenericMethods.printArray(new Integer[]{1, 2, 3});

String first = GenericMethods.firstOrNull(List.of("x", "y"));
Integer n = GenericMethods.firstOrNull(List.of(10, 20));
```

Пример с ограничением (`bounded type parameter`):

```java
public static <T extends Number> double sum(List<T> nums) {
    double total = 0;
    for (T n : nums) {
        total += n.doubleValue();
    }
    return total;
}
```

`T extends Number` означает: метод принимает только типы-наследники `Number` (`Integer`, `Double`, `Long` и т.д.).

---

### 5.4 Инвариантность в Java

В Java generic-типы **инвариантны**.

Это значит, что даже если `Integer` — наследник `Number`,
`List<Integer>` **не является** подтипом `List<Number>`.

```java
List<Integer> ints = List.of(1, 2, 3);
// List<Number> nums = ints; // compile error
```

Почему так: иначе можно было бы добавить `Double` в список `Integer` и сломать типобезопасность.

---

### 5.5 Ковариантность (`? extends T`)

Ковариантность в Java достигается через wildcard `? extends T`.

```java
public static double sumNumbers(List<? extends Number> numbers) {
    double sum = 0;
    for (Number n : numbers) {
        sum += n.doubleValue();
    }
    return sum;
}
```

Теперь можно передавать `List<Integer>`, `List<Double>`, `List<Long>`.

```java
double a = sumNumbers(List.of(1, 2, 3));
double b = sumNumbers(List.of(1.5, 2.5));
```

Ограничение: в `List<? extends Number>` безопасно **читать** как `Number`, но нельзя безопасно добавлять (кроме `null`).

---

### 5.6 Контрвариантность (`? super T`)

Контрвариантность задаётся через wildcard `? super T`.

```java
public static void addDefaults(List<? super Integer> target) {
    target.add(10);
    target.add(20);
}
```

Метод принимает `List<Integer>`, `List<Number>`, `List<Object>`.

```java
List<Number> numbers = new ArrayList<>();
addDefaults(numbers); // ок
```

Здесь можно безопасно **записывать** `Integer`, но при чтении получаем только `Object`.

---

### 5.7 PECS: Producer Extends, Consumer Super

Правило PECS:

- **Producer Extends** (`? extends T`) — когда источник **производит** значения типа `T` (мы читаем).
- **Consumer Super** (`? super T`) — когда приёмник **потребляет** значения типа `T` (мы пишем).

Классический пример копирования:

```java
public static <T> void copy(List<? extends T> src, List<? super T> dst) {
    for (T item : src) {
        dst.add(item);
    }
}
```

Использование:

```java
List<Integer> src = List.of(1, 2, 3);
List<Number> dst = new ArrayList<>();
copy(src, dst); // ок: Integer -> Number
```

`src` — producer (читаем, поэтому `extends`), `dst` — consumer (пишем, поэтому `super`).

---

### 5.8 Небольшая шпаргалка

- `List<T>` — нужен конкретный тип без wildcard, и читаем/пишем именно `T`.
- `List<? extends T>` — читаем как `T`, почти не пишем.
- `List<? super T>` — пишем `T`, читаем как `Object`.

Если сомневаешься:
1. Ты в основном **читаешь** из коллекции? → `extends`.
2. Ты в основном **пишешь** в коллекцию? → `super`.
3. И читаешь, и пишешь строго один и тот же тип? → `T` без wildcard.

Если хочешь, следующим шагом могу добавить отдельный блок с частыми ловушками по generics: `raw types`, bridge methods, ограничения type erasure и почему нельзя `new T()`.

---
## 🤔 Для чего нужно стирание типов?

Это механизм, используемый в Java для обеспечения обратной совместимости между старым кодом, написанным до введения обобщений (generics) в Java 5, и новым кодом, который их использует. Стирание типов позволяет компилировать обобщенный код в байт-код, совместимый с JVM, который не поддерживает обобщения.

🚩Основные цели стирания типов

🟠Обратная совместимость
Позволяет использовать старый код, написанный до введения обобщений, вместе с новым обобщенным кодом без изменений в существующем коде.

🟠Сокращение избыточности
Обеспечивает единообразие работы с различными типами, минимизируя избыточность в коде и устраняя необходимость дублирования кода для разных типов.

🚩Как работает стирание типов

При компиляции обобщенного кода компилятор Java удаляет информацию о типах (стирает типы) и заменяет их на их необобщенные версии или верхние границы (bounds). В результате обобщенный код компилируется в байт-код, который может выполняться на обычной JVM.

Обобщенный класс
```java
public class Box<T> {
private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
```
После стирания типов компилированный код будет выглядеть примерно так
```java
public class Box {
private Object value;

    public void set(Object value) {
        this.value = value;
    }

    public Object get() {
        return value;
    }
}
```
🚩Ограничения и последствия стирания типов

🟠Потеря информации о типе во время выполнения
После стирания типов информация о типах удаляется, и во время выполнения типовые параметры становятся объектами Object.

🟠Невозможность использования примитивных типов
Обобщения работают только с ссылочными типами, так как примитивные типы не могут быть использованы в качестве типовых параметров.

🟠Рефлексия и обобщения
Невозможно получить информацию о типовых параметрах через рефлексию, так как она теряется во время компиляции.

🚩Пример ограничения

Невозможность создания массивов обобщенных типов
```java
public class Box<T> {
private T value;

    public T[] createArray(int size) {
        return new T[size]; // Ошибка компиляции
    }
}
```
Обходное решение с использованием рефлексии
```java
public class Box<T> {
private T value;
private Class<T> type;

    public Box(Class<T> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public T[] createArray(int size) {
        return (T[]) java.lang.reflect.Array.newInstance(type, size);
    }
}
```
Ставь 👍 (https://t.me/eo_test_task_bot) и забирай 📚  (https://t.me/eo_test_task_bot)Базу знаний (https://t.me/easy_java_ru/548)

---

## 6) Stream API в Java: функциональные интерфейсы, анонимные классы, лямбды и операции

`Stream API` — это декларативный способ обработки данных: вы описываете **что сделать с набором данных**, а не **как вручную пройти цикл**.

Типичный пайплайн выглядит так:

```java
List<String> result = names.stream()         // источник
        .filter(s -> s.length() >= 4)        // промежуточная операция
        .map(String::toUpperCase)            // промежуточная операция
        .sorted()                            // промежуточная операция
        .toList();                           // терминальная операция
```

Ключевые идеи:
- Stream не хранит данные сам по себе — он работает поверх источника (`Collection`, массив, `Files.lines(...)`, генераторы и т.д.).
- Промежуточные операции (`map`, `filter`, `sorted`, `distinct`, `limit`...) ленивые.
- Вычисление стартует только на терминальной операции (`toList`, `collect`, `forEach`, `count`, `reduce`, `findFirst`...).
- Один stream можно потребить только один раз.

### 6.1 Функциональные интерфейсы: что это и зачем

**Функциональный интерфейс** — интерфейс с ровно одним абстрактным методом (`SAM`: single abstract method).

```java
@FunctionalInterface
public interface Calculator {
    int apply(int a, int b);
}
```

`@FunctionalInterface` необязателен, но полезен: компилятор проверит, что второй абстрактный метод случайно не добавлен.

Стандартные функциональные интерфейсы из `java.util.function`:
- `Predicate<T>`: `boolean test(T t)` — проверка условия.
- `Function<T, R>`: `R apply(T t)` — преобразование.
- `Consumer<T>`: `void accept(T t)` — «потребление» значения (обычно side effects).
- `Supplier<T>`: `T get()` — поставщик значения.
- `UnaryOperator<T>` / `BinaryOperator<T>` — частные случаи `Function`.
---
Java даёт много вариантов по части синтаксиса, но `функциональные интерфейсы` — одна из самых аккуратных и приятных фишек языка ☕️

Сегодня разберём 4 штуки, которые встречаются чаще всего. Если поймёшь их, писать код станет современнее, местами чище

Идея простая 👇

Функциональный интерфейс — это интерфейс с одним абстрактным методом. Благодаря этому его можно реализовать через лямбды.

В Java их много, но вот четыре, которые ты будешь видеть постоянно:

👉 Consumer — делает что-то

Consumer принимает значение и ничего не возвращает.

Отлично подходит для побочных эффектов: логирование, вывод в консоль, сохранение, отправка и так далее.
```java
Consumer<String> consumer = str -> System.out.println(str);
consumer.accept("Hola");
```
Проще говоря:
"получи это и сделай с этим что-то".

👉 Supplier — дай что-то

Supplier ничего не принимает и возвращает значение.

Часто используется для получения конфигураций, генерации ID, ленивого создания объектов и прочего.
```java
Supplier<Double> supplier = () -> Math.random();
supplier.get();
```
То есть:
"выдай нужную штуку, когда я попрошу".

👉 Function<T, R> — преобразуй что-то

Принимает значение типа T и возвращает значение типа R.

На практике эта штука — самая распространённая.
```java
Function<Integer, String> function = number -> "N° " + number;
function.apply(5);
```
По смыслу:
"получаю T, возвращаю R".

👉Predicate — реши что-то (true/false)

Принимает значение и возвращает boolean.
Часто нужен для фильтрации списков, простых проверок, валидаций, правил.
```java
Predicate<String> predicate = s -> s.length() > 5;
predicate.test("Java");
```
То есть:
"подходит или не подходит под условие".

Важно:

Эти интерфейсы существуют не ради компактного кода.

Они нужны, чтобы ты думал через операции, а не через классы.

Они идеально заходят в Streams, в коллбеки, в валидации, в композицию логики — везде, где есть простая операция, для которой не нужна отдельная сущность.

Это не замена всему на свете. Речь не про то, чтобы переписать всю систему в функциональном стиле.

Но они реально помогают во множестве сценариев.

Если научишься читать Function, Consumer, Supplier и Predicate, то спокойно разберёшь и напишешь современный Java-код без лишних страданий. И это уже хороший шаг вперёд. 😁

---

### 6.2 Анонимный класс vs лямбда

До Java 8 поведение часто передавали через анонимные классы:

```java
import java.util.Comparator;

public class AnonymousClassDemo {
    public static void main(String[] args) {
        Comparator<String> byLength = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        };

        System.out.println(byLength.compare("cat", "elephant")); // < 0
    }
}
```

С Java 8 тот же код компактнее лямбдой:

```java
import java.util.Comparator;

public class LambdaDemo {
    public static void main(String[] args) {
        Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());
        System.out.println(byLength.compare("cat", "elephant"));
    }
}
```

И ещё компактнее через метод-референс + фабрику:

```java
Comparator<String> byLength = Comparator.comparingInt(String::length);
```

### 6.3 Один и тот же сценарий тремя способами

Задача: отфильтровать чётные числа и возвести в квадрат.

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompareStylesDemo {
    public static void main(String[] args) {
        List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6);

        // 1) Императивный стиль
        List<Integer> imperative = new ArrayList<>();
        for (Integer n : source) {
            if (n % 2 == 0) {
                imperative.add(n * n);
            }
        }

        // 2) Через Stream API + лямбды
        List<Integer> streamLambda = source.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .toList();

        System.out.println(imperative);   // [4, 16, 36]
        System.out.println(streamLambda); // [4, 16, 36]
    }
}
```

### 6.4 Основные операции Stream API

#### `filter` — отбор

```java
List<String> adults = users.stream()
        .filter(u -> u.age() >= 18)
        .map(User::name)
        .toList();
```

#### `map` — преобразование 1 к 1

```java
List<Integer> lengths = List.of("java", "stream", "api").stream()
        .map(String::length)
        .toList(); // [4, 6, 3]
```

#### `flatMap` — «расплющивание» вложенных структур

```java
List<List<String>> lines = List.of(
        List.of("a", "b"),
        List.of("c"),
        List.of("d", "e")
);

List<String> all = lines.stream()
        .flatMap(List::stream)
        .toList(); // [a, b, c, d, e]
```

#### `distinct`, `sorted`, `limit`, `skip`

```java
List<Integer> top3 = List.of(7, 1, 3, 3, 9, 2, 9, 10).stream()
        .distinct()                  // убрали дубликаты
        .sorted()                    // [1, 2, 3, 7, 9, 10]
        .skip(1)                     // [2, 3, 7, 9, 10]
        .limit(3)                    // [2, 3, 7]
        .toList();
```

#### Терминальные операции: `forEach`, `count`, `findFirst`, `reduce`, `collect`

```java
long cnt = List.of("a", "bb", "ccc").stream()
        .filter(s -> s.length() >= 2)
        .count(); // 2

int sum = List.of(1, 2, 3, 4).stream()
        .reduce(0, Integer::sum); // 10
```

### 6.5 `collect`: группировка и агрегация

```java
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record Employee(String name, String department, int salary) {}

public class CollectorsDemo {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Ann", "IT", 2000),
                new Employee("Bob", "IT", 2500),
                new Employee("Kate", "HR", 1800)
        );

        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));

        Map<String, Integer> salaryByName = employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));

        double avgSalary = employees.stream()
                .collect(Collectors.averagingInt(Employee::salary));

        System.out.println(byDepartment.keySet());
        System.out.println(salaryByName);
        System.out.println(avgSalary);
    }
}
```

### 6.6 Важные практические моменты

- Не злоупотребляйте stream в очень простой логике, где обычный `for` читается лучше.
- Избегайте side effects в `map/filter` (например, изменения внешнего списка).
- `parallelStream()` применяйте только после измерений (профилирование/бенчмарк).
- Для nullable-значений удобно использовать `Optional` и `Stream.ofNullable(...)` (Java 9+).

### 6.7 Мини-шпаргалка

- **Функциональный интерфейс** = 1 абстрактный метод.
- **Анонимный класс** = старый способ передавать поведение.
- **Лямбда** = компактная реализация функционального интерфейса.
- **Stream** = цепочка преобразований данных: source -> intermediate ops -> terminal op.

---

## Java I/O на практике: базовые потоки, файлы, буферизация, IO vs NIO

Ниже — короткий конспект с рабочими примерами, который можно запускать как `main`-классы.

### 1) Базовые потоки ввода/вывода

В классическом `java.io` есть 2 базовые иерархии:
- `InputStream/OutputStream` — работа с **байтами**.
- `Reader/Writer` — работа с **символами** (текст, кодировки).

```java
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BasicStreamDemo {
    public static void main(String[] args) throws Exception {
        // Байт-уровень: в памяти
        byte[] src = "Hello IO".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(src);
             OutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            String result = out.toString();
            System.out.println(result); // Hello IO
        }

        // Символьный уровень: Reader/Writer
        try (Reader reader = new StringReader("Привет, Reader/Writer");
             StringWriter writer = new StringWriter()) {

            char[] cbuf = new char[8];
            int read;
            while ((read = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, read);
            }

            System.out.println(writer.toString());
        }
    }
}
```

---

### 2) Работа с файлами (`java.io.File` + `java.nio.file.Files`)

Старый API: `File` (метаданные, путь, базовые операции).
Современный API: `Path/Files` (удобнее и богаче по функционалу).

```java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class FileIoDemo {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("tmp/io-demo");
        Path file = dir.resolve("notes.txt");

        Files.createDirectories(dir);

        // Запись строки в файл
        Files.writeString(
                file,
                "line-1\nline-2\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        // Дозапись
        Files.writeString(file, "line-3\n", StandardOpenOption.APPEND);

        // Чтение
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        lines.forEach(System.out::println);

        // Копирование
        Path copy = dir.resolve("notes-copy.txt");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
    }
}
```

---

### 3) Буферизированные и небуферизированные потоки

**Небуферизированный** поток чаще ходит в ОС/диск небольшими порциями.
**Буферизированный** (`BufferedInputStream`, `BufferedOutputStream`, `BufferedReader`, `BufferedWriter`) уменьшает число системных операций и обычно быстрее.

```java
import java.io.*;
import java.nio.file.*;

public class BufferedVsUnbufferedDemo {
    public static void main(String[] args) throws Exception {
        Path src = Paths.get("tmp/io-demo/big.bin");
        Path dst1 = Paths.get("tmp/io-demo/copy-unbuffered.bin");
        Path dst2 = Paths.get("tmp/io-demo/copy-buffered.bin");

        Files.createDirectories(src.getParent());
        if (Files.notExists(src)) {
            // Генерируем ~10 MB
            byte[] data = new byte[10 * 1024 * 1024];
            for (int i = 0; i < data.length; i++) data[i] = (byte) (i % 256);
            Files.write(src, data);
        }

        long t1 = System.nanoTime();
        copyUnbuffered(src, dst1);
        long t2 = System.nanoTime();

        copyBuffered(src, dst2);
        long t3 = System.nanoTime();

        System.out.printf("Unbuffered: %.2f ms%n", (t2 - t1) / 1_000_000.0);
        System.out.printf("Buffered:   %.2f ms%n", (t3 - t2) / 1_000_000.0);
    }

    static void copyUnbuffered(Path src, Path dst) throws IOException {
        try (InputStream in = new FileInputStream(src.toFile());
             OutputStream out = new FileOutputStream(dst.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        }
    }

    static void copyBuffered(Path src, Path dst) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(src.toFile()));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(dst.toFile()))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
```

> Важный момент: даже без `Buffered*` можно частично компенсировать это, если читать/писать массивами (`byte[]`), а не по 1 байту.

---

### 4) IO vs NIO (что выбрать)

`java.io` (IO):
- Классический потоковый API.
- Проще для базовых задач.
- Часто достаточно для CLI/утилит и небольших сервисов.

`java.nio` / `java.nio.file` / `java.nio.channels` (NIO):
- `Path/Files`, каналы, буферы.
- Эффективнее для больших файлов, массовых операций, работы с каналами/селектором.
- Есть удобные high-level операции (`Files.copy/move/walk`, `FileChannel.transferTo`).

#### Пример: копирование файла через `FileChannel` (NIO)

```java
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.*;

public class NioChannelCopyDemo {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("tmp/io-demo/notes.txt");
        Path dst = Paths.get("tmp/io-demo/notes-channel-copy.txt");

        Files.createDirectories(src.getParent());
        if (Files.notExists(src)) {
            Files.writeString(src, "NIO channel demo\n");
        }

        try (FileChannel in = FileChannel.open(src, READ);
             FileChannel out = FileChannel.open(dst, CREATE, WRITE, TRUNCATE_EXISTING)) {

            long size = in.size();
            long transferred = 0;
            while (transferred < size) {
                transferred += in.transferTo(transferred, size - transferred, out);
            }
        }

        System.out.println("Copied with FileChannel: " + dst);
    }
}
```
---
### FileChannel

FileChannel используется для работы с файлами на низкоуровневом вводе-выводе.

Основные возможности класса FileChannel:
- Чтение данных из файла и запись данных в файл.
- Работа с файлом по смещениям. Можно получить текущее смещение в файле, переместить указатель чтения/записи в нужное смещение.
- Маппинг файлов в память. Файл можно отобразить в память и работать с его содержимым как с массивом байтов.
- Блокировка частей файла. Позволяет защитить критические участки файла от одновременной записи.
- Асинхронная работа с файлами.

FileChannel эффективнее потокового ввода-вывода, т. к. избавляет от накладных расходов на создание объектов и буферизацию.
Используется в приложениях, где нужна высокая производительность работы с файлами.
---

### Короткая шпаргалка

- Нужен текст с кодировкой → `Reader/Writer` или `Files.readString/writeString`.
- Нужны байты (картинки/архивы) → `InputStream/OutputStream` или `Files.newInputStream/newOutputStream`.
- Нужна скорость на больших объёмах → буферизация + NIO (`Files`, `FileChannel`).
- Всегда закрывай ресурсы через `try-with-resources`.
---

## 8) Сериализация в Java: концепция, `serialVersionUID`, ограничения

Сериализация — это преобразование объекта в поток байтов (например, для сохранения в файл или передачи по сети),
а десериализация — обратный процесс.

В Java это делается через `ObjectOutputStream` / `ObjectInputStream`.

### 8.1 Концепция и требования

Чтобы объект можно было сериализовать:
- класс должен реализовывать `java.io.Serializable` (маркерный интерфейс);
- все **несколько вложенные поля**, которые должны попасть в поток, тоже должны быть сериализуемыми;
- поля, которые нельзя/не нужно сохранять, помечают `transient`;
- `static`-поля не сериализуются (это состояние класса, а не объекта).

Пример базовой сериализации:

```java
import java.io.*;
import java.nio.file.*;

public class SerializationBasicsDemo {
    public static void main(String[] args) throws Exception {
        Path file = Path.of("tmp/serialization/user.bin");
        Files.createDirectories(file.getParent());

        User user = new User(42L, "alice", "secret-token");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeObject(user);
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            User restored = (User) in.readObject();
            System.out.println(restored.getSessionToken()); // null, т.к. transient
        }
    }

    static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private long id;
        private String login;
        private transient String sessionToken;

        User(long id, String login, String sessionToken) {
            this.id = id;
            this.login = login;
            this.sessionToken = sessionToken;
        }

        public String getSessionToken() {
            return sessionToken;
        }
    }
}
```

> Рабочие демо в проекте: `ASerializationBasicsDemo` и `BSerializationRequirementsDemo`.

---

### 8.2 Что такое `serialVersionUID` и зачем он нужен

`serialVersionUID` — версия сериализуемого класса. При десериализации JVM сверяет UID,
записанный в байтах, с UID текущего класса.

- Совпали → JVM пытается восстановить объект.
- Не совпали → `InvalidClassException`.

Если `serialVersionUID` не указан явно, JVM вычисляет его автоматически на основе структуры класса.
Это опасно для эволюции модели: даже «невинное» изменение кода может сломать совместимость.

Рекомендуемый стиль:

```java
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    // поля...
}
```

Когда повышать UID:
- вы делаете несовместимые изменения формата (удалили/переименовали важные поля, изменили иерархию и т.д.);
- вы **осознанно** хотите запретить чтение старых данных.

Когда можно оставить прежний UID:
- изменения обратно совместимы (например, добавили новое необязательное поле).

---

### 8.3 Проблемы и ограничения стандартной Java-сериализации

1. **Хрупкость версионирования**
   - при изменениях модели легко получить `InvalidClassException`;
   - нужно дисциплинированно управлять `serialVersionUID`.

2. **Безопасность**
   - десериализация недоверенных данных потенциально опасна (gadget chains, RCE-атаки);
   - не следует принимать произвольные serialized-байты извне без фильтрации и ограничений.

3. **Слабая переносимость и прозрачность формата**
   - формат Java-serialization привязан к JVM/классам;
   - неудобен для межъязыкового обмена и долгого хранения.

4. **Производительность и размер**
   - обычно хуже, чем у бинарных протоколов вроде Protobuf/Avro/Kryo;
   - поток содержит метаданные класса, что увеличивает объём.

5. **Конструкторы не вызываются как обычно**
   - при десериализации объект создаётся специальным механизмом;
   - инварианты, которые вы обеспечивали в конструкторе, могут быть обойдены.

6. **Прокси, лямбды, внутренние классы**
   - их сериализация может быть нестабильной/неочевидной между версиями.

---

### Практические рекомендации

- Для внешних API и интеграций лучше использовать JSON/Protobuf/Avro.
- Java Serialization применять только для внутренних, контролируемых сценариев.
- Всегда задавать `serialVersionUID` явно.
- Секреты (`token`, `password`, ключи) помечать `transient`.
- Не десериализовать недоверенные данные «как есть».

---

## 9) Копирование объектов в Java: `shallow/deep`, `clone()`, Prototype, сериализация

Ключевая идея: в Java копируется **ссылка**, а не сам объект. Поэтому «копирование объекта» — это всегда отдельная стратегия, которую вы реализуете явно.

### 9.1 Shallow copy vs Deep copy

Допустим, есть объект `User`, внутри которого лежит `Address`.

- **Shallow copy** копирует поля верхнего уровня, но вложенные объекты остаются общими.
- **Deep copy** копирует весь граф объектов (или ту его часть, которую вы считаете частью состояния).

```java
import java.util.ArrayList;
import java.util.List;

class Address {
    String city;

    Address(String city) {
        this.city = city;
    }
}

class User {
    String name;
    Address address;
    List<String> tags;

    User(String name, Address address, List<String> tags) {
        this.name = name;
        this.address = address;
        this.tags = tags;
    }

    // Shallow copy: Address и tags будут теми же объектами
    User shallowCopy() {
        return new User(this.name, this.address, this.tags);
    }

    // Deep copy: создаём новые вложенные объекты
    User deepCopy() {
        Address copiedAddress = new Address(this.address.city);
        List<String> copiedTags = new ArrayList<>(this.tags);
        return new User(this.name, copiedAddress, copiedTags);
    }
}

public class CopyDemo {
    public static void main(String[] args) {
        User original = new User("Alice", new Address("Berlin"), new ArrayList<>(List.of("vip")));

        User shallow = original.shallowCopy();
        User deep = original.deepCopy();

        original.address.city = "Paris";
        original.tags.add("new");

        System.out.println(shallow.address.city); // Paris  (общий Address)
        System.out.println(shallow.tags);         // [vip, new] (общий List)

        System.out.println(deep.address.city);    // Berlin (независимая копия)
        System.out.println(deep.tags);            // [vip]
    }
}
```

> На практике deep copy почти всегда нужно делать осознанно: какие поля считаются частью «владения» объекта, а какие можно делить.

---

### 9.2 `clone()` в Java: как работает и почему с ним осторожно

`Object#clone()` делает поверхностное копирование (по умолчанию) и требует `Cloneable`.

```java
import java.util.ArrayList;
import java.util.List;

class Profile implements Cloneable {
    String name;
    List<String> skills;

    Profile(String name, List<String> skills) {
        this.name = name;
        this.skills = skills;
    }

    @Override
    protected Profile clone() {
        try {
            Profile copy = (Profile) super.clone(); // shallow-копия полей
            copy.skills = new ArrayList<>(this.skills); // вручную углубляем нужные поля
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
```

Минусы `clone()`:
- неочевидный контракт (`Cloneable` — маркер без метода);
- легко забыть углубить mutable-поля;
- неудобно с `final` полями и иерархиями;
- хуже читается, чем явный copy-конструктор/фабрика.

Обычно в продакшене чаще используют:
- **copy constructor** (`new User(existingUser)`);
- статическую фабрику (`User.copyOf(user)`);
- record + неизменяемые поля (копирование через создание нового экземпляра).

---

### 9.3 Prototype pattern для копирования

Prototype — это «создавай новый объект, клонируя прототип».

```java
import java.util.HashMap;
import java.util.Map;

interface Prototype<T> {
    T copy();
}

class GameUnit implements Prototype<GameUnit> {
    private final String type;
    private final int baseHp;

    GameUnit(String type, int baseHp) {
        this.type = type;
        this.baseHp = baseHp;
    }

    @Override
    public GameUnit copy() {
        return new GameUnit(type, baseHp);
    }

    @Override
    public String toString() {
        return "GameUnit{" + "type='" + type + '\'' + ", baseHp=" + baseHp + '}';
    }
}

class UnitRegistry {
    private final Map<String, GameUnit> prototypes = new HashMap<>();

    void register(String key, GameUnit prototype) {
        prototypes.put(key, prototype);
    }

    GameUnit create(String key) {
        GameUnit prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown prototype: " + key);
        }
        return prototype.copy();
    }
}

public class PrototypeDemo {
    public static void main(String[] args) {
        UnitRegistry registry = new UnitRegistry();
        registry.register("archer", new GameUnit("Archer", 70));
        registry.register("tank", new GameUnit("Tank", 200));

        GameUnit u1 = registry.create("archer");
        GameUnit u2 = registry.create("tank");

        System.out.println(u1);
        System.out.println(u2);
    }
}
```

Где удобно:
- когда создание объекта «с нуля» дорогое;
- когда много готовых пресетов/шаблонов состояния;
- когда нужно скрыть детали инициализации.

---

### 9.4 Другие способы копирования (через сериализацию и не только)

#### Вариант A: Java Serialization (байтовый round-trip)

Работает как deep copy, если весь граф сериализуем.

```java
import java.io.*;

public class SerializationCopyUtil {
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(obj);
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (T) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }
}
```

Плюс: минимум ручного кода для сложных графов.  
Минусы: медленнее, требования к `Serializable`, ограничения безопасности/эволюции схемы.

#### Вариант B: JSON round-trip (Jackson/Gson)

Идея та же: `obj -> JSON -> obj`. Удобно, когда уже есть JSON-модель. Но:
- возможны потери типов/форматов;
- для полиморфизма нужна дополнительная конфигурация;
- обычно тоже медленнее ручного копирования.

#### Вариант C: mapstruct / мапперы / ручные DTO-конвертеры

Надёжный путь для бизнес-кода:
- явно контролируете, какие поля копируются;
- можно делать преобразования и валидацию;
- проще поддерживать при изменениях модели.

---

### 9.5 Что выбрать на практике

- Нужна **предсказуемость и контроль** → copy constructor / `copyOf` / mapper.
- Нужна **скорость** → избегать сериализации, писать явное копирование горячих путей.
- Нужна **простота для сложного графа** и это внутренний сценарий → можно рассмотреть serialization round-trip.
- `clone()` использовать только если в проекте уже принят этот стиль и есть чёткие правила для deep-copy mutable полей.
