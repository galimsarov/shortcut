# Java Multithreading — Part 1 (темы 1–8)

> Первая часть из 16 тем. В этом документе разбираем именно тот план, который вы предложили.

## План части 1 (8 тем)
1. Общие понятия: процесс, поток, синхронизация, монитор
2. Способы создания потоков: `Thread`, `Runnable`, `Callable`
3. `ExecutorService`, `Executors`
4. Жизненный цикл потока
5. Синхронизация: `wait/notify`, `join`, `Semaphore`, `Lock/ReentrantLock`, `CountDownLatch`, `CyclicBarrier`
6. Ключевое слово `volatile`
7. Java Memory Model (JMM), принципы happens-before
8. Atomic типы данных

---

## 1) Общие понятия: процесс, поток, синхронизация, монитор

### Процесс
**Процесс** — запущенная программа с собственным адресным пространством и ресурсами ОС.

- у процесса своя память (в контексте ОС), дескрипторы, системный контекст;
- процессы изолированы друг от друга;
- межпроцессное взаимодействие сложнее и дороже, чем взаимодействие потоков.

### Поток
**Поток (thread)** — отдельный путь выполнения внутри процесса.

- потоки одного процесса разделяют общую heap-память;
- у каждого потока есть свой stack (стек вызовов);
- несколько потоков ускоряют выполнение задач, но повышают сложность из-за shared state.

### Синхронизация
**Синхронизация** — это набор механизмов, которые управляют доступом нескольких потоков к общим данным и согласуют порядок их выполнения.

Иначе говоря, синхронизация отвечает на 2 вопроса:
1. кто сейчас имеет право работать с общим ресурсом;
2. когда один поток «увидит» изменения, сделанные другим.

Синхронизация нужна, чтобы:
- обеспечить взаимное исключение (чтобы только один поток входил в критическую секцию);
- обеспечить корректную видимость изменений между потоками;
- координировать порядок выполнения (кто и когда продолжит работу).

Что может быть синхронизировано через `synchronized`:

1) **Блок кода** (lock на конкретном объекте):
```java
private final Object lock = new Object();
private int counter = 0;

public void increment() {
    synchronized (lock) {
        counter++;
    }
}
```

2) **Метод экземпляра** (`synchronized`-метод, lock на `this`):
```java
private int balance = 0;

public synchronized void deposit(int amount) {
    balance += amount;
}
```

3) **Статический метод** (lock на `Class`-объекте, то есть «на уровне класса»):
```java
private static int globalCounter = 0;

public static synchronized void incGlobal() {
    globalCounter++;
}
```

> Важно: «синхронизация класса» в Java обычно означает захват монитора объекта `MyClass.class`.

### Монитор
**Монитор** — это объект-синхронизатор, который предоставляет:
- взаимное исключение (в один момент времени критическую секцию под этим монитором выполняет только один поток);
- очередь ожидания для `wait/notify/notifyAll`.

В Java монитор связан с объектом (или с `Class`-объектом для `static synchronized`), поэтому каждый объект может выступать **монитором**:
- вход в `synchronized` — захват монитора;
- выход из `synchronized` — освобождение монитора;
- `wait/notify/notifyAll` — механизм ожидания/сигнализации на этом же мониторе.

Пример 1: монитор — **отдельно созданный объект**:
```java
class Counter {
    private final Object monitor = new Object();
    private int value;

    public void inc() {
        synchronized (monitor) {
            value++;
        }
    }
}
```

Пример 2: монитор — **уже существующий объект** (`this`):
```java
class Counter {
    private int value;

    public synchronized void inc() { // эквивалентно synchronized(this)
        value++;
    }
}
```

Пример 3: монитор на уровне класса (`Counter.class`):
```java
class Counter {
    private static int global;

    public static synchronized void incGlobal() { // lock на Counter.class
        global++;
    }
}
```

---

## 2) Способы создания потоков

### 2.1 `Thread`
```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread: " + getName());
    }
}

MyThread t = new MyThread();
t.start();
```

Когда применять:
- для учебных примеров;
- в реальном коде реже, т.к. наследование от `Thread` жёстко связывает задачу и поток.

### 2.2 `Runnable`
```java
Runnable task = () -> System.out.println("Runnable task");
Thread t = new Thread(task);
t.start();
```

Плюсы:
- отделяет «что выполнять» от «где выполнять»;
- легче переиспользовать и тестировать.

### 2.3 `Callable`
```java
Callable<Integer> task = () -> 42;
ExecutorService pool = Executors.newSingleThreadExecutor();
Future<Integer> future = pool.submit(task);
Integer result = future.get();
pool.shutdown();
```

Отличия от `Runnable`:
- возвращает результат;
- может бросать checked exceptions.

---

## 3) `ExecutorService`, `Executors`

### Зачем
Создавать `new Thread(...)` на каждую задачу дорого и трудно контролировать. Обычно используют пул потоков.

### Базовый пример
```java
ExecutorService pool = Executors.newFixedThreadPool(4);

for (int i = 0; i < 10; i++) {
    int taskId = i;
    pool.submit(() -> System.out.println("Task " + taskId + " in " + Thread.currentThread().getName()));
}

pool.shutdown();
```

### Частые фабрики `Executors`
- `newFixedThreadPool(n)` — фиксированное число потоков;
- `newCachedThreadPool()` — динамический пул;
- `newSingleThreadExecutor()` — один рабочий поток;
- `newScheduledThreadPool(n)` — отложенные/периодические задачи.

### Практика
- всегда завершайте пул (`shutdown` / `shutdownNow`);
- для production часто создают `ThreadPoolExecutor` явно с настраиваемой очередью и политикой отказа.

---

## 4) Жизненный цикл потока

Основные состояния `Thread.State`:

- `NEW` — создан, не запущен;
- `RUNNABLE` — готов к выполнению/выполняется;
- `BLOCKED` — ждёт входа в `synchronized`;
- `WAITING` — бессрочно ждёт событие (`wait`, `join` без таймаута);
- `TIMED_WAITING` — ждёт с таймаутом (`sleep`, `wait(timeout)`, `join(timeout)`);
- `TERMINATED` — завершён.

Ключевые моменты:
- `start()` можно вызвать только один раз;
- прямой вызов `run()` не запускает новый поток (это обычный вызов метода);
- `join()` позволяет дождаться завершения другого потока.

---

## 5) Синхронизация

### 5.1 `wait/notify`
Используются для координации потоков через монитор.

Правила:
1. Вызывать только внутри `synchronized` на том же объекте.
2. `wait()` освобождает монитор и переводит поток в ожидание.
3. Проверка условия — через `while`, а не `if`.

```java
class SignalBox {
    private boolean ready = false;

    public synchronized void await() throws InterruptedException {
        while (!ready) {
            wait();
        }
    }

    public synchronized void signal() {
        ready = true;
        notifyAll();
    }
}
```

### 5.2 `join`
`join()` блокирует текущий поток, пока другой поток не завершится.

```java
Thread t = new Thread(() -> doWork());
t.start();
t.join(); // ждём завершения t
```

### 5.3 `Semaphore`
Ограничивает число потоков, одновременно входящих в секцию.

```java
Semaphore sem = new Semaphore(3); // максимум 3 потока
sem.acquire();
try {
    useResource();
} finally {
    sem.release();
}
```

### 5.4 `Lock` / `ReentrantLock`
Гибкая альтернатива `synchronized`.

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    criticalSection();
} finally {
    lock.unlock();
}
```

Плюсы:
- `tryLock`, `lockInterruptibly`;
- возможность нескольких `Condition`.

### 5.5 `CountDownLatch`
Позволяет одному/нескольким потокам ждать, пока счётчик не дойдёт до нуля.

```java
CountDownLatch latch = new CountDownLatch(3);

// в рабочих потоках
latch.countDown();

// в ожидающем потоке
latch.await();
```

### 5.6 `CyclicBarrier` (Barrier)
Барьер: группа потоков должна дойти до общей точки, после чего все продолжают.

```java
CyclicBarrier barrier = new CyclicBarrier(3);

// в каждом потоке
preparePart();
barrier.await();
mergePart();
```

---

## 6) Ключевое слово `volatile`

`volatile` гарантирует:
- видимость последней записи переменной для других потоков;
- ограничения на переупорядочивание инструкций вокруг чтения/записи volatile.

`volatile` **не гарантирует атомарность** составных операций (`x++`, `check-then-act`).

Пример:
```java
class Worker implements Runnable {
    private volatile boolean running = true;

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            // работа
        }
    }
}
```

---

## 7) Java Memory Model (JMM) и happens-before

### Что задаёт JMM
JMM определяет, какие значения могут видеть потоки и при каких условиях изменения одного потока гарантированно видны другому.

### Happens-before (основные правила)
Если действие A happens-before B, то результат A гарантированно виден в B.

Ключевые правила:
1. **Program Order Rule**: внутри одного потока операции видны в порядке программы.
2. **Monitor Lock Rule**: `unlock` монитора happens-before последующему `lock` того же монитора.
3. **Volatile Rule**: запись в `volatile` happens-before последующему чтению этой же переменной.
4. **Thread Start Rule**: всё до `thread.start()` видно в запущенном потоке.
5. **Thread Join Rule**: всё в потоке видно после успешного `join()`.
6. **Transitivity**: если A hb B и B hb C, тогда A hb C.

Практический вывод: корректная синхронизация нужна не только для взаимного исключения, но и для гарантии видимости данных.

---

## 8) Atomic типы данных

Пакет `java.util.concurrent.atomic` даёт lock-free операции на основе CAS.

Часто используемые типы:
- `AtomicInteger`
- `AtomicLong`
- `AtomicBoolean`
- `AtomicReference<T>`

Пример:
```java
AtomicInteger counter = new AtomicInteger(0);

counter.incrementAndGet();
counter.addAndGet(5);
int val = counter.get();
```

Почему это полезно:
- часто быстрее и проще, чем `synchronized` для простых счётчиков/флагов;
- атомарные операции без явной блокировки.

Ограничение:
- если нужно атомарно менять **несколько полей сразу**, одних атомиков может быть недостаточно — может потребоваться lock или другая стратегия.

---

## Краткий итог Part 1

После этой части вы должны уверенно понимать:
- базовые сущности (`process`, `thread`, `monitor`);
- как запускать задачи (`Thread`/`Runnable`/`Callable`, `ExecutorService`);
- как координировать потоки (`join`, `wait/notify`, `Semaphore`, `Lock`, `CountDownLatch`, `CyclicBarrier`);
- как обеспечивать корректную видимость и атомарность (`volatile`, JMM hb-правила, atomic типы).
