# Java Collections Framework — продвинутый гайд с примерами

Этот раздел разбирает **коллекции Java** на практических примерах:

1. Иерархия Collections Framework.
2. Отличия основных коллекций и когда что выбирать.
3. Производительность (Big-O + практические нюансы).

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
