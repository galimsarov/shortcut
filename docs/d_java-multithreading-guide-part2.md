# Java Multithreading — Part 2 (темы 9–16)

> Продолжение первой части. Здесь закрываем темы 9–16 из вашего плана.

## План части 2 (8 тем)
9. Приоритеты потоков, потоки-демоны  
10. Прерывание потока  
11. `Future` / `CompletableFuture`  
12. Проблемы многопоточности: race condition, дедлок, лайвлок, data race, starvation  
13. `ForkJoinPool`, связь со Stream API  
14. Синхронизированные коллекции: `Concurrent`-аналоги, `CopyOnWriteArrayList`, `BlockingQueue`  
15. `ThreadLocal` переменные  
16. `Exchanger`

---

## 9) Приоритеты потоков и потоки-демоны

### Приоритеты потоков (`Thread#setPriority`)
У потока в Java есть приоритет от `1` до `10`:
- `Thread.MIN_PRIORITY = 1`
- `Thread.NORM_PRIORITY = 5`
- `Thread.MAX_PRIORITY = 10`

```java
Thread t = new Thread(() -> {
    // работа
});
t.setPriority(Thread.MAX_PRIORITY);
t.start();
```

Важно понимать: приоритет — это **подсказка планировщику**, а не жёсткая гарантия.

- На разных ОС и JVM эффект может отличаться.
- Высокий приоритет не гарантирует «всегда раньше всех».
- В современном коде рассчитывать на приоритеты как на механизм синхронизации — плохая идея.

Практический вывод: приоритеты почти не используют в бизнес-логике; лучше применять `ExecutorService`, ограничение пула и корректные примитивы синхронизации.

### Потоки-демоны (`setDaemon(true)`)
**Daemon thread** — фоновый поток, который не удерживает JVM «живой».

Если в JVM остались только daemon-потоки, процесс завершается.

```java
Thread daemon = new Thread(() -> {
    while (true) {
        // фоновая housekeeping-задача
    }
});
daemon.setDaemon(true);
daemon.start();
```

Ключевые правила:
- `setDaemon(true)` нужно вызывать **до** `start()`, иначе `IllegalThreadStateException`.
- daemon-поток может быть прерван завершением JVM в любой момент, поэтому критичные данные (например, запись важного состояния) лучше не поручать только ему.
- типичные daemon-задачи: мониторинг, фоновые сервисные операции, периодическая чистка кэша.

---

## 10) Прерывание потока (`interrupt`)

Прерывание — кооперативный механизм «вежливой остановки».

Один поток выставляет другому флаг прерывания:
```java
thread.interrupt();
```

Дальше сценарии:
1. Если поток блокирован в `sleep()/wait()/join()` или ряде блокирующих операций — обычно выбрасывается `InterruptedException`.
2. Если поток активно крутит цикл, он должен сам проверять флаг через `Thread.currentThread().isInterrupted()`.

Пример корректной обработки:
```java
Runnable task = () -> {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            // полезная работа
            Thread.sleep(200);
        }
    } catch (InterruptedException e) {
        // Восстанавливаем статус, чтобы верхний уровень увидел прерывание
        Thread.currentThread().interrupt();
    } finally {
        // cleanup
    }
};
```

Почему важно «восстановить» interrupt status в `catch`:
- `InterruptedException` очищает флаг прерывания;
- если просто проглотить исключение, внешний код может решить, что прерывания не было.

Антипаттерн:
- ловить `InterruptedException` и молча игнорировать.

Корректный подход:
- либо пробросить исключение выше,
- либо восстановить флаг (`Thread.currentThread().interrupt()`) и завершить текущую задачу.

---

## 11) `Future` и `CompletableFuture`

### `Future`
`Future<T>` — это «обещание» результата асинхронной задачи.

Типичный путь: `ExecutorService.submit(Callable<T>)`.

```java
ExecutorService pool = Executors.newFixedThreadPool(2);
Future<Integer> f = pool.submit(() -> 40 + 2);

Integer result = f.get(); // блокирует до готовности
pool.shutdown();
```

Что умеет `Future`:
- `get()` — получить результат (возможно с блокировкой);
- `get(timeout, unit)` — получить с таймаутом;
- `cancel(mayInterruptIfRunning)` — попытаться отменить;
- `isDone()`, `isCancelled()`.

Ограничения классического `Future`:
- слабая композиция (сложно «склеивать» несколько шагов);
- callback-стиля почти нет;
- обработка ошибок ограничена обёрткой в `ExecutionException`.

### `CompletableFuture`
`CompletableFuture<T>` решает проблему композиции асинхронных стадий.

Пример цепочки:
```java
CompletableFuture<String> cf = CompletableFuture
        .supplyAsync(() -> "42")
        .thenApply(Integer::parseInt)
        .thenApply(x -> x * 2)
        .thenApply(x -> "Result=" + x)
        .exceptionally(ex -> "fallback");

String out = cf.join(); // join бросает unchecked CompletionException
```

Частые методы:
- `thenApply` — преобразовать результат;
- `thenCompose` — «плоская» композиция async->async;
- `thenCombine` — объединить результаты двух независимых futures;
- `allOf` / `anyOf` — ожидание группы задач;
- `handle`, `exceptionally`, `whenComplete` — обработка ошибок/финализация.

Про `join()` vs `get()`:
- `get()` — checked exceptions (`InterruptedException`, `ExecutionException`);
- `join()` — unchecked (`CompletionException`), удобен в functional-цепочках.

Важный нюанс: по умолчанию async-методы часто используют `ForkJoinPool.commonPool()`. В нагруженном сервисе лучше явно передавать свой `Executor`.

---

## 12) Типичные проблемы многопоточности

### 12.1 Race condition (состояние гонки)
Общее определение: итог программы зависит от непредсказуемого порядка выполнения потоков.

Наглядный пример: два потока инкрементируют один `int` без синхронизации.

```java
class Counter {
    int value = 0;
}

Counter c = new Counter();

Thread t1 = new Thread(() -> {
    for (int i = 0; i < 100_000; i++) c.value++; // не атомарно
});
Thread t2 = new Thread(() -> {
    for (int i = 0; i < 100_000; i++) c.value++;
});

t1.start();
t2.start();
t1.join();
t2.join();

System.out.println(c.value); // часто < 200_000
```

Операция распадается на read-modify-write, и часть обновлений теряется.

Способ решения на Java:

```java
AtomicInteger safe = new AtomicInteger(0);

Thread t1 = new Thread(() -> {
    for (int i = 0; i < 100_000; i++) safe.incrementAndGet();
});
Thread t2 = new Thread(() -> {
    for (int i = 0; i < 100_000; i++) safe.incrementAndGet();
});

t1.start();
t2.start();
t1.join();
t2.join();

System.out.println(safe.get()); // 200_000
```

Альтернативы: `synchronized`, `ReentrantLock`, `LongAdder` (для высококонкурентных счётчиков).

### 12.2 Data race
Более формально (в духе JMM): два потока одновременно обращаются к одной переменной, хотя бы один доступ — запись, и между доступами нет happens-before отношения.

Следствие — неопределённая видимость/поведение (в рамках JMM допускаются неожиданные результаты).

Связь терминов:
- `data race` — формальное свойство доступа к памяти;
- `race condition` — более широкий класс логических гонок в конкурентном коде.

Наглядный пример data race (проблема видимости):

```java
class FlagHolder {
    boolean ready = false; // нет volatile
}

FlagHolder holder = new FlagHolder();

Thread reader = new Thread(() -> {
    while (!holder.ready) {
        // busy-wait
    }
    System.out.println("reader observed ready=true");
});

Thread writer = new Thread(() -> holder.ready = true);

reader.start();
writer.start();
```

Поток `reader` может слишком долго не увидеть изменение из-за отсутствия корректной публикации.

Способ решения на Java:

```java
class FlagHolder {
    volatile boolean ready = false;
}
```

Альтернативы: чтение/запись под одним lock-ом (`synchronized`/`Lock`), либо использование `AtomicBoolean`.

### 12.3 Deadlock (взаимная блокировка)
Два или более потока навсегда ждут ресурсы друг друга.

Классика:
- `T1` держит `lockA`, ждёт `lockB`;
- `T2` держит `lockB`, ждёт `lockA`.

Наглядный пример:

```java
Object lockA = new Object();
Object lockB = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lockA) {
        sleep(100);
        synchronized (lockB) {
            System.out.println("t1 done");
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lockB) {
        sleep(100);
        synchronized (lockA) {
            System.out.println("t2 done");
        }
    }
});
```

Здесь оба потока могут зависнуть навсегда.

Минимизация риска:
- единый порядок захвата lock-ов;
- таймауты (`tryLock(timeout, unit)`);
- уменьшение числа вложенных блокировок;
- как можно меньше shared mutable state.

Способ решения на Java (фиксированный порядок lock-ов):

```java
Object first = lockA;
Object second = lockB;

Thread t1 = new Thread(() -> {
    synchronized (first) {
        synchronized (second) {
            // ...
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (first) {
        synchronized (second) {
            // ...
        }
    }
});
```

Оба потока берут блокировки в одном порядке, и цикл ожидания не возникает.

### 12.4 Livelock (лайвлок)
Потоки не заблокированы, но «слишком вежливо» уступают друг другу и не делают прогресс.

То есть активность есть, а полезного продвижения нет.

Наглядный пример (упрощённо):

```java
class Worker {
    private final String name;
    private boolean active = true;

    Worker(String name) { this.name = name; }

    void work(Worker other, SharedResource resource) {
        while (active) {
            if (resource.owner != this) {
                Thread.yield();
                continue;
            }
            if (other.active) {
                System.out.println(name + ": you go first");
                resource.owner = other; // оба постоянно уступают
                continue;
            }
            resource.use();
            active = false;
            resource.owner = other;
        }
    }
}
```

Способ решения на Java:
- добавить случайную задержку (`ThreadLocalRandom`) перед повторной попыткой;
- ограничить количество «уступок»;
- использовать более строгий протокол синхронизации (`Lock` + очередь/condition).

Мини-пример «backoff»:

```java
int retries = 0;
while (!lock.tryLock()) {
    TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 10));
    if (++retries > 100) throw new IllegalStateException("too much contention");
}
```

### 12.5 Starvation (голодание)
Поток слишком долго не получает CPU/lock/ресурс из-за несправедливого планирования или постоянной конкуренции.

Причины:
- неудачная политика планирования;
- перекос в приоритетах;
- «вечно занятая» критическая секция.

Практика профилактики:
- делать критические секции короткими;
- использовать справедливые блокировки там, где это реально нужно (`new ReentrantLock(true)` — с оговоркой по throughput);
- грамотно ограничивать конкуренцию через пулы/очереди.

Наглядный пример (условно):

```java
ReentrantLock lock = new ReentrantLock(); // по умолчанию unfair

Runnable greedy = () -> {
    while (true) {
        lock.lock();
        try {
            // очень короткая работа, но поток постоянно перезахватывает lock
        } finally {
            lock.unlock();
        }
    }
};

Runnable unlucky = () -> {
    while (true) {
        lock.lock();
        try {
            System.out.println("I finally got lock");
            break;
        } finally {
            lock.unlock();
        }
    }
};
```

`unlucky` может ждать непропорционально долго.

Способ решения на Java:

```java
ReentrantLock fairLock = new ReentrantLock(true); // fair policy
```

Дополнительно помогают bounded-очереди задач (`BlockingQueue`) и отказ от бесконечных «жадных» циклов.

---

## 13) `ForkJoinPool` и связь со Stream API

`ForkJoinPool` оптимизирован для задач типа divide-and-conquer:
- большая задача рекурсивно дробится на подзадачи (`fork`),
- затем результаты объединяются (`join`).

Ключевая идея производительности — **work-stealing**:
- у каждого worker-потока своя deque,
- простаивающий поток «крадёт» задачи у других.

Пример через `RecursiveTask`:
```java
class SumTask extends RecursiveTask<Long> {
    private final long[] arr;
    private final int l, r;
    private static final int THRESHOLD = 10_000;

    SumTask(long[] arr, int l, int r) {
        this.arr = arr;
        this.l = l;
        this.r = r;
    }

    @Override
    protected Long compute() {
        if (r - l <= THRESHOLD) {
            long s = 0;
            for (int i = l; i < r; i++) s += arr[i];
            return s;
        }
        int m = (l + r) >>> 1;
        SumTask left = new SumTask(arr, l, m);
        SumTask right = new SumTask(arr, m, r);
        left.fork();
        long rightRes = right.compute();
        long leftRes = left.join();
        return leftRes + rightRes;
    }
}
```

### Как это связано с `parallelStream()`
Параллельные стримы обычно выполняются в `ForkJoinPool.commonPool()`.

```java
long sum = list.parallelStream()
        .mapToLong(Long::longValue)
        .sum();
```

Практические замечания:
- `parallelStream()` полезен на CPU-bound задачах и больших объёмах данных;
- может деградировать на маленьких коллекциях из-за overhead;
- плохо сочетается с блокирующим I/O (можно «забить» commonPool).

Если нужна предсказуемая изоляция ресурсов, лучше использовать собственный `ForkJoinPool` или иной `Executor`.

---

## 14) Синхронизированные коллекции и concurrent-структуры

Есть два больших подхода:

1. **Старые synchronized-обёртки** (`Collections.synchronizedList/map/...`)  
2. **Коллекции из `java.util.concurrent`** (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `BlockingQueue` и др.)

### 14.1 `Concurrent`-аналоги
Примеры замен:
- `Hashtable` / `synchronizedMap` → `ConcurrentHashMap`
- `synchronizedList(new ArrayList<>())` → часто лучше `CopyOnWriteArrayList` или иная структура по профилю доступа
- ручная очередь + `wait/notify` → `BlockingQueue`

Почему `ConcurrentHashMap` обычно лучше `synchronizedMap`:
- выше параллелизм доступа;
- неблокирующие/слабо блокирующие алгоритмы внутри (в зависимости от операции);
- атомарные методы `compute`, `merge`, `putIfAbsent`.

### 14.2 `CopyOnWriteArrayList`
Идея: при каждой модификации создаётся новая копия массива.

Плюсы:
- очень быстрое чтение и безопасная итерация без `ConcurrentModificationException`;
- удобно для read-mostly сценариев (например, список listeners).

Минусы:
- дорогие записи (копирование массива);
- лишние аллокации и pressure на GC при частых изменениях.

### 14.3 `BlockingQueue`
`BlockingQueue` — очередь, где операции могут блокироваться:
- `put()` ждёт место, если очередь заполнена;
- `take()` ждёт элемент, если очередь пуста.

Классический producer-consumer:
```java
BlockingQueue<String> q = new ArrayBlockingQueue<>(100);

Thread producer = new Thread(() -> {
    try {
        q.put("task");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

Thread consumer = new Thread(() -> {
    try {
        String item = q.take();
        // обработка
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});
```

Популярные реализации:
- `ArrayBlockingQueue` — bounded, на массиве;
- `LinkedBlockingQueue` — linked-структура, может быть bounded/unbounded;
- `SynchronousQueue` — без буфера, передача «из рук в руки».

---

## 15) `ThreadLocal` переменные

`ThreadLocal<T>` даёт отдельное значение переменной **для каждого потока**.

```java
ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);

local.set(local.get() + 1);
int v = local.get();
local.remove();
```

Когда полезно:
- хранить per-thread контекст (например, traceId, форматтеры, временные буферы);
- избегать синхронизации для truly-thread-confined данных.

Риски и нюансы:
- в пулах потоков значения могут «утекать» между задачами, если забыть `remove()`;
- не подходит как глобальное хранилище состояния запроса без дисциплины очистки;
- усложняет тестирование и понимание неявных зависимостей.

Правило: в серверном коде с thread pools обычно ставят `try/finally` и обязательно очищают `ThreadLocal`.

---

## 16) `Exchanger`

`Exchanger<T>` — синхронизационная точка, где **два потока обмениваются данными**.

Оба потока вызывают `exchange(...)`; каждый получит объект другого.

```java
Exchanger<String> exchanger = new Exchanger<>();

Thread t1 = new Thread(() -> {
    try {
        String fromT2 = exchanger.exchange("from T1");
        System.out.println("T1 got: " + fromT2);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

Thread t2 = new Thread(() -> {
    try {
        String fromT1 = exchanger.exchange("from T2");
        System.out.println("T2 got: " + fromT1);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});
```

Особенности:
- если второй участник не пришёл, первый будет ждать (или можно использовать версию с таймаутом);
- хорошо подходит для парной синхронизации стадий пайплайна;
- используется реже, чем `BlockingQueue`, но полезен для сценариев «обмен буферами» между двумя рабочими потоками.

---

## Мини-шпаргалка по выбору инструмента

- Нужен результат одной асинхронной задачи → `Future`
- Нужна композиция async-шагов/пайплайн → `CompletableFuture`
- Producer-consumer, backpressure → `BlockingQueue`
- Много чтений, мало записей списка → `CopyOnWriteArrayList`
- Параллельный расчёт на divide-and-conquer → `ForkJoinPool`
- Пер-поточный контекст → `ThreadLocal` (с обязательным `remove()` в пулах)
- Парный обмен данными между двумя потоками → `Exchanger`

Если хотите, следующим шагом могу сделать «Part 3: 30+ практических задач и мини-кейсов по всем 16 темам» с прогрессией от easy к hard.
