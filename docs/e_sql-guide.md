# SQL Guide (PostgreSQL)

> Конспект для подготовки к встрече: от базового SQL до оптимизации.

---

## 1) Базы данных и SQL

### DDL и DML: что это и чем отличается

- **DDL (Data Definition Language)** — команды для структуры БД:
  - `CREATE`, `ALTER`, `DROP`, `TRUNCATE`, `RENAME`.
- **DML (Data Manipulation Language)** — команды для данных:
  - `INSERT`, `UPDATE`, `DELETE`, `SELECT`.

Ключевая разница:
- DDL меняет **схему** (таблицы, индексы, ограничения).
- DML меняет/читает **записи** в уже существующей схеме.

Пример:
```sql
-- DDL
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- DML
INSERT INTO users (email) VALUES ('alice@example.com');
SELECT * FROM users;
```

### Базовые операции

```sql
-- CREATE
INSERT INTO users (email) VALUES ('bob@example.com');

-- READ
SELECT id, email
FROM users
WHERE email LIKE '%@example.com'
ORDER BY id DESC
LIMIT 10;

-- UPDATE
UPDATE users
SET email = 'robert@example.com'
WHERE id = 1;

-- DELETE
DELETE FROM users
WHERE id = 1;
```

### JOIN: INNER, LEFT, RIGHT

Пусть есть таблицы:
- `customers(id, name)`
- `orders(id, customer_id, total)`

#### INNER JOIN
Возвращает только совпадающие строки.

```sql
SELECT c.id, c.name, o.id AS order_id, o.total
FROM customers c
INNER JOIN orders o ON o.customer_id = c.id;
```

#### LEFT JOIN
Возвращает все строки слева + совпадения справа. Если справа нет пары — `NULL`.

```sql
SELECT c.id, c.name, o.id AS order_id
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id;
```

#### RIGHT JOIN
Симметричен `LEFT JOIN`: все строки справа + совпадения слева.

```sql
SELECT c.id, c.name, o.id AS order_id
FROM customers c
RIGHT JOIN orders o ON o.customer_id = c.id;
```

> На практике чаще используют `LEFT JOIN`, а `RIGHT JOIN` реже (читаемость).

### UNION и MERGE

#### UNION
Объединяет результаты двух `SELECT`.

- `UNION` — удаляет дубликаты.
- `UNION ALL` — оставляет дубликаты (обычно быстрее).

```sql
SELECT email FROM users_2025
UNION ALL
SELECT email FROM users_2026;
```

#### MERGE (PostgreSQL 15+)
Позволяет в одном операторе делать upsert/delete по условию сопоставления.

```sql
MERGE INTO users u
USING staging_users s
ON u.email = s.email
WHEN MATCHED THEN
  UPDATE SET created_at = s.created_at
WHEN NOT MATCHED THEN
  INSERT (email, created_at)
  VALUES (s.email, s.created_at);
```

> До появления `MERGE` часто использовали `INSERT ... ON CONFLICT ... DO UPDATE`.

### Агрегация и GROUP BY

Агрегатные функции: `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`.

```sql
SELECT customer_id,
       COUNT(*) AS orders_count,
       SUM(total) AS orders_sum,
       AVG(total) AS avg_order
FROM orders
GROUP BY customer_id
HAVING SUM(total) > 1000;
```

- `WHERE` фильтрует строки **до** агрегации.
- `HAVING` фильтрует группы **после** агрегации.

### Подзапросы: коррелированные/некоррелированные, EXISTS

#### Некоррелированный подзапрос
Выполняется независимо от внешнего запроса.

```sql
SELECT *
FROM orders
WHERE total > (
  SELECT AVG(total)
  FROM orders
);
```

#### Коррелированный подзапрос
Ссылается на строку внешнего запроса.

```sql
SELECT c.id, c.name
FROM customers c
WHERE EXISTS (
  SELECT 1
  FROM orders o
  WHERE o.customer_id = c.id
);
```

#### EXISTS
Проверяет факт существования хотя бы одной строки.

- `EXISTS` часто эффективнее, чем `IN`, когда важен именно факт наличия.
- Обычно прекращает поиск на первом совпадении.

### Констрейнты (constraints)

Основные ограничения:
- `PRIMARY KEY`
- `FOREIGN KEY`
- `UNIQUE`
- `NOT NULL`
- `CHECK`
- `DEFAULT` (формально не ограничение целостности, но часть определения колонки)

Пример:
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  sku TEXT NOT NULL UNIQUE,
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  status TEXT NOT NULL DEFAULT 'active'
);
```

### Отношения между таблицами, PK и FK

- **1:1** — редкий случай, обычно отдельная таблица с FK + UNIQUE.
- **1:N** — самая частая связь (один customer → много orders).
- **N:M** — через таблицу-связку (`student_courses`).

```sql
CREATE TABLE customers (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL REFERENCES customers(id),
  total NUMERIC(12,2) NOT NULL
);
```

- `PRIMARY KEY` — уникальный идентификатор строки.
- `FOREIGN KEY` — ссылка на PK/UNIQUE другой таблицы, поддерживает ссылочную целостность.

### CTE (Common Table Expression)

CTE — временный именованный результат внутри одного запроса (`WITH`).

```sql
WITH rich_customers AS (
  SELECT customer_id, SUM(total) AS sum_total
  FROM orders
  GROUP BY customer_id
  HAVING SUM(total) > 5000
)
SELECT c.id, c.name, rc.sum_total
FROM rich_customers rc
JOIN customers c ON c.id = rc.customer_id;
```

Полезно для:
- разбиения сложного запроса на читаемые блоки;
- рекурсивных запросов (`WITH RECURSIVE`).

---

## 2) Индексы

### Что это и зачем

**Индекс** — дополнительная структура данных, ускоряющая поиск/сортировку/соединение.

Плюсы:
- быстрее `SELECT` по условиям, `JOIN`, `ORDER BY`, иногда `GROUP BY`.

Минусы:
- занимают место;
- замедляют `INSERT/UPDATE/DELETE` (индексы тоже нужно поддерживать);
- «слишком много индексов» может ухудшить общую производительность.

Создание:
```sql
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

### Почему нельзя «сделать индексы на всё»

1. Каждый индекс — дополнительные записи на диск и в память.
2. Любое изменение строки приводит к обновлению всех затронутых индексов.
3. Планировщик может выбрать неидеальный план при избытке конкурирующих индексов.
4. Индексы на низкоселективные поля (например, `is_active`) часто бесполезны.

### Кластеризованные и некластеризованные

В PostgreSQL нет «clustered index» как постоянного свойства таблицы (как в MS SQL).

- Есть команда `CLUSTER`, которая **физически** переупорядочивает таблицу по индексу в момент выполнения.
- Со временем порядок нарушается новыми вставками/апдейтами.

Некластеризованный индекс в классическом смысле — обычный индекс PostgreSQL (`btree`, `hash`, `gist` и т.д.), который хранится отдельно от heap-таблицы.

### Типы индексов: BTREE, HASH, GiST

#### BTREE (по умолчанию)
Подходит для:
- `=`, `<`, `<=`, `>`, `>=`, `BETWEEN`,
- сортировок,
- prefix-поиска по тексту (`LIKE 'abc%'`).

```sql
CREATE INDEX idx_users_email_btree ON users(email);
```

#### HASH
Специализирован под равенство `=`.

```sql
CREATE INDEX idx_users_email_hash ON users USING HASH(email);
```

Обычно BTREE достаточно и универсальнее.

#### GiST
Обобщённая структура для сложных типов и операторов:
- геоданные (PostGIS),
- диапазоны,
- полнотекстовые сценарии в связке с расширениями.

```sql
-- пример с диапазоном дат
CREATE INDEX idx_bookings_period_gist ON bookings USING GIST(period);
```

### Внутреннее устройство BTREE (упрощённо)

- Дерево состоит из **страниц** (pages): корень, внутренние узлы, листья.
- В листьях хранятся ключи и ссылки на строки таблицы (TID).
- Поиск: от корня к листу, отбрасывая большие диапазоны на каждом уровне.
- Сложность: примерно `O(log N)`.
- При вставках возможен **split** страниц; для «пухлых» индексов важны `VACUUM`/`REINDEX`/настройка fillfactor.

---

## 3) Транзакции и ACID

### Что такое транзакция

Транзакция — набор операций, выполняемый как единое целое.

```sql
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;
```

Если ошибка:
```sql
ROLLBACK;
```

### ACID с примерами

#### A — Atomicity (атомарность)
Либо все операции прошли, либо ни одна.

Пример: перевод денег между счетами не должен списать деньги «в никуда».

#### C — Consistency (согласованность)
Транзакция переводит БД из одного корректного состояния в другое, соблюдая ограничения.

Пример: `CHECK (balance >= 0)` и FK не позволяют записать недопустимые данные.

#### I — Isolation (изоляция)
Параллельные транзакции не должны «ломать» друг другу результат.

Пример: пользователь не видит незакоммиченные изменения другого пользователя.

#### D — Durability (долговечность)
После `COMMIT` данные сохраняются даже при сбое.

Пример: PostgreSQL подтверждает коммит после записи в WAL (с учётом настроек надёжности).

### Уровни изоляции и какие проблемы решают

В PostgreSQL:
- `READ COMMITTED` (по умолчанию)
- `REPEATABLE READ`
- `SERIALIZABLE`

`READ UNCOMMITTED` в PostgreSQL фактически ведёт себя как `READ COMMITTED`.

#### Аномалии
- **Dirty read** — чтение незакоммиченных данных.
- **Non-repeatable read** — повторное чтение той же строки даёт другой результат.
- **Phantom read** — повторный запрос по условию возвращает другой набор строк.
- **Serialization anomaly** — результат параллельного выполнения нельзя получить никакой последовательностью.

#### READ COMMITTED
- Не допускает dirty read.
- Допускает non-repeatable read и phantom.

#### REPEATABLE READ (MVCC snapshot в PostgreSQL)
- Не допускает dirty и non-repeatable.
- В PostgreSQL также предотвращает классические phantom в пределах snapshot-чтения, но возможны write skew-сценарии в общем случае конкурентной логики.

#### SERIALIZABLE
- Самый строгий.
- Эмулирует последовательное выполнение транзакций.
- Может завершать транзакции с `serialization_failure` — нужно делать retry.

---

## 4) Оптимизация запросов

### EXPLAIN и EXPLAIN ANALYZE

- `EXPLAIN` — показывает план, **не выполняя** запрос.
- `EXPLAIN ANALYZE` — выполняет запрос и показывает фактические метрики (`actual time`, `rows`, loops).

```sql
EXPLAIN ANALYZE
SELECT *
FROM orders
WHERE customer_id = 42;
```

Смотреть в первую очередь:
- `Seq Scan` vs `Index Scan` / `Bitmap Index Scan`;
- оценка `rows` vs фактические `rows` (ошибки статистики);
- самые дорогие узлы (`cost`, `actual time`).

### Влияние индексов на производительность

- Для точечного поиска и selective-фильтров индекс резко ускоряет запрос.
- Для маленькой таблицы или «широкого» фильтра planner может выбрать `Seq Scan` — и это нормально.
- Композитный индекс важен в порядке колонок:

```sql
CREATE INDEX idx_orders_customer_created
  ON orders(customer_id, created_at);
```

Он эффективен для:
- `WHERE customer_id = ?`
- `WHERE customer_id = ? AND created_at >= ?`

Но хуже для запроса только по `created_at`.

### Партиционирование

**Партиционирование** — логическая таблица, физически разделённая на части (например, по месяцу).

Когда полезно:
- очень большие таблицы,
- типичные фильтры по ключу партиции (`created_at`, `tenant_id`),
- lifecycle-операции (быстро удалить старую партицию).

```sql
CREATE TABLE events (
  id BIGSERIAL,
  created_at DATE NOT NULL,
  payload JSONB
) PARTITION BY RANGE (created_at);
```

### Шардирование

**Шардирование** — распределение данных по нескольким инстансам/серверам.

Плюсы:
- горизонтальное масштабирование.

Минусы:
- сложные join/транзакции между шардами,
- сложнее поддержка, миграции, ребалансировка.

Обычно применяют, когда вертикальное масштабирование и партиционирование уже недостаточны.

### CQRS (Command Query Responsibility Segregation)

Идея: разделить модели/хранилища для:
- **Command** (запись),
- **Query** (чтение).

Практически:
- OLTP-база пишет «истину»;
- отдельные read-модели/витрины оптимизированы под быстрые запросы отчётности.

Компромисс: сложнее архитектура и появляется eventual consistency.

---

## 5) Объекты БД

### VIEW

Виртуальная таблица на основе запроса. Данные физически не хранятся.

```sql
CREATE VIEW active_users AS
SELECT id, email
FROM users
WHERE status = 'active';
```

### MATERIALIZED VIEW

Физически хранит результат запроса (снимок), требует обновления.

```sql
CREATE MATERIALIZED VIEW sales_daily AS
SELECT date_trunc('day', created_at) AS day,
       SUM(total) AS total
FROM orders
GROUP BY 1;

REFRESH MATERIALIZED VIEW sales_daily;
```

Полезно для тяжёлых отчётных запросов.

### Процедуры и функции

В PostgreSQL чаще используют **functions** (`CREATE FUNCTION`) и **procedures** (`CREATE PROCEDURE`, вызываются через `CALL`).

```sql
CREATE OR REPLACE FUNCTION inc_counter(p_id BIGINT)
RETURNS VOID AS $$
BEGIN
  UPDATE counters SET value = value + 1 WHERE id = p_id;
END;
$$ LANGUAGE plpgsql;
```

### Триггеры

Триггер — код, который автоматически выполняется при `INSERT/UPDATE/DELETE`.

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
```

Использовать аккуратно: триггеры могут усложнить отладку и скрыть бизнес-логику.

---

## 6) Нормализация БД

Цель нормализации — убрать избыточность и аномалии обновления.

Коротко по формам:

1. **1NF**: атомарные значения, нет повторяющихся групп.
2. **2NF**: нет частичной зависимости от составного ключа.
3. **3NF**: нет транзитивных зависимостей неключевых атрибутов.
4. **BCNF**: усиленная 3NF (каждый детерминант — кандидатный ключ).

Практика:
- для OLTP обычно нормализуют до 3NF/BCNF;
- для аналитики допускают денормализацию ради скорости чтения.

---

## 7) Опционально: Liquibase

**Liquibase** — инструмент версионирования схемы БД (database migrations).

Что даёт:
- хранение изменений схемы в VCS;
- воспроизводимое применение миграций в разных средах;
- контроль порядка и целостности применения (`DATABASECHANGELOG`).

Мини-пример XML changeset:
```xml
<changeSet id="001-create-users" author="you">
    <createTable tableName="users">
        <column name="id" type="BIGINT" autoIncrement="true">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="email" type="TEXT">
            <constraints nullable="false" unique="true"/>
        </column>
    </createTable>
</changeSet>
```

---

## Мини-чеклист перед собеседованием по SQL

1. Уметь руками написать `JOIN`, `GROUP BY`, подзапрос и CTE.
2. Понимать разницу `WHERE` vs `HAVING`.
3. Объяснить, когда индекс помогает, а когда нет.
4. Прочитать базовый `EXPLAIN ANALYZE` и найти узкое место.
5. Уверенно рассказать ACID и уровни изоляции с 1–2 примерами аномалий.
6. Понимать `PK/FK` и типы отношений (`1:1`, `1:N`, `N:M`).

Если хочешь, следующим шагом сделаем отдельный файл с **20 тренировочными SQL-задачами** (с решениями и разбором плана выполнения).
