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
- быть помечен `@Entity`;
- иметь первичный ключ (`@Id`);
- иметь конструктор без аргументов (как минимум protected/public);
- не быть `final` (рекомендуется, особенно при проксировании);
- соблюдать корректные `equals/hashCode` (осторожно с mutable id).

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

#### `@Embeddable`
Класс-значение без собственного id, встраивается в entity через `@Embedded`.

Пример: `Address` (city, street, zip) внутри `User`.

#### `@MappedSuperclass`
Базовый класс с общими полями маппинга (id, audit-поля),
но сам по себе не является отдельной сущностью/таблицей.

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

---

## 6. Стратегии маппинга наследования

В JPA наследование задаётся через `@Inheritance(strategy = ...)`.

### `SINGLE_TABLE`
- Вся иерархия хранится в одной таблице.
- Тип сущности различается по discriminator-колонке.

**Плюсы:** быстрые запросы без JOIN между таблицами.
**Минусы:** много nullable-колонок, слабее нормализация.

### `JOINED`
- Базовые поля в базовой таблице.
- Поля наследников в отдельных таблицах.
- Для чтения наследника нужны JOIN.

**Плюсы:** нормализованная схема.
**Минусы:** более тяжёлые запросы.

### `TABLE_PER_CLASS`
- Отдельная таблица на каждый конкретный класс.
- Базовый класс обычно абстрактный.

**Плюсы:** нет nullable-полей, простая структура на класс.
**Минусы:** полиморфные запросы дорогие (часто UNION).

### Практический выбор
- По умолчанию часто выбирают `SINGLE_TABLE` для производительности.
- `JOINED` выбирают, когда важна нормализация и чистая модель данных.
- `TABLE_PER_CLASS` используют реже, точечно.

---

## 7. Маппинг различных видов сущностей

### 7.1 Enum

Рекомендуемый вариант:
```java
@Enumerated(EnumType.STRING)
private Status status;
```

Почему `STRING` лучше `ORDINAL`:
- устойчиво к изменению порядка enum-констант;
- читаемо в БД;
- безопаснее при эволюции кода.

### 7.2 Коллекции базовых типов — `@ElementCollection`

Используется, когда нужно хранить набор value-элементов (не entity), например список тегов.

```java
@ElementCollection
@CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
@Column(name = "tag")
private Set<String> tags = new HashSet<>();
```

Особенности:
- элементы не имеют собственного id как entity;
- обычно хранятся в отдельной таблице коллекции;
- lifecycle коллекции привязан к владельцу-entity.

### 7.3 Составной ключ — `@IdClass` и `@EmbeddedId`

#### `@IdClass`
- Поля ключа объявляются прямо в entity.
- Отдельный класс key служит «зеркалом» полей id.

Подходит, когда хочется обращаться к частям ключа как к обычным полям entity.

#### `@EmbeddedId`
- Ключ оформлен как value-объект (`@Embeddable`) и хранится единым полем.
- Более объектно-ориентированный и часто более чистый дизайн.

```java
@Embeddable
public class OrderItemId {
    private Long orderId;
    private Long productId;
}

@Entity
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;
}
```

### Что выбрать для составного ключа
- Нужна явная работа с отдельными id-полями в entity → `@IdClass`.
- Нужен цельный value object ключа → `@EmbeddedId`.

---

## Краткий итог части 1

В первых 7 вопросах важно чётко понимать:
1. JPA — спецификация, Hibernate — популярная реализация.
2. `Session/EntityManager` + `Transaction` + `Persistence Context` — ядро работы ORM.
3. `Entity` и её lifecycle определяют, когда и как изменения попадут в БД.
4. Наследование, enum, value-коллекции и составные ключи требуют осознанного выбора стратегии.

Если нужно, в следующей части можно разобрать пункты 8–14 (связи, каскады, кэш, блокировки, N+1, EntityListeners) в том же формате.
