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

Ниже — классический стек Hibernate (native API) и его JPA-аналогии.

### `Configuration` (Hibernate)
Используется для bootstrap-конфигурации Hibernate:
- настройка datasource, dialect, ddl-auto и т.д.;
- регистрация entity-классов;
- построение `SessionFactory`.

> В современных приложениях (Spring Boot) это часто скрыто автоконфигурацией.

### `SessionFactory` (Hibernate)
- Тяжёлый потокобезопасный объект уровня приложения.
- Создаётся обычно один раз при старте.
- Фабрика для `Session`.

JPA-аналог: `EntityManagerFactory`.

### `Session` (Hibernate)
- Основной объект для работы с БД в пределах unit of work.
- Содержит persistence context (1st-level cache).
- Выполняет CRUD и запросы.

JPA-аналог: `EntityManager`.

### `Transaction`
- Граница атомарной операции (begin/commit/rollback).
- Все изменения сущностей должны фиксироваться в транзакции.

JPA: `EntityTransaction` (либо контейнерные транзакции, например `@Transactional` в Spring).

### `Query`
- Абстракция запроса (HQL/JPQL/native SQL).
- Поддерживает параметры, пагинацию, single/list result.

В JPA обычно используют:
- `TypedQuery<T>` для типобезопасных JPQL-запросов,
- `Query` для универсальных/нативных сценариев.

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
