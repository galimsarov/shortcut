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

### Что такое «схема» в БД

В терминах PostgreSQL обычно есть 3 уровня:
1. **Database** — отдельная база данных (например, `app_db`).
2. **Schema** — пространство имён внутри базы (например, `public`, `billing`, `analytics`).
3. **Table** — конкретная таблица внутри схемы (например, `billing.invoices`).

То есть схема действительно «между БД и таблицей»:
- не отдельный сервер и не отдельная база,
- а логическая папка/namespace для объектов.

Пример:
```sql
CREATE DATABASE app_db;

-- подключились к app_db
CREATE SCHEMA billing;

CREATE TABLE billing.invoices (
  id BIGSERIAL PRIMARY KEY,
  amount NUMERIC(12,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

Что это даёт на практике:
- удобная группировка объектов по доменам (`billing.*`, `crm.*`);
- меньше конфликтов имён таблиц;
- можно гибко выдавать права на уровне схем.

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

#### MERGE (PostgreSQL 15+): подробнее

`MERGE` — это оператор синхронизации данных между **целевой** таблицей (`MERGE INTO`) и **источником** (`USING`).

Типовой кейс: есть «боевые» данные `products` и стейджинг-таблица `products_staging`, куда загружается новая версия каталога.

```sql
CREATE TABLE products (
  sku TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE products_staging (
  sku TEXT,
  name TEXT,
  price NUMERIC(12,2),
  is_active BOOLEAN
);
```

Пример синхронизации:
```sql
MERGE INTO products p
USING products_staging s
ON p.sku = s.sku
WHEN MATCHED AND s.is_active = FALSE THEN
  DELETE
WHEN MATCHED THEN
  UPDATE SET
    name = s.name,
    price = s.price,
    is_active = s.is_active,
    updated_at = now()
WHEN NOT MATCHED THEN
  INSERT (sku, name, price, is_active)
  VALUES (s.sku, s.name, s.price, COALESCE(s.is_active, TRUE));
```

Как читать этот `MERGE`:
1. `ON p.sku = s.sku` — правило сопоставления строк.
2. `WHEN MATCHED AND ... THEN DELETE` — если товар найден и в источнике выключен, удаляем.
3. `WHEN MATCHED THEN UPDATE` — если товар найден, обновляем.
4. `WHEN NOT MATCHED THEN INSERT` — если товара нет в целевой таблице, добавляем.

> До появления `MERGE` часто использовали `INSERT ... ON CONFLICT ... DO UPDATE`, но `MERGE` удобнее, когда в одном месте нужно и `UPDATE`, и `INSERT`, и `DELETE`.

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

#### EXISTS: подробнее

`EXISTS` возвращает `TRUE`, если вложенный запрос вернул хотя бы одну строку.

Важно:
- что именно стоит в `SELECT` внутри `EXISTS` (например, `SELECT 1` или `SELECT *`) — обычно неважно;
- проверяется **факт наличия** строки, а не значения;
- хорошо подходит для проверок «есть/нет связанных записей».

Структура таблиц для примера:
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

1) Клиенты, у которых есть хотя бы один заказ:
```sql
SELECT c.id, c.name
FROM customers c
WHERE EXISTS (
  SELECT 1
  FROM orders o
  WHERE o.customer_id = c.id
);
```

2) Клиенты, у которых заказов нет (`NOT EXISTS`):
```sql
SELECT c.id, c.name
FROM customers c
WHERE NOT EXISTS (
  SELECT 1
  FROM orders o
  WHERE o.customer_id = c.id
);
```

Когда `EXISTS` особенно удобен:
- при коррелированных условиях по связанной таблице;
- когда нужно избежать ловушек с `NULL`, которые бывают в `NOT IN`.

### Констрейнты (constraints)

**Определение:** констрейнт — это правило целостности данных, которое БД автоматически проверяет при `INSERT/UPDATE` (а иногда и при `DELETE`, если речь о FK).

Основные ограничения:
- `PRIMARY KEY`
- `FOREIGN KEY`
- `UNIQUE`
- `NOT NULL`
- `CHECK`
- `DEFAULT` (формально не ограничение целостности, но часть определения колонки)

Это всегда «свойство столбца»?
- **Не только.** Ограничения бывают:
  1. **column-level** (на уровне колонки), например `email TEXT UNIQUE`;
  2. **table-level** (на уровне таблицы), например составной `UNIQUE (country_code, phone)`.
- Это **не отдельный “технический столбец”**, а правило в метаданных схемы.

Можно ли добавить ограничение после `CREATE TABLE`?
- **Да**, через `ALTER TABLE ... ADD CONSTRAINT ...`.

#### Пример со структурой до/после

Создаём таблицу без части ограничений:
```sql
CREATE TABLE users (
  id BIGSERIAL,
  email TEXT,
  age INT,
  country_code TEXT,
  phone TEXT
);
```

Добавляем ограничения после создания:
```sql
ALTER TABLE users
  ADD CONSTRAINT users_pk PRIMARY KEY (id);

ALTER TABLE users
  ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
  ADD CONSTRAINT users_email_uq UNIQUE (email);

ALTER TABLE users
  ADD CONSTRAINT users_age_chk CHECK (age >= 18);

ALTER TABLE users
  ADD CONSTRAINT users_country_phone_uq UNIQUE (country_code, phone);
```

Структура **после** (логически):
```sql
-- users(
--   id BIGSERIAL PRIMARY KEY,
--   email TEXT NOT NULL UNIQUE,
--   age INT CHECK (age >= 18),
--   country_code TEXT,
--   phone TEXT,
--   UNIQUE(country_code, phone)
-- )
```

Пример внешнего ключа (ещё один констрейнт):
```sql
CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
  CONSTRAINT orders_user_fk
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE RESTRICT
);
```

### Отношения между таблицами, PK и FK

**Определение:** отношение между таблицами — это логическая связь записей одной таблицы с записями другой таблицы по ключам.

База этой связи:
- `PRIMARY KEY` — уникальный идентификатор строки в «родительской» таблице.
- `FOREIGN KEY` — ссылка на `PRIMARY KEY` (или `UNIQUE`) другой таблицы.

Как строятся отношения?
- Для **1:1** и **1:N** обычно через дополнительный FK-столбец в дочерней таблице.
- Для **N:M** всегда нужна отдельная таблица-связка (junction table), где обычно два FK.

#### 1:N (один ко многим)

Один клиент — много заказов.

```sql
CREATE TABLE customers (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
  CONSTRAINT orders_customer_fk
    FOREIGN KEY (customer_id)
    REFERENCES customers(id)
);
```

#### 1:1 (один к одному)

Один пользователь — один профиль.

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email TEXT NOT NULL UNIQUE
);

CREATE TABLE user_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  birthday DATE,
  avatar_url TEXT,
  CONSTRAINT profiles_user_fk
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);
```

Здесь `UNIQUE(user_id)` гарантирует, что у одного `users.id` не будет двух профилей.

#### N:M (многие ко многим)

Один студент может учиться на многих курсах, и один курс содержит многих студентов.

```sql
CREATE TABLE students (
  id BIGSERIAL PRIMARY KEY,
  full_name TEXT NOT NULL
);

CREATE TABLE courses (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL
);

CREATE TABLE student_courses (
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (student_id, course_id),
  CONSTRAINT student_courses_student_fk
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT student_courses_course_fk
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);
```

Да, составной `PRIMARY KEY (student_id, course_id)` в таблице-связке — это **корректно и часто лучший вариант**.

Почему это хорошая практика:
- пара `(student_id, course_id)` естественно уникальна;
- не даёт вставить дубликат связи;
- индекс по PK сразу помогает типичным join/поискам по этим двум полям;
- не нужен лишний surrogate-id, если сама связь и есть сущность.

Когда добавляют отдельный `id` в таблицу-связку:
- если на связь часто ссылаются другие таблицы по одному FK;
- если у связи появляется «своя жизнь» (статусы, workflow, audit как отдельная сущность);
- если удобнее использовать single-column FK в ORM/интеграциях.

Тогда best practice обычно такая:
- `id BIGSERIAL PRIMARY KEY` как технический ключ;
- плюс обязательный `UNIQUE (student_id, course_id)`, чтобы сохранить уникальность самой связи.

Пример такого варианта:
```sql
CREATE TABLE student_courses (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
  course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT student_courses_student_course_uq UNIQUE (student_id, course_id)
);
```

Итог:
- не каждое отношение — «через новый столбец в основной таблице»;
- для N:M правильный путь — отдельная таблица-связка;
- выбор между составным PK и surrogate-id зависит от того, самостоятельна ли сущность «связь».

### CTE (Common Table Expression)

CTE — временный именованный результат внутри **одного SQL-оператора** (`WITH ... SELECT/INSERT/UPDATE/DELETE/MERGE`).

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

Ответ на практический вопрос про область видимости:
- `WITH` действительно живёт «рядом» со своим оператором и действует только для него;
- ниже в этом же `SELECT` можно иметь вложенные подзапросы;
- эти подзапросы **могут использовать тот же CTE**, пока они внутри того же SQL-оператора.

Пример: один `WITH`, основной `SELECT` и подзапрос внутри него используют общий CTE.

```sql
WITH customer_totals AS (
  SELECT customer_id, SUM(total) AS sum_total
  FROM orders
  GROUP BY customer_id
)
SELECT c.id,
       c.name,
       ct.sum_total,
       (
         SELECT COUNT(*)
         FROM orders o
         WHERE o.customer_id = c.id
           AND o.total > ct.sum_total / 10
       ) AS expensive_orders_count
FROM customers c
JOIN customer_totals ct ON ct.customer_id = c.id;
```

Что важно помнить:
- в **следующем отдельном запросе** этот CTE уже недоступен;
- если нужно переиспользование между запросами, делают `VIEW`/`MATERIALIZED VIEW` или временную таблицу;
- можно объявлять несколько CTE через запятую в одном `WITH`.

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

#### Определения

- **Кластеризованный индекс** (в общем смысле СУБД) — индекс, определяющий физический порядок строк в таблице.
- **Некластеризованный индекс** — отдельная индексная структура, где хранятся ключи и ссылки на строки таблицы; физический порядок самих строк таблицы он не задаёт.

#### Как это в PostgreSQL

В PostgreSQL таблица обычно хранится как heap, а индексы — отдельные структуры.

- «Постоянного clustered-index режима» (как в MS SQL/InnoDB PK-order) у таблицы нет.
- Есть команда `CLUSTER`, которая разово переписывает таблицу в порядке выбранного индекса.

Создание и применение:
```sql
-- обычный (некластеризованный) индекс
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- физически упорядочить таблицу по этому индексу
CLUSTER orders USING idx_orders_created_at;

-- запомнить индекс для будущего CLUSTER без USING
ALTER TABLE orders CLUSTER ON idx_orders_created_at;
```

#### Как хранятся и как использует СУБД

Некластеризованные индексы (обычные для PostgreSQL):
- хранятся отдельно от heap-таблицы;
- содержат ключ + ссылку на строку (TID);
- planner выбирает `Index Scan`/`Bitmap Index Scan`, если это дешевле `Seq Scan`.

После `CLUSTER`:
- данные в heap-таблице физически становятся ближе к порядку индексного ключа;
- range-сканы по этому ключу могут читать меньше случайных страниц;
- но порядок со временем «размывается» новыми `INSERT/UPDATE/DELETE`.

Ограничения и практические нюансы:
- `CLUSTER` — тяжёлая операция переписывания таблицы, её делают периодически как maintenance;
- она не поддерживает порядок автоматически при каждой новой записи;
- обычно нужны `VACUUM/ANALYZE`, чтобы planner корректно оценивал планы после крупных изменений.

Практика выбора:
- по умолчанию создаём обычные индексы (`CREATE INDEX`);
- `CLUSTER` применяем точечно для больших таблиц с частыми range-сканами по одному ключу.

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

### VACUUM: что это и зачем

В PostgreSQL из-за MVCC старые версии строк не удаляются мгновенно. `VACUUM` очищает «мёртвые» версии строк и возвращает место для повторного использования внутри таблицы.

Ключевые формы:
- `VACUUM table_name;` — регулярная очистка без полного переписывания таблицы;
- `VACUUM (ANALYZE) table_name;` — очистка + обновление статистики для planner;
- `VACUUM FULL table_name;` — полное переписывание таблицы с физическим освобождением места на диске (тяжёлая операция).

Пример:
```sql
-- после массовых DELETE/UPDATE
VACUUM (ANALYZE) orders;
```

Зачем это нужно:
- снижает bloat таблиц/индексов;
- помогает поддерживать производительность чтения и записи;
- актуальная статистика (`ANALYZE`) улучшает планы запросов.

> В проде обычно полагаются на autovacuum, а ручной `VACUUM` используют точечно после крупных операций.

### Партиционирование

**Партиционирование** — одна логическая таблица, разбитая на физические части (partition) по правилу (`RANGE`, `LIST`, `HASH`).

Аналогия с «главами книги» в целом уместна:
- для приложения это всё ещё **одна таблица**;
- но физически данные лежат в нескольких дочерних таблицах;
- planner может прочитать только нужные партиции (partition pruning), что ускоряет запрос.

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

CREATE TABLE events_2026_01 PARTITION OF events
  FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE events_2026_02 PARTITION OF events
  FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
```

Пример, где pruning помогает:
```sql
SELECT *
FROM events
WHERE created_at >= DATE '2026-02-01'
  AND created_at <  DATE '2026-03-01';
```

Здесь PostgreSQL может обратиться только к `events_2026_02`, а не ко всем партициям.

### Шардирование

**Шардирование** — распределение данных по нескольким инстансам/серверам (shard-1, shard-2, ...).

Да, запросы могут отличаться от «один сервер / один инстанс».

#### На одном сервере (без шардирования)

Обычный `JOIN` работает прозрачно:
```sql
SELECT o.id, c.name, o.total
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.total > 1000;
```

#### В шардированной системе

Если `orders` и `customers` лежат на разных шардах, появляется два типовых сценария:

1. **Colocated join** (лучший вариант):
   - шардируем обе таблицы по одному ключу (`customer_id` / `id`),
   - связанные строки попадают на один shard,
   - `JOIN` можно выполнить локально на каждом шарде и затем собрать результаты.

2. **Cross-shard join**:
   - данные для `JOIN` разбросаны по разным шардам,
   - нужен coordinator/federation слой (или приложение),
   - дороже по сети и сложнее по latency.

Упрощённый пример через FDW (идея federated join в PostgreSQL):
```sql
-- локальная таблица orders_local, удалённая таблица customers_remote (foreign table)
SELECT o.id, c.name, o.total
FROM orders_local o
JOIN customers_remote c ON c.id = o.customer_id;
```

SQL синтаксически похож, но выполнение распределённое и обычно дороже.

### CQRS (Command Query Responsibility Segregation)

Идея: разделить модели/хранилища для:
- **Command** (запись),
- **Query** (чтение).

Почему это полезно (преимущества):
1. **Независимая оптимизация**
   - write-модель держим нормализованной и строгой по инвариантам;
   - read-модель делаем денормализованной и быстрой под конкретные экраны/отчёты.
2. **Масштабирование чтения отдельно от записи**
   - можно иметь много read-реплик/витрин, не перегружая OLTP ядро.
3. **Простые и быстрые запросы на чтение**
   - вместо тяжёлых `JOIN + GROUP BY` в реальном времени читаем предрасчитанные проекции.
4. **Изоляция сложной бизнес-логики записи**
   - сложные правила в command-части не утяжеляют query-часть.

Компромиссы:
- более сложная архитектура;
- eventual consistency (данные чтения могут обновляться с задержкой);
- нужны процессы синхронизации (events/outbox/ETL).

Мини-пример:
- `orders` (command): транзакционная таблица заказов.
- `sales_daily_read_model` (query): агрегированная витрина по дням.

```sql
-- query-модель: быстрый отчёт без тяжёлой агрегации "на лету"
SELECT day, total_amount
FROM sales_daily_read_model
WHERE day >= current_date - INTERVAL '30 days';
```

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
