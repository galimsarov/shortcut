# Hibernate / JPA — Part 2 (вопросы 8–14)

> Конспект по темам 8–14: связи, каскады, кэширование, генерация ID, блокировки, N+1 и EntityListeners.

---

## 8. Связи между сущностями

### Какие бывают связи

В JPA есть 4 базовых типа связей (сопоставим «человеческое» название и аннотации):

1. **Один к одному** → `@OneToOne`
2. **Один ко многим** → `@OneToMany`
3. **Многие к одному** → `@ManyToOne`
4. **Многие ко многим** → `@ManyToMany`

Ниже — не только названия сущностей, но и структура таблиц (PK/FK), чтобы было понятно, как связь живет в БД.

#### 1) Один к одному (`@OneToOne`)

Пример: `users` ↔ `profiles` (у пользователя один профиль, у профиля один пользователь).

**Таблицы:**
- `users(id PK, email, ...)`
- `profiles(id PK, user_id FK UNIQUE -> users.id, first_name, last_name, ...)`

`UNIQUE` на `profiles.user_id` обеспечивает именно 1:1 на уровне БД.

**Классы (идея):**
- `User { Long id; Profile profile; }`
- `Profile { Long id; User user; }`

#### 2) Один ко многим / многие к одному (`@OneToMany` + `@ManyToOne`)

Пример: `departments` ↔ `employees` (в одном департаменте много сотрудников, у сотрудника один департамент).

**Таблицы:**
- `departments(id PK, name, ...)`
- `employees(id PK, department_id FK -> departments.id, full_name, ...)`

FK лежит в таблице «многие» (`employees`), поэтому именно она физически хранит связь.

**Классы (идея):**
- `Department { Long id; List<Employee> employees; }`
- `Employee { Long id; Department department; }`

#### 3) Многие ко многим (`@ManyToMany`)

Пример: `students` ↔ `courses`.

**Таблицы:**
- `students(id PK, name, ...)`
- `courses(id PK, title, ...)`
- `student_course(student_id FK -> students.id, course_id FK -> courses.id, PK(student_id, course_id))`

Связь хранится в отдельной таблице-связке (`join table`).

**Классы (идея):**
- `Student { Long id; Set<Course> courses; }`
- `Course { Long id; Set<Student> students; }`

> Практический совет: если у связи появляются свои поля (`enrolled_at`, `grade`, `status`), почти всегда лучше не `@ManyToMany`, а отдельная сущность (например, `Enrollment`) и две связи `@ManyToOne`.

### Однонаправленные и двунаправленные (bidirectional) связи

Перед `mappedBy` важно договориться о терминах:

- **Unidirectional (однонаправленная)**: только одна сущность «знает» о другой.  
  Пример: `Order -> Customer` (в `Order` есть поле `customer`, в `Customer` нет коллекции заказов).
- **Bidirectional (двунаправленная)**: обе сущности ссылаются друг на друга.  
  Пример: `Order -> Customer` и `Customer -> List<Order>`.

Альтернатива bidirectional — оставить связь однонаправленной и получать обратную сторону через запрос (`select o from Order o where o.customer.id = :id`).

Когда что выбирать:
- **Unidirectional** — проще модель, меньше риска рассинхронизации двух сторон в памяти, удобна когда обратная навигация редко нужна.
- **Bidirectional** — удобнее навигация в коде и бизнес-логиках «туда-обратно», но нужно поддерживать консистентность обеих сторон (например, helper-методами `addOrder/removeOrder`).

### Владелец связи (owning side) и `mappedBy`

Ключевая идея: **только owning side управляет внешним ключом или записью в join table**.

Важно: термин **owning side** — это термин JPA/Hibernate (ORM-мэппинга), а не «официальный термин БД».  
Однако он напрямую связан со структурой БД: owning side — это сторона, через которую ORM записывает FK/строки в таблицу связи.

Как понять, кто owner:

1. В bidirectional `@OneToMany/@ManyToOne` owner почти всегда сторона `@ManyToOne`, где стоит `@JoinColumn`.
2. Сторона с `mappedBy = "..."` — **не owner** (inverse side, read/navigation side).
3. В `@ManyToMany` owner — сторона без `mappedBy` (обычно та, где объявлен `@JoinTable`).
4. В unidirectional связи с `@JoinColumn` owner очевиден: это единственная сторона, где описана колонка связи.

```java
@Entity
class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}

@Entity
class Customer {
    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();
}
```

В примере выше:
- owner = `Order.customer` (там `@JoinColumn(name = "customer_id")`);
- inverse side = `Customer.orders` (там `mappedBy = "customer"`).

На уровне таблиц это видно так: FK `orders.customer_id` физически хранится в `orders`.

### Lazy / Eager загрузка

#### Значения по умолчанию (важно помнить)
- Для `@ManyToOne` и `@OneToOne` дефолт в JPA — **`EAGER`**.
- Для `@OneToMany` и `@ManyToMany` дефолт — **`LAZY`**.

Практический совет: почти всегда лучше явно указывать fetch-стратегию и чаще выбирать `LAZY`, чтобы избежать лишних запросов и раздувания графа объектов.

```java
@ManyToOne(fetch = FetchType.LAZY)
private Customer customer;
```

#### Что такое LAZY
Ассоциация загружается **по требованию** при первом обращении к полю/коллекции. В Hibernate это часто прокси/перехватчик.

Плюсы:
- меньше данных читается «на старте»;
- лучше контролируется производительность.

Риски:
- `LazyInitializationException`, если обращение к lazy-полю произошло вне открытой сессии/транзакции.

#### Что такое EAGER
Связь подгружается сразу при загрузке сущности (либо join, либо дополнительными select — зависит от запроса и провайдера).

Минусы:
- неожиданные дополнительные SQL;
- риск каскадного роста графа и деградации производительности.

#### Пример: что реально загрузится (без Spring, чистый JPA/Hibernate)

```java
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

Order order = em.find(Order.class, 1L); // SQL к orders
System.out.println(order.getId());

// customer LAZY: до этого момента customer не загружался
System.out.println(order.getCustomer().getName()); // дополнительный SQL к customers

em.getTransaction().commit();
em.close();
```

Если сделать `@ManyToOne(fetch = FetchType.EAGER)`, провайдер обычно подтянет `customer` сразу (join или доп. select при find/load).

#### `LazyInitializationException`: отдельный пример

Типичный сценарий ошибки:

```java
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

Customer customer = em.find(Customer.class, 10L); // orders = LAZY

em.getTransaction().commit();
em.close(); // persistence context закрыт

// Ошибка: коллекция orders не может инициализироваться вне сессии
System.out.println(customer.getOrders().size()); // LazyInitializationException
```

Как избежать (без Spring):

1. **Инициализировать внутри транзакции**, пока `EntityManager` открыт:
   - обратиться к коллекции (`customer.getOrders().size()`),
   - или выполнить `JOIN FETCH` запрос.
2. **Сразу читать DTO проекцией**, а не тянуть ленивые сущности за границу транзакции.
3. **Точечно использовать fetch join в запросе**, а не глобально переводить всё в `EAGER`.

Пример с `JOIN FETCH`:

```java
Customer customer = em.createQuery(
        "select c from Customer c left join fetch c.orders where c.id = :id",
        Customer.class)
    .setParameter("id", 10L)
    .getSingleResult();

em.close();
// orders уже загружены запросом, коллекция доступна
System.out.println(customer.getOrders().size());
```

### Аннотации связей и важные параметры

Сопоставление с типами связей из начала пункта:
- **Один к одному** → `@OneToOne`
- **Один ко многим** → `@OneToMany`
- **Многие к одному** → `@ManyToOne`
- **Многие ко многим** → `@ManyToMany`

#### `@OneToOne`
Частые параметры:
- `fetch` — стратегия загрузки (`LAZY`/`EAGER`).  
  Рекомендация: чаще `LAZY`, `EAGER` только если связь нужна почти всегда.
- `optional` — можно ли `null` в объектной модели.  
  Обычно `false`, если в бизнес-правиле профиль обязателен.
- `mappedBy` — имя поля-владельца на другой стороне (для inverse side).
- `orphanRemoval` (`true/false`) — удалять ли «осиротевший» дочерний объект.
- `cascade` — кратко: какие операции распространять (подробно в п. 9).

```java
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
@JoinColumn(name = "profile_id", unique = true)
private Profile profile;
```

#### `@ManyToOne`
Частые параметры:
- `fetch` — часто явно ставят `LAZY`, чтобы не тащить «родителя» всегда.
- `optional` — обязателен ли родитель (`false` => связь обязательна).
- `@JoinColumn(name = "...", nullable = ..., foreignKey = ...)`:
  - `name` — имя FK-колонки;
  - `nullable` — допускается ли `NULL` в БД;
  - `foreignKey` — имя FK-констрейнта (полезно для читаемых миграций/DDL).
- `cascade` — обычно очень аккуратно; часто вообще не нужен на `ManyToOne`.

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

#### `@OneToMany`
Частые параметры:
- `mappedBy` — имя поля owner-стороны (почти всегда в bidirectional-модели).
- `fetch` — обычно оставляют `LAZY` (это и так дефолт).
- `orphanRemoval` — нужно ли удалять дочернюю сущность при удалении из коллекции.
- `cascade` — часто `PERSIST/MERGE`, иногда `ALL` в агрегатах.
- Тип коллекции:
  - `List` — если важен порядок/дубликаты;
  - `Set` — если нужна уникальность элементов.

```java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees = new ArrayList<>();
```

#### `@ManyToMany`
Чаще всего задают через `@JoinTable`:
- `name` — имя таблицы связи;
- `joinColumns` — FK на текущую сущность;
- `inverseJoinColumns` — FK на вторую сущность;
- `mappedBy` — на обратной стороне bidirectional-связи.

```java
@ManyToMany
@JoinTable(
    name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id")
)
private Set<Course> courses = new HashSet<>();
```

> На практике для `@ManyToMany` нередко лучше вводить отдельную сущность-связку (например `Enrollment`), чтобы хранить атрибуты связи и лучше управлять жизненным циклом.
>
> Про каскады здесь — кратко; детальный разбор см. в п. 9.

---

## 9. Каскады

`CascadeType` определяет, какие операции над родительской сущностью автоматически применяются к связанным сущностям.

Основные типы:
- `PERSIST` — при `persist` родителя сохраняются дети.
- `MERGE` — при `merge` родителя мержатся дети.
- `REMOVE` — удаление родителя удаляет детей.
- `REFRESH` — обновление состояния родителя из БД обновляет детей.
- `DETACH` — `detach` распространяется на детей.
- `ALL` — все выше.

Пример:

```java
@OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

### `orphanRemoval` vs `CascadeType.REMOVE`

- `CascadeType.REMOVE`: удалит детей, когда удаляют родителя.
- `orphanRemoval = true`: удалит «осиротевшего» ребенка, когда его убрали из коллекции у родителя.

Это разные механики, часто применяются вместе.

### Осторожно с каскадами

- Не ставьте бездумно `CascadeType.ALL` на `@ManyToOne`/`@ManyToMany`.
- Каскад удаления может снести связанные данные в неожиданных местах.
- Каскады должны отражать **агрегатные границы** (DDD-подход): кто реально владеет жизненным циклом кого.

---

## 10. Кэширование

### Первый уровень кэша (L1, persistence context)

- Есть всегда, встроен в `Session`/`EntityManager`.
- Живет в рамках одной сессии.
- Если в рамках сессии дважды запрашиваем одну и ту же сущность по ID, второй раз БД обычно не дергается.

```java
User u1 = em.find(User.class, 10L);
User u2 = em.find(User.class, 10L); // из L1-кэша
```

### Второй уровень кэша (L2)

- Общий между сессиями (на уровне `SessionFactory`).
- Не включен «автоматически для всего»: требуется конфигурация провайдера кэша (Ehcache, Caffeine/JCache, Infinispan и т.п.) и пометки сущностей.

Пример:

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(
    usage = CacheConcurrencyStrategy.READ_WRITE,
    region = "users"
)
class User { ... }
```

Типичные стратегии конкурентности:
- `READ_ONLY` — для неизменяемых данных;
- `NONSTRICT_READ_WRITE` — допускает кратковременную нестрогую консистентность;
- `READ_WRITE` — компромисс между консистентностью и производительностью;
- `TRANSACTIONAL` — при поддержке транзакционного кэша.

### Кэш запросов (Query Cache)

Hibernate умеет кэшировать **результат запроса** (обычно набор ID/скаляров), но:
- кэш запросов обычно работает эффективно вместе с L2;
- нужно явно включать и помечать конкретные запросы как cacheable.

```java
List<User> users = em.createQuery(
        "select u from User u where u.status = :status", User.class)
    .setParameter("status", Status.ACTIVE)
    .setHint("org.hibernate.cacheable", true)
    .getResultList();
```

Важно:
- любой апдейт таблиц, затронутых запросом, может инвалидировать query cache;
- кэш запросов полезен для часто повторяемых, относительно стабильных выборок.

---

## 11. Типы автоматической генерации ID

В JPA используется `@GeneratedValue(strategy = ...)`.

Основные стратегии:

1. **`GenerationType.IDENTITY`**
   - ID генерируется БД при `INSERT` (auto-increment/identity column).
   - Просто настроить, но хуже для batch insert в ряде сценариев.

2. **`GenerationType.SEQUENCE`**
   - Использует sequence в БД.
   - Обычно предпочтительнее для PostgreSQL/Oracle.
   - Можно тонко настраивать через `@SequenceGenerator` (`allocationSize` сильно влияет на производительность).

3. **`GenerationType.TABLE`**
   - Табличный генератор ID.
   - Универсален, но чаще медленнее и используется редко.

4. **`GenerationType.AUTO`**
   - Провайдер сам выбирает стратегию в зависимости от диалекта/БД.

Пример с sequence:

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
@SequenceGenerator(
    name = "user_seq_gen",
    sequenceName = "user_seq",
    allocationSize = 50
)
private Long id;
```

---

## 12. Оптимистические и пессимистические блокировки

### Понятие блокировок

Блокировки нужны, чтобы корректно обрабатывать конкурентные изменения данных (когда несколько транзакций читают/пишут одни и те же строки).

### Оптимистическая блокировка

Идея: «конфликты редки». Не блокируем строку заранее, а проверяем на коммите, что никто не изменил запись между чтением и записью.

Обычно реализуется через поле версии:

```java
@Version
private Long version;
```

При `UPDATE` Hibernate добавляет условие по версии. Если обновлено 0 строк — значит была конкурентная модификация (обычно кидается `OptimisticLockException`).

Когда подходит:
- много чтений, мало конфликтующих записей;
- web-приложения с короткими транзакциями.

### Пессимистическая блокировка

Идея: «конфликты вероятны». Блокируем строку на уровне БД во время транзакции.

В JPA:
- `LockModeType.PESSIMISTIC_READ` (аналогично `FOR SHARE`/shared lock в ряде БД),
- `LockModeType.PESSIMISTIC_WRITE` (аналог `FOR UPDATE`),
- `LockModeType.PESSIMISTIC_FORCE_INCREMENT`.

```java
Order order = em.find(Order.class, id, LockModeType.PESSIMISTIC_WRITE);
```

Или через query:

```java
Order order = em.createQuery(
        "select o from Order o where o.id = :id", Order.class)
    .setParameter("id", id)
    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
    .getSingleResult();
```

### `FOR UPDATE` и `FOR SHARE`

Это SQL-конструкции уровня БД:
- `FOR UPDATE` — эксклюзивная блокировка строк на запись;
- `FOR SHARE` (или эквиваленты в конкретной БД) — разделяемая блокировка на чтение.

Hibernate/JPA обычно транслирует lock mode в соответствующий SQL диалекта БД.

### Практические замечания

- Пессимистические блокировки увеличивают риск deadlock и снижают параллелизм.
- Нужны короткие транзакции и аккуратный порядок доступа к данным.
- Оптимистическая блокировка проще масштабируется, но требует корректной обработки retry/ошибок.

---

## 13. Проблема N+1 и способы решения

### Что такое N+1

Сценарий:
1. Один запрос получает N родительских сущностей.
2. Затем для каждой сущности делается отдельный запрос к связанной сущности/коллекции.

Итого: `1 + N` запросов вместо 1–2.

### Типичный пример

Получили 100 `Order`, потом в цикле обращаемся к `order.getCustomer().getName()` при lazy-связи — получаем 100 дополнительных select.

### Как решать

1. **`JOIN FETCH` в JPQL/HQL**

```java
List<Order> orders = em.createQuery(
    "select o from Order o join fetch o.customer where o.status = :status", Order.class)
  .setParameter("status", Status.NEW)
  .getResultList();
```

2. **`@EntityGraph`** — декларативно описать, что подгружать в конкретном use-case.

3. **Batch fetching** (`@BatchSize`, глобальные настройки batch fetch size).
   - Вместо 100 одиночных запросов получить, например, 5 запросов по 20 элементов.

4. **`FetchMode.SUBSELECT`** (Hibernate-специфично) — для некоторых коллекций может сильно снизить число запросов.

5. **DTO-проекции**
   - Когда нужен read-only сценарий, лучше сразу выбрать только нужные поля, чем тянуть граф сущностей.

Важно: глобальное переключение всего на `EAGER` — плохое «решение», обычно приводит к другим проблемам производительности.

---

## 14. EntityListeners

`EntityListeners` — механизм колбэков жизненного цикла сущности.

События (основные):
- `@PrePersist`, `@PostPersist`
- `@PreUpdate`, `@PostUpdate`
- `@PreRemove`, `@PostRemove`
- `@PostLoad`

Можно определять:
1. В самой сущности (методы с аннотациями).
2. Во внешнем listener-классе и подключать через `@EntityListeners(...)`.

Пример:

```java
@Entity
@EntityListeners(AuditListener.class)
class User {
    @Id
    private Long id;

    private Instant createdAt;
    private Instant updatedAt;
}

public class AuditListener {
    @PrePersist
    public void onCreate(User user) {
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
    }

    @PreUpdate
    public void onUpdate(User user) {
        user.setUpdatedAt(Instant.now());
    }
}
```

### Где полезно

- аудит технических полей (`createdAt`, `updatedAt`);
- нормализация/валидация данных перед сохранением;
- инкапсуляция повторяемой инфраструктурной логики.

### Ограничения и практика

- Не стоит помещать тяжелую бизнес-логику в listener.
- Нужно помнить о порядке вызовов и контексте транзакции.
- Для сложного аудита часто используют Hibernate Envers или отдельные доменные/инфраструктурные механизмы.

---

## Краткий итог по части 2

- Связи и fetch-стратегии — главный источник как удобства, так и проблем производительности.
- Каскады и `orphanRemoval` нужно настраивать по границам владения.
- L1/L2/query cache помогают ускорять чтение, но требуют аккуратной стратегии инвалидации.
- Выбор генерации ID влияет на batching и throughput.
- Блокировки (`@Version` и pessimistic lock modes) — ключ к корректной конкурентной работе.
- N+1 решается точечными fetch-подходами под конкретный use-case.
- EntityListeners удобны для lifecycle-инфраструктуры, но не для тяжёлой бизнес-логики.
