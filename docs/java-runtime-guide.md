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
