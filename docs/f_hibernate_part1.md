# Hibernate / JPA — Part 1 (вопросы 1–7)

> Конспект по первым 7 темам: от ORM и базовых интерфейсов до Persistence Context, Entity и специальных видов маппинга.

---

## 1. Концепция ORM. Hibernate, JPA

### Что такое ORM
**ORM (Object-Relational Mapping)** — подход, который связывает объектную модель приложения (классы, поля, связи) и реляционную модель БД (таблицы, колонки, внешние ключи).

Идея: работать в коде с объектами (`User`, `Order`), а не писать SQL для каждой операции вручную.

### Почему ORM уменьшает шаблонный JDBC-код (наглядно)

#### Что обычно нужно сделать в чистом JDBC
Для одного простого `SELECT user by id` обычно нужны шаги:
1. Открыть `Connection`.
2. Подготовить `PreparedStatement` с SQL.
3. Проставить параметры.
4. Выполнить `executeQuery()`.
5. Руками вычитать `ResultSet`.
6. Руками собрать объект `User` из колонок.
7. Закрыть ресурсы (или аккуратно обернуть в try-with-resources).

```java
String sql = "SELECT id, email, status FROM users WHERE id = ?";

try (Connection c = dataSource.getConnection();
     PreparedStatement ps = c.prepareStatement(sql)) {

    ps.setLong(1, userId);

    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setStatus(Status.valueOf(rs.getString("status")));
            return user;
        }
        return null;
    }
}
```

#### То же через JPA/Hibernate
```java
User user = entityManager.find(User.class, userId);
```

ORM берёт на себя connection/SQL mapping boilerplate, а разработчик работает с сущностью.

### Автоматический маппинг: таблица ↔ сущность

Пример таблицы:

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  email VARCHAR(320) NOT NULL UNIQUE,
  status VARCHAR(32) NOT NULL
);
```

Пример entity:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;
}
```

Соответствие:
- `users.id` ↔ `User.id`
- `users.email` ↔ `User.email`
- `users.status` ↔ `User.status`

### Жизненный цикл сущности: это термин ORM/JPA, а не SQL

Да, в SQL/реляционной БД нет понятий `managed`/`detached`.
Эти состояния существуют на уровне **ORM-провайдера** (Hibernate) и API **JPA**,
то есть в persistence context (`Session`/`EntityManager`).

Кратко:
1. **Transient** — новый объект в памяти, ORM его не отслеживает.
2. **Managed** — объект в persistence context, изменения ловятся через dirty checking.
3. **Detached** — объект был managed, но вышел из контекста.
4. **Removed** — объект помечен на удаление.

### JPA: это ORM или нет?

Корректнее так:
- **JPA — не отдельный ORM-фреймворк**, а **стандарт (спецификация) ORM для Java**.
- Реальные ORM-операции выполняет конкретная реализация (provider): Hibernate, EclipseLink и т.д.

### Какие ещё есть подходы/спецификации кроме JPA

- **Jakarta Data** (новее, более высокоуровневый подход к репозиториям; часто поверх JPA-провайдеров).
- Исторически существовали и vendor-specific API (например, чистый Hibernate API без JPA).
- Также есть **не-JPA ORM/mapper** инструменты (например, MyBatis — SQL mapper, а не классический ORM).

Почему JPA чаще выбирают:
- единый стандарт и переносимость кода между провайдерами;
- сильная экосистема (Spring Data JPA, большое количество материалов);
- удобство найма/поддержки: паттерны и API знакомы большинству Java-разработчиков.

### Hibernate относительно JPA: что ещё есть и почему Hibernate популярен

Другие популярные JPA-провайдеры:
- **EclipseLink**
- **OpenJPA**

Почему Hibernate особенно популярен:
- де-факто стандарт в большом числе enterprise/Spring-проектов;
- богатые возможности сверх JPA (тонкие настройки fetch/query/cache);
- зрелость, большая документация/комьюнити, много production-кейсов.

### Аналоги ORM-подхода (кратко)

- **Java:** EclipseLink, OpenJPA, MyBatis (частичный аналог: mapper-подход).
- **Python:** SQLAlchemy, Django ORM.
- **.NET:** Entity Framework.
- **Node.js:** TypeORM, Prisma (ближе к ORM/ORM-like).

---

## 2. Основные интерфейсы Hibernate/JPA

Ниже — ключевые интерфейсы с парным указанием: Hibernate-уровень / JPA-уровень.

### `Configuration` (Hibernate) / (в JPA напрямую обычно нет аналога)

`Configuration` — это bootstrap-объект Hibernate для **сборки метаданных**:
- читает настройки (`hibernate.cfg.xml` или programmatic properties);
- регистрирует entity-классы;
- строит внутреннюю модель маппинга;
- участвует в создании `SessionFactory`.

Что «под капотом» кратко:
- формируется `ServiceRegistry` (сервисы Hibernate: connection provider, dialect resolver и т.д.);
- строится `Metadata` (описание сущностей, таблиц, связей);
- на основе метаданных создаётся `SessionFactory`.

Про жизненный цикл настроек:
- да, это настройка фреймворка **до начала рабочей фазы**;
- после создания `SessionFactory` конфигурация считается фиксированной для этого экземпляра;
- для изменения ключевых настроек обычно пересоздают `SessionFactory` (на практике часто это означает перезапуск приложения).

Пример (обычное Java-приложение, без Spring Boot):

```java
Configuration cfg = new Configuration();

// 1) Загружаем базовую конфигурацию (hibernate.cfg.xml)
cfg.configure("hibernate.cfg.xml");

// 2) Регистрируем сущности (можно и через XML mapping)
cfg.addAnnotatedClass(User.class);
cfg.addAnnotatedClass(Order.class);

// 3) Строим ServiceRegistry и SessionFactory
StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .applySettings(cfg.getProperties())
        .build();

SessionFactory sessionFactory = cfg.buildSessionFactory(registry);
```

Разбор `configure()` и источников настроек:
- `new Configuration().configure()` — допустимый сокращённый вызов; Hibernate ищет дефолтный `hibernate.cfg.xml`.
- `cfg.configure("hibernate.cfg.xml")` — явное указание имени ресурса (тот же результат, если имя стандартное).
- По умолчанию `hibernate.cfg.xml` кладут в classpath (обычно `src/main/resources/hibernate.cfg.xml`), чтобы файл попал в корень classpath после сборки.
- Конфигурацию можно задавать и без XML:
  - через `hibernate.properties` в classpath;
  - программно (`cfg.setProperty(...)`, `StandardServiceRegistryBuilder.applySetting(...)`);
  - через JPA `persistence.xml` + свойства провайдера.

Мини-пример programmatic-конфигурации:

```java
Configuration cfg = new Configuration()
        .addAnnotatedClass(User.class)
        .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/app")
        .setProperty("hibernate.connection.username", "app")
        .setProperty("hibernate.connection.password", "secret")
        .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        .setProperty("hibernate.hbm2ddl.auto", "validate");
```

### `SessionFactory` (Hibernate) / `EntityManagerFactory` (JPA)

`SessionFactory` — тяжёлый потокобезопасный объект уровня приложения.

Что «под капотом» кратко:
- хранит метаданные сущностей и SQL-генерации;
- создаёт `Session` (или `EntityManager`, если идти через JPA API);
- содержит инфраструктурные компоненты (2nd-level cache, statistics, query plan cache — в зависимости от настроек).

Практика:
- создают один раз при старте приложения;
- переиспользуют во всех запросах;
- закрывают при остановке приложения.

Пример создания (Java SE):

```java
Configuration cfg = new Configuration().configure();
StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .applySettings(cfg.getProperties())
        .build();

SessionFactory sessionFactory = cfg.buildSessionFactory(registry);

// Вариант через JPA-тип:
EntityManagerFactory emf = sessionFactory; // SessionFactory реализует EntityManagerFactory
```

> Важно: в Hibernate 6 `SessionFactory` также выступает как JPA `EntityManagerFactory`.

### `Session` (Hibernate) / `EntityManager` (JPA)

`Session` — основной объект unit of work.

Что значит **unit of work**:
- открыли сессию;
- выполнили набор связанных действий (чтение/изменение сущностей);
- зафиксировали транзакцию;
- закрыли сессию.

Да, вы правильно понимаете: сделали запрос(ы) → закрыли `Session`; позже для новой бизнес-операции обычно берут **новую** сессию из `SessionFactory`.

Что «под капотом» кратко:
- внутри сессии живёт persistence context (1st-level cache);
- `Session` отслеживает изменения managed-сущностей (dirty checking);
- при `flush`/`commit` генерирует и отправляет SQL.

Пример (Java SE):

```java
Session session = sessionFactory.openSession();
// JPA-представление того же объекта:
EntityManager em = session; // Session расширяет/реализует JPA EntityManager API

try {
    User user = session.find(User.class, 1L);
    // ... работа с объектом
} finally {
    session.close();
}
```

Что чаще использовать: `openSession()` или `createEntityManager()`?
- В современных enterprise-проектах обычно чаще пишут через **JPA (`EntityManager`)**:
  - переносимость между провайдерами;
  - стандартный API для Spring Data/Jakarta EE.
- `Session` используют, когда нужны Hibernate-специфичные возможности
  (тонкие настройки, специфичные API, оптимизации).
- На практике это не «или/или»: в коде можно работать через JPA и при необходимости делать `unwrap(Session.class)`.

### `Transaction` (Hibernate) / `EntityTransaction` (JPA)

Соответствие SQL-транзакции:
- обычно это та же ACID-транзакция на уровне БД/соединения;
- Hibernate/JPA добавляют объектную обёртку и интеграцию с lifecycle сущностей;
- `commit()` приводит к `flush` и затем фиксации DB-транзакции (если flush mode стандартный).

Пример (Java SE):

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction(); // Hibernate API

try {
    User u = new User();
    u.setEmail("new@mail.com");
    session.persist(u);

    tx.commit();
    // commit() обычно возвращает void, результат операции — изменённое состояние БД
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    session.close();
}
```

JPA-вариант аналогичен:

```java
EntityManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();
tx.begin();
// ...
tx.commit();
```

### `Query` (Hibernate) / `TypedQuery` и `Query` (JPA)

Почему это «абстракция запроса»:
- вы работаете через объект `Query`, а не напрямую через `Statement/ResultSet`;
- ORM сам решает, как превратить запрос в SQL, как связать параметры и как материализовать результат.

Разница между HQL / JPQL / native SQL:
- **HQL** — язык запросов Hibernate к сущностям и их полям;
- **JPQL** — стандартный JPA-язык, очень близок к HQL;
- **native SQL** — обычный SQL конкретной БД (PostgreSQL, Oracle и т.д.).

Про «транзакционный запрос» и «обычный запрос»:
- технически `query` — это способ выполнить чтение/изменение;
- транзакция (`transaction`) — это граница атомарности;
- `SELECT` можно делать и без явной транзакции (зависит от окружения), но для `INSERT/UPDATE/DELETE` транзакция практически обязательна.

Пример query в Java SE:

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

try {
    // 1) JPQL/HQL: результат — список сущностей User
    List<User> users = session
            .createQuery("select u from User u where u.status = :st", User.class)
            .setParameter("st", Status.ACTIVE)
            .getResultList();

    // 2) Update query: результат — количество затронутых строк (int)
    int updated = session
            .createMutationQuery("update User u set u.status = :newSt where u.status = :oldSt")
            .setParameter("newSt", Status.INACTIVE)
            .setParameter("oldSt", Status.BANNED)
            .executeUpdate();

    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    session.close();
}
```

Типы результатов у query:
- `getSingleResult()` → один объект (или исключение, если 0/много);
- `getResultList()` → `List<T>`;
- `executeUpdate()` → `int` (число изменённых строк).

---

## 3. Persistence Context

**Persistence Context** — это «контекст управляемых сущностей» (identity map) внутри `Session`/`EntityManager`.

Ключевые свойства:
- для каждой записи (по PK) в контексте существует один Java-объект;
- изменения managed-сущностей отслеживаются автоматически (dirty checking);
- при flush изменения синхронизируются в БД (INSERT/UPDATE/DELETE);
- повторный `find()` того же id в рамках контекста обычно не делает новый SQL-select.

### Зачем это важно
1. Гарантия согласованности объектного графа.
2. Меньше лишних запросов в рамках одной транзакции.
3. Возможность писать «object-oriented» логику без ручного SQL-update на каждое поле.

### Можно ли посмотреть/изменить состояние Persistence Context через `Session`?
Да, частично.

Что можно сделать штатно:
- `session.contains(entity)` — проверить, находится ли объект в контексте;
- `session.detach(entity)` / `session.evict(entity)` — убрать конкретную сущность из контекста;
- `session.clear()` — очистить весь persistence context;
- `session.flush()` — принудительно отправить накопленные изменения в БД.

Пример с логированием и управлением контекстом:

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

try {
    User user = session.find(User.class, 1L);
    System.out.println("managed before detach = " + session.contains(user)); // true

    session.detach(user); // или session.evict(user)
    System.out.println("managed after detach = " + session.contains(user)); // false

    user.setEmail("detached@mail.com"); // change не будет автоматически сохранён

    session.merge(user); // возвращает managed-экземпляр с применёнными изменениями
    session.flush();

    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    session.close();
}
```

> Важно: стандартный API не даёт удобного "списка всех сущностей в контексте" для прод-кода; обычно используют `contains`, логирование SQL и Hibernate statistics.

---

## 4. Entity

### Понятие
**Entity** — это класс доменной модели, экземпляры которого хранятся в БД и имеют идентичность (обычно `@Id`).

### Базовые требования к entity
Класс обычно должен:
- быть помечен `@Entity` — иначе JPA-провайдер не включит класс в метамодель и не будет считать его управляемой сущностью (нельзя будет корректно `persist/find` как entity);
- иметь первичный ключ (`@Id`) — потому что сущность в JPA обязана иметь идентичность: по id ORM отслеживает объект в persistence context, формирует `UPDATE/DELETE` и связывает объект со строкой таблицы;
- иметь конструктор без аргументов (как минимум `protected`/`public`) — провайдер использует его для создания экземпляров через reflection при чтении из БД;
- не быть `final` (рекомендуется, особенно при проксировании) — Hibernate часто создаёт runtime-прокси (наследник класса) для lazy loading; `final`-класс нельзя унаследовать, поэтому проксирование entity-класса становится невозможным/ограниченным;
- соблюдать корректные `equals/hashCode` (осторожно с mutable id) — чтобы не ломать поведение entity в `Set/Map` и не получать трудноуловимые баги при переходах состояний (transient/managed/detached).

Пояснения к важным моментам:
- **Проксирование** — это техника, когда вместо «настоящей» сущности Hibernate сначала подставляет объект-обёртку (proxy), который догружает данные по требованию (обычно при первом обращении к полю/геттеру).
- Это **не всегда обязательно** в JPA: провайдер может использовать и другие механизмы (например, bytecode enhancement), но прокси — очень распространённый сценарий, поэтому ограничение `final` считается хорошей практикой.
- Что значит «корректные `equals/hashCode`»: методы должны быть согласованы между собой, стабильны в рамках использования в коллекциях и учитывать идентичность объекта без эффекта «значение хэша поменялось после `persist`».
- Практика: для entity с генерируемым id часто избегают использования mutable id в `hashCode` до присвоения ключа (иначе объект «теряется» в `HashSet` после flush/persist).
- `toString()` формально не обязателен для JPA, но обычно полезен для логирования/отладки. Важно не включать туда лениво загружаемые связи, чтобы случайно не триггерить лишние запросы или рекурсию.

### Основные аннотации

#### `@Entity`
Помечает класс как персистентную сущность.

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

#### `@Table`
Задаёт имя таблицы и доп. настройки (схема, индексы, unique constraints).

```java
@Entity
@Table(name = "users")
public class User { ... }
```

#### `@Column`
Управляет колонкой: имя, nullable, длина, unique, precision/scale и т.д.

```java
@Column(name = "email", nullable = false, unique = true, length = 320)
private String email;
```

#### `@Id`
Отмечает поле первичного ключа.

#### `@GeneratedValue`
Стратегия автогенерации ключа:
- `IDENTITY` — генерация на стороне БД (часто auto-increment);
- `SEQUENCE` — через sequence;
- `TABLE` — через специальную таблицу;
- `AUTO` — выбор провайдера.

### Жизненный цикл entity
1. **Transient** — объект создан через `new`, в БД не сохранён, контекст его не знает.
2. **Managed (Persistent)** — объект прикреплён к persistence context.
3. **Detached** — объект был managed, но контекст закрыт/очищен.
4. **Removed** — объект помечен на удаление (физически удалится при flush/commit).

### Варианты entity-моделей

#### `@Embeddable` + `@Embedded`

`@Embeddable` — это value-тип, который **не является отдельной entity**.

Важно: у `@Embeddable` действительно нет собственного `@Id`, и он не живёт как самостоятельная строка
в своей таблице. Но его поля **хранятся в БД** — обычно в таблице владельца (`@Embedded`) или в таблице коллекции (если `@ElementCollection`).

Полноценный пример:

```java
@Embeddable
public class Address {
    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;

    protected Address() {}

    public Address(String street, String city, String zipCode) {
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
    }
}

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Address address;
}
```

Как это выглядит в таблице `users`:
- `id`
- `email`
- `street`
- `city`
- `zip_code`

Зачем вводить `Embeddable`, а не делать «обычную сущность»:
- это **value object** без собственной идентичности (адрес как часть пользователя);
- меньше лишних таблиц/джойнов, если отдельная жизнь объекта не нужна;
- лучшее переиспользование повторяющегося набора полей (`Address`, `Money`, `AuditInfo`) в нескольких entity.

Когда всё же лучше обычная entity:
- когда объект должен существовать отдельно, иметь свой id, собственный lifecycle и ссылки от других сущностей.

#### `@MappedSuperclass`

`@MappedSuperclass` — базовый класс с полями маппинга, который **не является отдельной таблицей/сущностью**,
но его поля наследуются entity-классами.

Полноценный пример:

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;
}

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
}
```

Результат в БД:
- таблица `users` содержит `id`, `created_at`, `updated_at`, `email`;
- таблица `orders` содержит `id`, `created_at`, `updated_at`, `total_amount`;
- отдельной таблицы `base_entity` нет.

Зачем вводить `@MappedSuperclass`, а не дублировать всё в каждой сущности:
- исключаем копипасту (id, аудит, технические поля);
- централизуем общую логику (`@PrePersist`, `@PreUpdate`);
- упрощаем сопровождение: меняем одно место, а не 15 сущностей.

Когда лучше не использовать:
- если по базовому типу нужны полиморфные запросы (`select b from BaseEntity b`) — для этого чаще подходит inheritance mapping через `@Inheritance`, а не `@MappedSuperclass`.

---

## 5. EntityManager: основные операции

`EntityManager` — главный JPA-интерфейс для работы с сущностями.

### Часто используемые методы
- `persist(entity)` — сделать новую сущность managed и вставить в БД (обычно при flush).
- `find(Entity.class, id)` — найти по PK.
- `merge(entity)` — скопировать состояние detached-объекта в managed-экземпляр.
- `remove(entity)` — удалить managed-сущность.
- `flush()` — принудительно синхронизировать изменения с БД.
- `clear()` — очистить persistence context (все сущности станут detached).
- `createQuery(...)` / `createNativeQuery(...)` — JPQL/native запросы.

### Мини-пример
```java
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

User u = new User();
u.setEmail("a@b.com");
em.persist(u);

User loaded = em.find(User.class, u.getId());
loaded.setEmail("new@b.com");

em.getTransaction().commit();
em.close();
```

### Hibernate-вариант: `Session` (часто используемые методы)

Аналогичные операции через Hibernate API:
- `persist(entity)` — сохранить новую сущность;
- `get(Entity.class, id)` / `find(Entity.class, id)` — загрузить по id;
- `merge(entity)` — слить detached-состояние;
- `remove(entity)` — удалить сущность;
- `flush()` — синхронизировать контекст с БД;
- `clear()` / `evict(entity)` — очистить контекст полностью или частично;
- `createQuery(...)`, `createMutationQuery(...)`, `createNativeQuery(...)` — запросы.

Мини-пример через `Session`:

```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

try {
    User u = new User();
    u.setEmail("a@b.com");
    session.persist(u);

    User loaded = session.get(User.class, u.getId());
    loaded.setEmail("new@b.com");

    session.flush();
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    session.close();
}
```

### Что чаще на практике: Spring Data JPA, `EntityManager` или `Session`?

Кратко:
- да, в большинстве современных enterprise-проектов чаще всего работают через **Spring Data JPA** (репозитории + JPA-стек).
- в **чистой Java (без Spring)** чаще выбирают **JPA `EntityManager`** как основной API, если нужна переносимость и стандартный подход.
- **`Session`** выбирают, когда проект осознанно завязан на Hibernate и нужны его специфичные возможности/тонкие оптимизации.

Почему так:
- `EntityManager` = стандарт, проще сменить провайдера, легче поддерживать vendor-neutral код.
- `Session` = глубже доступ к Hibernate-фичам (batch/fetch tuning, специфичные настройки, расширенные API).
- распространённая практика: базово писать на JPA API и локально делать `unwrap(Session.class)`, где нужны Hibernate-специфичные фичи.

---
❓Что такое EntityManager в JPA и за что он отвечает?

EntityManager (https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/entitymanager) является основным интерфейс JPA для работы с сущностями и их жизненным циклом. В сути своей он отвечает за сохранение объектов в базу данных, поиск объектов по идентификатору, обновление и удаление сущностей, управление состоянием объектов (жизненный цикл), работу с транзакциями.


Проще говоря, Entity Manager это мост между Java-объектами и базой данных.

В рамках жизненного цикла сущности в JPA могут обладать различным состоянием:
- New — объект создан, но еще не добавлен в контекст и не сохранен
- Managed — объект связан с Entity Manager (добавлен в контекст), его изменения отслеживаются
- Detached — объект отсоединен от контекста
- Removed — объект помечен на удаление

EntityManager управляет этими состояниями и синхронизирует изменения с базой данных.

Основные операции Entity Manager:

🔹Создание и сохранение сущности
```java
EntityManager em = entityManagerFactory.createEntityManager();
em.getTransaction().begin();

User user = new User();
user.setName("Alice");

em.persist(user); // сохраняет сущность в базу
em.getTransaction().commit();
```
🔹Поиск сущности
```java
EntityManager em = entityManagerFactory.createEntityManager();
User user = em.find(User.class, 1L); // ищет User с ID = 1
```
🔹Обновление сущности
```java
EntityManager em = entityManagerFactory.createEntityManager();
em.getTransaction().begin();
user.setName("Bob");
em.getTransaction().commit(); // изменения автоматически сохраняются
```
🔹Удаление сущности
```java
EntityManager em = entityManagerFactory.createEntityManager();
em.getTransaction().begin();
em.remove(user);
em.getTransaction().commit();
```
📌 Вывод
EntityManager является сердцем JPA и то, что поистине необходимо знать. Он позволяет управлять жизненным циклом сущностей, сохранять, обновлять и удалять объекты, выполнять запросы к базе через JPQL или нативный SQL, работать с транзакциями и отслеживать изменения автоматически.


Без него невозможно построить полноценную ORM-архитектуру основанную на JPA.

---

## 6. Стратегии маппинга наследования

### Что это вообще такое

Стратегии маппинга наследования — это правила, по которым ORM раскладывает Java-иерархию классов
(базовый класс + наследники) на таблицы реляционной БД.

Проблема, которую они решают:
- в Java есть наследование (`Vehicle -> Car`, `Vehicle -> Truck`),
- в SQL нет прямого механизма наследования классов,
- поэтому JPA/Hibernate должны выбрать схему хранения этих типов.

Стратегия задаётся на базовом классе:

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // или JOINED / TABLE_PER_CLASS
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String model;
}
```

### `SINGLE_TABLE`

Все классы иерархии лежат в одной таблице, тип строки различается discriminator-колонкой.

Пример:

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
}

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
    private Integer seatCount;
}

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    private BigDecimal payloadCapacity;
}
```

Как выглядит таблица (упрощённо):
- `vehicles(id, model, vehicle_type, seat_count, payload_capacity)`

**Плюсы:** быстрые select без join между таблицами.
**Минусы:** много nullable-колонок для полей, неактуальных конкретному подтипу.

### `JOINED`

Базовые поля в базовой таблице, поля наследников — в отдельных таблицах, связанных по PK/FK.

Пример:

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
}

@Entity
@Table(name = "cars")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class Car extends Vehicle {
    private Integer seatCount;
}

@Entity
@Table(name = "trucks")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class Truck extends Vehicle {
    private BigDecimal payloadCapacity;
}
```

Как выглядит схема (упрощённо):
- `vehicles(id, model)`
- `cars(vehicle_id, seat_count)`
- `trucks(vehicle_id, payload_capacity)`

**Плюсы:** нормализованная схема, меньше nullable-полей.
**Минусы:** чтение наследников требует join.

### `TABLE_PER_CLASS`

Каждый конкретный класс хранится в своей таблице, базовый обычно абстрактный.

Пример:

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String model;
}

@Entity
@Table(name = "cars")
public class Car extends Vehicle {
    private Integer seatCount;
}

@Entity
@Table(name = "trucks")
public class Truck extends Vehicle {
    private BigDecimal payloadCapacity;
}
```

Как выглядит схема (упрощённо):
- `cars(id, model, seat_count)`
- `trucks(id, model, payload_capacity)`

**Плюсы:** нет nullable-полей, каждая таблица самодостаточна.
**Минусы:** полиморфные запросы по базовому типу (`select v from Vehicle v`) часто приводят к `UNION` и могут быть дороже.

### Практический выбор
- `SINGLE_TABLE` — часто default для производительности и простоты.
- `JOINED` — когда важна чистая/нормализованная схема данных.
- `TABLE_PER_CLASS` — реже, обычно для узких сценариев.

---

## 7. Маппинг различных видов сущностей

### 7.1 Enum

`Enum` в Java можно хранить в БД двумя основными способами через `@Enumerated`:
- `EnumType.STRING` — в колонку пишется **имя** enum-константы (`ACTIVE`, `BANNED`);
- `EnumType.ORDINAL` — в колонку пишется **порядковый номер** enum-константы (`0`, `1`, `2`).

Пример enum:

```java
public enum UserStatus {
    NEW,
    ACTIVE,
    BLOCKED
}
```

Пример entity с комментариями:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Рекомендуемый вариант: хранится текст "NEW" / "ACTIVE" / "BLOCKED"
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;
}
```

Как это обычно выглядит в таблице `users`:
- `id BIGINT PRIMARY KEY`
- `status VARCHAR(32) NOT NULL` (если `EnumType.STRING`)

Отдельная таблица для `UserStatus` в таком маппинге **не создаётся**: enum хранится прямо в колонке той же таблицы (`users.status`).
Если нужен «справочник статусов» с внешним ключом и доп. атрибутами, это уже обычно отдельная сущность/таблица, а не `@Enumerated`.

Почему обычно рекомендуют `STRING`:
- безопасно при изменении порядка констант в enum;
- данные в БД читаемы человеком;
- меньше риск «тихой» порчи данных при эволюции кода.

Почему `ORDINAL` опасен:
- если поменять порядок enum-констант, старые числа начнут означать другие состояния.

---

### 7.2 Коллекции базовых типов — `@ElementCollection`

Что такое «value-элементы (не entity)»:
- это значения **без собственного id и самостоятельной жизни** в модели;
- примеры: `Set<String> tags`, `List<Integer> scores`, `Map<String, String> settings`;
- ORM хранит их в отдельной коллекционной таблице, но это не отдельные entity-классы.

Пример: у пользователя есть набор тегов и список языков.

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Набор строковых тегов (без отдельной entity Tag)
    @ElementCollection
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tag", nullable = false)
    private Set<String> tags = new HashSet<>();

    // Список предпочитаемых языков (порядок важен)
    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "language", nullable = false)
    @OrderColumn(name = "lang_order")
    private List<String> languages = new ArrayList<>();
}
```

Как это хранится:
- `users(id, email)`
- `user_tags(user_id, tag)`
- `user_languages(user_id, lang_order, language)`

Практические детали по таблицам коллекций:
- да, это физически обычные таблицы в БД, к ним можно обращаться отдельными SQL-запросами (например, для диагностики/отчётов);
- но в JPA-модели это не самостоятельные entity, поэтому типичный доступ идёт через владельца (`User`);
- первичный ключ зависит от дизайна:
  - для `Set` часто делают составной PK/unique по `(user_id, tag)`, чтобы не было дублей;
  - для `List` с `@OrderColumn` часто используют `(user_id, lang_order)` как ключ порядка;
  - иногда добавляют surrogate-id, но это уже решение схемы БД, а не требование `@ElementCollection`.

Когда `@ElementCollection` уместен:
- когда нет необходимости ссылаться на элемент как на отдельную сущность;
- когда не нужен отдельный lifecycle, отдельные связи и отдельные запросы к элементу.

---

### 7.3 Составной ключ — `@IdClass` и `@EmbeddedId`

Составной ключ = первичный ключ из **нескольких колонок**, например `(order_id, product_id)`.

Это типично для таблиц-связок, исторических таблиц, доменных ключей.

#### Вариант 1: `@IdClass`

Идея:
- ключевые поля объявляются прямо в entity как несколько `@Id`;
- отдельно создаётся key-класс (`implements Serializable`) с теми же полями.

```java
// Класс ключа (имена и типы полей должны совпадать с @Id-полями entity)
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;

    public OrderItemId() {}

    // equals/hashCode обязательны:
    // JPA сравнивает идентификаторы по значению,
    // key-объект используется как ключ идентичности в контексте/кэше/коллекциях
    // (обычно генерируются IDE/Lombok)
}

@Entity
@Table(name = "order_items")
@IdClass(OrderItemId.class)
public class OrderItem {
    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
```

Плюс `@IdClass`: удобно обращаться к полям ключа напрямую (`entity.getOrderId()`).

#### Вариант 2: `@EmbeddedId`

Идея:
- составной ключ оформляется как value-объект (`@Embeddable`);
- в entity хранится единое поле `id` с этим типом.

```java
@Embeddable
public class OrderItemKey implements Serializable {
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    public OrderItemKey() {}

    // equals/hashCode обязательны по тем же причинам:
    // составной id — value object, который должен корректно сравниваться по значениям полей
    // (обычно генерируются IDE/Lombok)
}

@Entity
@Table(name = "order_items")
public class OrderItem {
    @EmbeddedId
    private OrderItemKey id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
```

Как выглядит таблица `order_items` в БД (упрощённо, для обоих подходов):
- `order_id BIGINT NOT NULL`
- `product_id BIGINT NOT NULL`
- `quantity INT NOT NULL`
- `PRIMARY KEY (order_id, product_id)`

Как хранятся `OrderItemId` / `OrderItemKey`:
- в БД нет отдельной «колонки объекта» и нет отдельной таблицы только под ключ;
- их поля раскладываются в обычные колонки таблицы (`order_id`, `product_id`);
- разница между `@IdClass` и `@EmbeddedId` в основном в модели Java-кода, а не в физическом хранении.

Плюс `@EmbeddedId`: ключ моделируется как цельный объект, чаще более «чистая» OO-модель.

#### Что выбрать
- `@IdClass` — когда удобнее иметь ключевые части как отдельные поля entity.
- `@EmbeddedId` — когда ключ логически целостен и хочется держать его как value object.

Оба варианта корректны и широко используются.

---

## Краткий итог части 1

В первых 7 вопросах важно чётко понимать:
1. JPA — спецификация, Hibernate — популярная реализация.
2. `Session/EntityManager` + `Transaction` + `Persistence Context` — ядро работы ORM.
3. `Entity` и её lifecycle определяют, когда и как изменения попадут в БД.
4. Наследование, enum, value-коллекции и составные ключи требуют осознанного выбора стратегии.

Если нужно, в следующей части можно разобрать пункты 8–14 (связи, каскады, кэш, блокировки, N+1, EntityListeners) в том же формате.
