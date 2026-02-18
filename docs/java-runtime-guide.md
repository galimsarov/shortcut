# Как работает Java — разбор на примерах

## 1) JDK, JRE, JVM

- **JVM (Java Virtual Machine)** — виртуальная машина, которая исполняет байткод (`.class`).
- **JRE (Java Runtime Environment)** — JVM + стандартные библиотеки Java для запуска приложений.
- **JDK (Java Development Kit)** — JRE + инструменты разработки (`javac`, `javadoc`, `jdb`, `jar` и т.д.).

Иерархия:

```text
JDK = JRE + инструменты разработчика
JRE = JVM + стандартные библиотеки
```

Пример процесса:

```bash
# 1) компиляция исходников в байткод
javac App.java

# 2) запуск байткода на JVM
java App
```

---

## 2) Загрузка приложения. Раннее и позднее связывание

### Как загружается Java-приложение

1. Вызывается `java Main`.
2. JVM запускается и создает базовые runtime-структуры.
3. ClassLoader загружает `Main.class` и зависимости.
4. Происходит **linking** класса:
   - **verification** (проверка байткода),
   - **preparation** (выделение памяти под static-поля),
   - **resolution** (разрешение символических ссылок в реальные).
5. **initialization** — выполняются статические инициализаторы (`static {}`) и инициализация static-полей.
6. Запускается `public static void main(String[] args)`.

### Раннее и позднее связывание

- **Раннее связывание** (compile-time): вызов определяется на этапе компиляции.
  - Например: `private`, `static`, `final` методы, перегрузка (overloading).
- **Позднее связывание** (runtime): конкретный метод выбирается во время выполнения.
  - Например: переопределенные (`@Override`) методы (полиморфизм).

Пример:

```java
class Parent {
    static void staticMethod() {
        System.out.println("Parent.staticMethod");
    }

    void instanceMethod() {
        System.out.println("Parent.instanceMethod");
    }
}

class Child extends Parent {
    static void staticMethod() {
        System.out.println("Child.staticMethod");
    }

    @Override
    void instanceMethod() {
        System.out.println("Child.instanceMethod");
    }
}

public class BindingDemo {
    public static void main(String[] args) {
        Parent p = new Child();

        // Раннее связывание: static-метод вызывается по типу ссылки Parent
        p.staticMethod(); // Parent.staticMethod

        // Позднее связывание: instance-метод выбирается по реальному типу Child
        p.instanceMethod(); // Child.instanceMethod
    }
}
```

---

## 3) JIT-компилятор

JIT (Just-In-Time) — часть JVM, которая компилирует "горячие" участки байткода в машинный код во время выполнения.

Что это дает:

- старт может быть чуть медленнее (интерпретация + профилирование),
- затем ускорение на long-running нагрузках,
- оптимизации на основе реальных данных выполнения.

Пример идеи:

```java
public class JitDemo {
    public static void main(String[] args) {
        long sum = 0;
        for (int i = 0; i < 100_000_000; i++) {
            sum += i;
        }
        System.out.println(sum);
    }
}
```

На первых итерациях код может исполняться интерпретатором, после чего JIT скомпилирует цикл в более быстрый машинный код.

Полезные флаги для наблюдения (зависят от версии JVM):

```bash
java -XX:+PrintCompilation JitDemo
```

---

## 4) ClassLoaders

В JVM используется иерархия загрузчиков классов:

1. **Bootstrap ClassLoader** — загружает базовые классы JDK (`java.lang.*`).
2. **Platform (Extension) ClassLoader** — платформенные модули.
3. **Application ClassLoader** — классы приложения из classpath.

Пример:

```java
public class ClassLoaderDemo {
    public static void main(String[] args) {
        ClassLoader app = ClassLoaderDemo.class.getClassLoader();
        System.out.println("App loader: " + app);

        ClassLoader platform = app.getParent();
        System.out.println("Platform loader: " + platform);

        ClassLoader bootstrap = platform != null ? platform.getParent() : null;
        System.out.println("Bootstrap loader (обычно null в выводе): " + bootstrap);

        // Классы JDK обычно загружены bootstrap-загрузчиком
        System.out.println("String loader: " + String.class.getClassLoader()); // null
    }
}
```

Важно: действует модель **parent delegation** (сначала запрос родителю, потом попытка загрузки самим).

---

## 5) Устройство памяти Java и утечки памяти

(для HotSpot, упрощенно)

- **Heap** — объекты (`new`), управляется GC.
  - Young Generation (Eden + Survivor)
  - Old Generation
- **Metaspace** — метаданные классов (вместо PermGen в старых версиях).
- **Thread Stack** — стек каждого потока (локальные переменные, вызовы методов).
- **PC Register / Native Stack** — служебные области исполнения.

### Пример утечки памяти в Java

В Java утечка обычно не из-за "забыли free", а из-за того, что объект остается достижимым.

```java
import java.util.ArrayList;
import java.util.List;

public class MemoryLeakDemo {
    // Статическая коллекция удерживает ссылки на объекты до конца жизни приложения
    private static final List<byte[]> CACHE = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            CACHE.add(new byte[1024 * 1024]); // +1 MB
            System.out.println("Allocated MB: " + CACHE.size());
        }
    }
}
```

Этот пример рано или поздно приведет к `OutOfMemoryError`, потому что GC не может освободить объекты, на которые есть ссылки в `CACHE`.

Типичные причины утечек:

- бесконтрольные cache/map/list,
- забытые listeners/observers,
- ThreadLocal без cleanup,
- ClassLoader leak в контейнерах.

---

## 6) Параметры запуска Java (JVM options)

Часто используемые параметры:

```bash
# начальный и максимальный размер heap
java -Xms512m -Xmx2g App

# выбор GC (пример для G1)
java -XX:+UseG1GC App

# ограничение metaspace
java -XX:MaxMetaspaceSize=256m App

# вывод информации о GC (Java 9+)
java -Xlog:gc App

# передача системного свойства
java -Denv=prod App
```

Пример комплексного запуска:

```bash
java -Xms1g -Xmx1g -XX:+UseG1GC -Xlog:gc -Dspring.profiles.active=prod -jar app.jar
```

Мини-чеклист:

- для контейнеров обязательно проверять лимиты памяти,
- не ставить `-Xmx` вплотную к лимиту контейнера,
- подбирать GC и размеры памяти по профилю нагрузки,
- смотреть GC-логи и метрики, а не только "ощущения".

---

## Итог

- JDK нужен для разработки, JRE/JVM — для выполнения.
- Загрузка класса проходит этапы loading/linking/initialization.
- Полиморфизм — это позднее связывание.
- JIT ускоряет hot spots во время работы.
- Память Java не сводится только к heap; утечки в Java чаще всего связаны с удержанием ссылок.
- JVM-флаги критичны для производительности и стабильности в проде.

---

## 7) Сборщик мусора (GC) в Java: виды, принципы, G1 и ссылки

### 7.1 Базовые принципы работы GC

GC в Java освобождает память **автоматически**: удаляет объекты, которые больше не достижимы из GC Roots.

Что такое GC Roots (упрощенно):

- локальные переменные в стеках потоков,
- активные потоки,
- static-поля загруженных классов,
- JNI-ссылки из нативного кода.

Если от roots до объекта нельзя пройти по ссылкам — объект считается мусором и может быть удален.

Пример:

```java
public class GcBasicExample {
    static class User {
        String name;
        User(String name) { this.name = name; }
    }

    public static void main(String[] args) {
        User u = new User("Alice");
        // объект достижим через локальную переменную u

        u = null;
        // после этой строки объект недостижим и может быть собран GC

        System.gc(); // только просьба к JVM, а не гарантия немедленного GC
    }
}
```

Ключевая идея поколений (Generational hypothesis):

- большинство объектов "живут" недолго,
- поэтому heap делится на поколения: Young и Old,
- часто чистим Young (быстро), реже — Old (дороже).

---

### 7.2 Виды GC в HotSpot JVM

Ниже — упрощенная практическая картина (актуальность зависит от версии JDK):

1. **Serial GC** (`-XX:+UseSerialGC`)
   - Один поток GC, простейший, stop-the-world.
   - Подходит для маленьких heap/простых сред.

2. **Parallel GC** (`-XX:+UseParallelGC`)
   - Несколько потоков GC, ориентирован на throughput.
   - Паузы могут быть длиннее, чем у low-pause коллекторов.

3. **G1 GC** (`-XX:+UseG1GC`)
   - Делит heap на регионы, собирает их приоритетно.
   - Цель: предсказуемые паузы при большом heap.
   - Часто дефолт в современных JDK.

4. **ZGC** (`-XX:+UseZGC`)
   - Очень короткие паузы, масштабируется на большие heap.
   - Подходит для latency-sensitive сервисов.

5. **Shenandoah** (`-XX:+UseShenandoahGC`)
   - Тоже low-pause, конкурентная очистка.
   - Доступность зависит от сборки/дистрибутива JDK.

Мини-пример запуска с разными GC:

```bash
java -Xms1g -Xmx1g -XX:+UseSerialGC App
java -Xms1g -Xmx1g -XX:+UseParallelGC App
java -Xms1g -Xmx1g -XX:+UseG1GC App
```

---

### 7.3 Подробный разбор G1 GC

G1 = Garbage First, потому что он старается сначала собирать регионы, где можно освободить больше памяти за меньшую цену.

#### Как G1 организует память

- Heap разбивается на множество **regions** одинакового размера (например, 1–32 MB).
- Логически регионы могут быть Eden, Survivor, Old, Humongous.
- Нет жесткого физического деления как в старых схемах "сплошной Young/Old".

#### Ключевые циклы G1

1. **Young GC (эвакуация)**
   - Stop-the-world пауза.
   - Живые объекты из Eden/Survivor копируются в новые Survivor или Old.
   - Мусор просто не копируется.

2. **Concurrent Marking (конкурентная маркировка Old)**
   - G1 параллельно с приложением оценивает живые объекты в old-регионах.
   - Формирует карту "где сколько мусора".

3. **Mixed GC**
   - После маркировки G1 делает паузы, где очищает не только Young, но и часть Old-регионов с наибольшей выгодой.
   - Именно тут проявляется "Garbage First".

4. **Remark / Cleanup**
   - Короткие фазы для финализации результатов маркировки и обновления служебных структур.

#### Почему G1 предсказуемее по паузам

- Можно задавать целевую паузу: `-XX:MaxGCPauseMillis=200` (не строгая гарантия, а цель).
- G1 выбирает набор регионов на сборку с учетом модели стоимости паузы.

#### Важные параметры G1

```bash
java \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -Xlog:gc*:file=gc.log \
  -jar app.jar
```

- `MaxGCPauseMillis` — целевая длительность паузы.
- `InitiatingHeapOccupancyPercent` — порог заполнения heap для старта concurrent marking.
- `-Xlog:gc*` — подробные GC-логи.

#### Мини-сценарий "что происходит"

```java
import java.util.ArrayList;
import java.util.List;

public class G1BehaviorDemo {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<>();

        for (int i = 0; i < 10_000; i++) {
            // короткоживущие объекты
            byte[] tmp = new byte[1024 * 50];

            // часть объектов делаем долгоживущими
            if (i % 100 == 0) {
                list.add(new byte[1024 * 200]);
            }

            if (i % 500 == 0) {
                System.out.println("step=" + i + ", retained=" + list.size());
            }
        }
    }
}
```

- Большинство `tmp` умрет в Young GC.
- Объекты в `list` переживают несколько циклов и продвигаются в Old.
- При накоплении мусора в Old G1 запускает concurrent marking и mixed GC.

---

### 7.4 Виды ссылок в Java (Reference Types)

В Java, кроме обычных (strong) ссылок, есть специальные типы в `java.lang.ref`.

1. **Strong reference** (обычная ссылка)
   - Пока есть strong-ссылка, объект не удаляется.

```java
Object strong = new Object();
```

2. **SoftReference**
   - Объект может быть удален при нехватке памяти.
   - Исторически использовалась для memory-sensitive cache.

```java
import java.lang.ref.SoftReference;

SoftReference<byte[]> soft = new SoftReference<>(new byte[1024 * 1024]);
byte[] data = soft.get(); // может быть null после GC под давлением памяти
```

3. **WeakReference**
   - Объект удаляется при ближайшем GC, если нет strong-ссылок.
   - Часто применяется в структурах наподобие `WeakHashMap`.

```java
import java.lang.ref.WeakReference;

Object obj = new Object();
WeakReference<Object> weak = new WeakReference<>(obj);
obj = null;
System.gc();
System.out.println(weak.get()); // часто null (но не гарантировано немедленно)
```

4. **PhantomReference**
   - `get()` всегда возвращает `null`.
   - Используется вместе с `ReferenceQueue` для пост-мортем уведомлений (после недостижимости объекта), например, контроль cleanup ресурсов.

```java
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

Object target = new Object();
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> phantom = new PhantomReference<>(target, queue);

target = null;
System.gc();
// затем можно проверять queue.poll()/remove() и выполнять cleanup-логику
```

Практический ориентир:

- для обычной логики приложения — strong ссылки,
- для ассоциативных структур "не мешать GC" — weak,
- для специальных сценариев кэша — soft (с осторожностью),
- для контроля жизненного цикла с очередью ссылок — phantom.

---

### 7.5 Что полезно смотреть в проде

- частоту и длительность пауз GC,
- динамику heap occupancy до/после GC,
- скорость аллокаций,
- promotion rate (переход из Young в Old),
- full GC (если появляются часто — это тревожный сигнал).

Быстрый старт логирования:

```bash
java -Xlog:gc*,safepoint:file=gc.log:time,uptime,level,tags -jar app.jar
```

