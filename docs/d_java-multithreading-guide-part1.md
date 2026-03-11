# Java Multithreading — Part 1 (темы 1–8)

> Первая часть из 16 тем. В этом документе разбираем именно тот план, который вы предложили.

## Перед стартом: параллелизм, многопоточность и асинхронность

**Параллелизм** — это фактическое одновременное выполнение нескольких операций в один и тот же момент времени (обычно на разных ядрах CPU).

- если у машины одно ядро, «честного» параллелизма по CPU-задачам не будет;
- если ядер несколько, независимые задачи могут реально выполняться одновременно.

**Многопоточность** — это модель, в которой внутри одного процесса одновременно (или псевдо-одновременно, в зависимости от планировщика и числа ядер) выполняются несколько потоков (`Thread`).

**Асинхронность** — это модель неблокирующего выполнения, когда задача запускается и результат забирается позже (через callback, `Future`, `CompletableFuture`, event loop и т.д.), без обязательного ожидания в текущем месте кода.

Ключевая разница:
- параллелизм отвечает на вопрос **«выполняется ли реально одновременно?»**;
- многопоточность отвечает на вопрос **«кто исполняет?»** (несколько потоков);
- асинхронность отвечает на вопрос **«как организовано ожидание результата?»** (не блокируя текущий поток).

Их можно комбинировать: асинхронный API может выполняться на пуле потоков (и тогда часто даёт и многопоточность, и параллелизм), а может быть и однопоточным (например, event loop).

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

По умолчанию любая Java-программа стартует с потока `main` (именно в нём выполняется метод `public static void main(...)`).
При этом в JVM обычно есть и другие служебные потоки, например:
- потоки сборщика мусора (GC);
- компилятор JIT;
- обработчики служебных событий JVM.

То есть даже «однопоточное» приложение на уровне кода часто уже работает в процессе, где живут дополнительные внутренние потоки JVM.

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

Можно ли вызвать `start()` второй раз?
- **нет**: один и тот же объект `Thread` можно запустить только один раз;
- повторный вызов `start()` бросит `IllegalThreadStateException`.

### 2.2 `Runnable`
```java
Runnable task = () -> System.out.println("Runnable task");
Thread t = new Thread(task);
t.start();
```

Плюсы:
- отделяет «что выполнять» от «где выполнять»;
- легче переиспользовать и тестировать.

Можно ли вызвать `start()` второй раз?
- для **того же объекта `Thread`** — **нет** (будет `IllegalThreadStateException`);
- но **тот же `Runnable`** можно запустить повторно, создав новый `Thread`, либо отправив задачу в `ExecutorService`.

### 2.3 `Callable`
```java
Callable<Integer> task = () -> {
    System.out.println("Callable runs in: " + Thread.currentThread().getName());
    return 42;
};

// FutureTask<T> — это адаптер, который одновременно реализует Runnable и Future<T>:
// его можно запустить в Thread как Runnable, а результат получить как Future.
FutureTask<Integer> futureTask = new FutureTask<>(task);

Thread t = new Thread(futureTask, "callable-thread");
t.start();

Integer result = futureTask.get(); // получаем результат (при необходимости ждём завершения)
System.out.println("Result = " + result);
```

Отличия от `Runnable`:
- возвращает результат;
- может бросать checked exceptions.

Можно ли вызвать `start()` второй раз?
- для **того же объекта `Thread`** — **нет** (как и всегда);
- `Callable` обычно запускают через `ExecutorService`: его можно отправлять повторно (создавая новые `Future`),
  но конкретный `FutureTask` — одноразовый (повторный запуск после завершения не используется как штатный сценарий).

---

## 3) `ExecutorService`, `Executors`

### Сначала определения

**`ExecutorService`** — это интерфейс из `java.util.concurrent` для управления выполнением задач.

Это **не только «пул потоков» как идея**, а контракт, который даёт:
- отправку задач (`execute`, `submit`, `invokeAll`, `invokeAny`);
- получение результата через `Future`;
- управление жизненным циклом исполнителя (`shutdown`, `awaitTermination`, `shutdownNow`).

На практике чаще всего `ExecutorService` действительно реализован как **пул потоков** (`ThreadPoolExecutor`), но концептуально это именно «сервис выполнения задач».

**`Executors`** — это утилитный класс-фабрика, который создаёт готовые реализации `ExecutorService` и `ScheduledExecutorService`.

Проще: `ExecutorService` — **что используем в коде (интерфейс)**, `Executors` — **как быстро создать типовой экземпляр**.

### Почему пул лучше ручного создания потоков
Ручной подход (`new Thread(...).start()`) полезен для базового понимания, но в прикладном коде у него минусы:
- дорогой старт потока на каждую задачу;
- нет централизованного ограничения конкуренции;
- сложно контролировать очередь задач и graceful shutdown;
- легко «пересоздать» слишком много потоков и уронить производительность.

Пул решает это так:
- потоки переиспользуются;
- есть ограничение параллелизма;
- есть очередь задач;
- есть стандартные механизмы завершения и мониторинга.

### Базовый пример
```java
ExecutorService pool = Executors.newFixedThreadPool(4);

for (int i = 0; i < 10; i++) {
    int taskId = i;
    pool.execute(() -> System.out.println("Task " + taskId + " in " + Thread.currentThread().getName()));
}

pool.shutdown();
```

Здесь уместен `execute(...)`, потому что результат не нужен (аналог `Runnable`).

### Пример с возвратом значений (`Callable` + `submit`)
```java
ExecutorService pool = Executors.newFixedThreadPool(4);

List<Future<Integer>> futures = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    int value = i;
    futures.add(pool.submit(() -> value * value)); // Callable<Integer>
}

List<Integer> squares = new ArrayList<>();
for (Future<Integer> future : futures) {
    squares.add(future.get()); // ждём конкретную задачу и забираем результат
}

System.out.println("Squares = " + squares);
pool.shutdown();
```

Что важно по методам:
- `execute(Runnable)` — запускает задачу без возвращаемого значения;
- `submit(Runnable)` — тоже без полезного результата, но вернёт `Future<?>` (можно проверить завершение/ошибку);
- `submit(Callable<T>)` — вернёт `Future<T>` с результатом.

### Частые фабрики `Executors`
- `newFixedThreadPool(n)` — фиксированное число потоков;
- `newCachedThreadPool()` — динамический пул;
- `newSingleThreadExecutor()` — один рабочий поток;
- `newScheduledThreadPool(n)` — отложенные/периодические задачи.

### Когда какой вариант выбирать

1. **`newFixedThreadPool(n)`**
   - когда нужно предсказуемо ограничить число одновременно выполняемых задач;
   - типично для CPU-bound задач (часто `n` близко к числу ядер);
   - подходит для стабильной нагрузки.

2. **`newCachedThreadPool()`**
   - когда много коротких задач и нагрузка сильно «пульсирует»;
   - пул может быстро расти и так же сжиматься;
   - осторожно: без верхней границы можно создать слишком много потоков под пиком.

3. **`newSingleThreadExecutor()`**
   - когда нужен строгий порядок выполнения задач (FIFO в одном рабочем потоке);
   - полезно для сериализации доступа к ресурсу без `synchronized` в вызывающем коде.

4. **`newScheduledThreadPool(n)`**
   - когда есть задачи «через X времени» или «каждые N секунд»;
   - поддерживает `schedule(...)`, `scheduleAtFixedRate(...)`, `scheduleWithFixedDelay(...)`.

### Можно ли одним пулом закрыть всё приложение
Технически можно, но обычно это плохая идея.

Почему:
- периодические/долгие задачи могут занять все потоки и блокировать срочные;
- разные классы задач имеют разный профиль (CPU-bound, I/O-bound, scheduled) и требуют разной конфигурации;
- сложнее наблюдать и дебажить деградацию.

Практичнее разделять исполнители по назначению:
- отдельный пул для «обычных» асинхронных задач;
- отдельный `ScheduledExecutorService` для таймеров/периодики;
- при необходимости — отдельные пулы для тяжёлого I/O и для CPU-задач.

То есть: **да, в одном приложении можно запускать часть задач сразу, часть отложенно и часть периодически**, но лучше делать это через несколько специализированных executor-ов, а не через один «универсальный».

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

Как посмотреть текущее состояние потока:
- для текущего потока: `Thread.currentThread().getState()`;
- для конкретного объекта потока: `someThread.getState()`.

Пример:
```java
System.out.println("Current thread = " + Thread.currentThread().getName());
System.out.println("Current state = " + Thread.currentThread().getState());
```

---

## 5) Синхронизация

Напоминание: синхронизация — это механизмы, которые координируют доступ потоков к общим данным,
обеспечивают взаимное исключение и корректную видимость изменений между потоками.

Важно по API:
- `wait()`, `notify()`, `notifyAll()` — это методы класса `Object` (не `Thread`);
- `join()` — это метод класса `Thread`.

Поэтому `wait/notify` вызываются на объекте-мониторе (том же, на котором захвачен `synchronized`),
а `join` — на объекте потока, завершения которого мы ждём.

### 5.1 `wait/notify`
Используются для координации потоков через монитор.

Правила:
1. Вызывать только внутри `synchronized` на том же объекте.
2. `wait()` освобождает монитор и переводит поток в ожидание.
3. `notify()` будит один произвольный поток, ожидающий на этом мониторе.
4. `notifyAll()` будит все потоки, ожидающие на этом мониторе (дальше они конкурируют за монитор).
5. Проверка условия — через `while`, а не `if`.

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
`Semaphore` — класс из `java.util.concurrent`, который хранит набор «разрешений» (permits)
и ограничивает число потоков, одновременно входящих в секцию.

- `acquire()` — пытается получить разрешение; если разрешений нет, поток блокируется до появления разрешения;
- `release()` — возвращает разрешение обратно в семафор (обычно в `finally`).

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
`Lock` — это интерфейс синхронизации (пакет `java.util.concurrent.locks`).
`ReentrantLock` — его самая часто используемая реализация: reentrant означает, что поток,
уже удерживающий lock, может захватить его повторно без deadlock.

Почему обычно акцент именно на `ReentrantLock`:
- это прямой и понятный аналог `synchronized` с расширенными возможностями;
- широко используется в прикладном коде и в учебных примерах.

Но это не единственный вариант:
- есть и другие реализации/связанные примитивы (`ReentrantReadWriteLock.ReadLock/WriteLock`, `StampedLock` и др.)
  для более специализированных сценариев.

Базовые методы:
- `lock()` — блокирующе захватывает lock;
- `unlock()` — освобождает lock (обязательно в `finally`);
- `tryLock()` — пытается захватить lock без ожидания (или с таймаутом в перегрузке);
- `lockInterruptibly()` — как `lock`, но ожидание можно прервать через interrupt.

Про «несколько `Condition`»:
- у `synchronized`/монитора фактически один набор ожидания на объект;
- у `ReentrantLock` можно создать несколько отдельных `Condition` (`notEmpty`, `notFull` и т.д.)
  и будить только нужную группу ожидающих потоков.

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

Это класс из `java.util.concurrent` для сценария «дождаться завершения N событий».
Важная особенность: latch одноразовый — после достижения нуля его нельзя «сбросить».

```java
CountDownLatch latch = new CountDownLatch(3);

// в рабочих потоках
latch.countDown();

// в ожидающем потоке
latch.await();
```

Более развёрнутый пример (ждём инициализацию трёх сервисов перед стартом обработки):
```java
CountDownLatch ready = new CountDownLatch(3);

Runnable initTask = () -> {
    try {
        initService();                 // инициализация конкретного сервиса
    } finally {
        ready.countDown();             // сигнал: один сервис готов
    }
};

new Thread(initTask, "svc-1").start();
new Thread(initTask, "svc-2").start();
new Thread(initTask, "svc-3").start();

ready.await();                        // главный поток ждёт, пока все 3 завершат init
startServingRequests();               // безопасно принимать запросы
```

### 5.6 `CyclicBarrier` (Barrier)
`CyclicBarrier` — класс синхронизации, где фиксированная группа потоков
должна дойти до общей точки (`await()`), и только после этого все продолжают.

В отличие от `CountDownLatch`, барьер циклический: его можно использовать повторно по раундам.
Также можно задать `barrierAction` — действие, которое выполнится один раз при сборе всех участников.

```java
CyclicBarrier barrier = new CyclicBarrier(3);

// в каждом потоке
preparePart();
barrier.await();
mergePart();
```

Более развёрнутый пример (итеративные расчёты по шагам):
```java
int workers = 3;
CyclicBarrier barrier = new CyclicBarrier(workers,
        () -> System.out.println("Все потоки завершили шаг, переходим к следующему"));

Runnable worker = () -> {
    for (int step = 1; step <= 5; step++) {
        computeStep(step);             // каждый поток делает свой кусок шага
        try {
            barrier.await();           // ждём остальных участников этого шага
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }
};

new Thread(worker, "w1").start();
new Thread(worker, "w2").start();
new Thread(worker, "w3").start();
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
