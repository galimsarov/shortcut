# Hibernate / JPA — Part 1 (вопросы 1–7)

> Конспект по первым 7 темам: от ORM и базовых интерфейсов до Persistence Context, Entity и специальных видов маппинга.

---

## 1. Концепция ORM. Hibernate, JPA

### Что такое ORM
**ORM (Object-Relational Mapping)** — подход, который связывает объектную модель приложения (классы, поля, связи) и реляционную модель БД (таблицы, колонки, внешние ключи).

Идея: работать в коде с объектами (`User`, `Order`), а не писать SQL для каждой операции вручную.

### Что даёт ORM
- уменьшает количество шаблонного JDBC-кода;
- автоматически маппит строки таблиц в Java-объекты и обратно;
- поддерживает жизненный цикл сущностей (managed/detached и т.д.);
- даёт декларативные транзакции, кэш, механизмы загрузки графа сущностей.

### Hibernate и JPA — в чём разница
- **JPA (Jakarta Persistence API)** — это **спецификация** (контракты: интерфейсы, аннотации, правила).
- **Hibernate** — это **реализация** JPA + дополнительные возможности сверх спецификации.

На практике:
- код пишут в терминах JPA (`EntityManager`, `@Entity`),
- в runtime часто используют Hibernate как provider.

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
