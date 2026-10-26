# Spring Framework — Part 2 (вопросы 8–14)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Продолжение `g_spring_part1.md`: MVC, сквозная логика, REST, Security, Boot, Data и основные компоненты Spring Cloud.

---

## 8. Паттерны MVC и Front Controller в Spring (опционально)

### Что такое MVC?

**MVC (Model–View–Controller)** — архитектурный паттерн, разделяющий ответственность между тремя частями:

- **Model** — данные и состояние, с которыми работает приложение. В Spring MVC под этим словом могут подразумеваться как предметная модель, так и данные, передаваемые представлению через `Model`.
- **View** — представление результата, например HTML-шаблон.
- **Controller** — принимает запрос, координирует обработку и выбирает результат.

В классическом Spring MVC контроллер может вернуть имя шаблона, а данные передать через `Model`. В REST-приложении вместо HTML обычно возвращается тело HTTP-ответа — например, DTO, сериализуемый в JSON.

~~~java
@Controller
public class PageController {
    @GetMapping("/greeting")
    public String greeting(Model model) {
        model.addAttribute("message", "Hello!");
        return "greeting"; // имя шаблона, например greeting.html
    }
}
~~~

REST-вариант:

~~~java
@RestController
@RequestMapping("/api/greetings")
public class GreetingController {
    @GetMapping
    public GreetingDto greeting() {
        return new GreetingDto("Hello!");
    }
}

public record GreetingDto(String message) {}
~~~

### Что такое Front Controller?

**Front Controller** — паттерн, в котором входящие запросы проходят через единый центральный компонент. Он выполняет общие задачи маршрутизации и координации, после чего передаёт запрос подходящему обработчику.

В Spring MVC эту роль играет **`DispatcherServlet`**. Он не содержит бизнес-логику конкретных endpoint'ов, а координирует обработку HTTP-запроса.

Упрощённый путь запроса:

~~~text
HTTP request
    |
    v
Servlet Filter chain
    |
    v
DispatcherServlet (Front Controller)
    |
    +--> HandlerMapping: найти обработчик
    |
    +--> HandlerAdapter: вызвать обработчик
    |
    +--> Controller
    |
    +--> обработка результата / ViewResolver
         или HttpMessageConverter для тела ответа
    |
    v
HTTP response
~~~

Для REST-контроллера объект, возвращённый из метода с `@ResponseBody` или `@RestController`, обычно преобразуется в тело ответа через подходящий `HttpMessageConverter` (например, Jackson для JSON). Для MVC с представлениями имя view разрешается через `ViewResolver`.

### Зачем нужен `DispatcherServlet`?

Он централизует диспетчеризацию запросов и взаимодействует с инфраструктурой Spring MVC, в частности:

- `HandlerMapping` определяет, какой обработчик соответствует запросу;
- `HandlerAdapter` вызывает найденный обработчик;
- `HandlerExceptionResolver` помогает преобразовать исключения в подходящий ответ;
- `ViewResolver` выбирает представление для MVC;
- `HttpMessageConverter` читает тела запросов и записывает тела ответов.

**Важно:** servlet-фильтры обычно выполняются до `DispatcherServlet`; Spring MVC interceptors работают внутри механизма диспетчеризации. Поэтому это разные уровни обработки.

**Короткий ответ на собеседовании:** Spring MVC использует MVC как способ разделения ответственности, а `DispatcherServlet` реализует Front Controller — единую точку входа, которая находит обработчик и координирует выполнение запроса.

---

## 9. AOP в Spring. Фильтры, интерсепторы, листенеры

### Для чего нужен AOP?

**AOP (Aspect-Oriented Programming, аспектно-ориентированное программирование)** помогает вынести сквозную функциональность из бизнес-логики. Сквозная функциональность — поведение, нужное многим компонентам: транзакции, аудит, логирование, метрики, проверка доступа или кэширование.

Основные понятия:

- **Aspect** — модуль, описывающий сквозное поведение.
- **Advice** — действие, выполняемое в определённый момент относительно вызова метода: до, после, при возврате или при исключении.
- **Pointcut** — выражение, выбирающее точки, к которым применяется advice.
- **Join point** — точка выполнения, к которой можно привязать аспект. В Spring proxy-based AOP это прежде всего вызовы методов бинов.

Пример логирования времени выполнения:

~~~java
@Aspect
@Component
public class TimingAspect {
    @Around("within(ru.example.service..*)")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long started = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.nanoTime() - started;
            System.out.println(joinPoint.getSignature() + ": " + elapsed + " ns");
        }
    }
}
~~~

Для работы аспектов обычно нужна соответствующая AOP-инфраструктура; в Spring Boot её часто подключают через `spring-boot-starter-aop`.

По умолчанию Spring AOP использует прокси — JDK dynamic proxy для интерфейсов или class-based proxy в зависимости от настроек и типа бина. Это не то же самое, что полное weaving-возможности AspectJ.

**Ограничения proxy-based AOP:**
- вызов должен проходить через прокси;
- внутренний вызов через `this.someMethod()` обходит прокси;
- `private`-метод не может быть перехвачен обычным Spring proxy-based AOP;
- AOP не заменяет явную бизнес-логику и не должен скрывать важные побочные эффекты.

### Filter, Interceptor и AOP — чем отличаются?

Эти механизмы могут решать похожие задачи, но работают на разных уровнях.

| Механизм | Где работает | Типичные задачи |
|---|---|---|
| Servlet `Filter` | Servlet-контейнер, до/после servlet | CORS, обработка request/response, correlation ID, обёртки тела, низкоуровневая HTTP-логика |
| Spring MVC `HandlerInterceptor` | Внутри Spring MVC, вокруг вызова handler | Логирование endpoint, проверка метаданных запроса, измерение времени обработки |
| Spring AOP | Вокруг вызовов методов Spring-бинов через прокси | Транзакции, кэширование, методный аудит, метрики, политики на уровне сервисов |
| Listener / event listener | Реагирует на событие | Реакция на событие приложения, завершение контекста, доменное событие, событие servlet |

#### Servlet Filter

Фильтр получает `HttpServletRequest` и `HttpServletResponse` и может продолжить цепочку вызовом `chain.doFilter(request, response)`. Если не вызвать его, дальнейшая обработка не продолжится.

~~~java
@Component
public class RequestIdFilter implements Filter {
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestId = httpRequest.getHeader("X-Request-Id");

        // В реальном коде здесь можно сгенерировать ID,
        // положить его в MDC и гарантированно очистить MDC в finally.
        chain.doFilter(request, response);
    }
}
~~~

Порядок фильтров важен. Например, аутентификация и CORS должны быть настроены на подходящем этапе цепочки. Spring Security сама интегрируется с servlet-цепочкой через специальный фильтр.

#### Spring MVC Interceptor

У `HandlerInterceptor` есть основные callback-методы:

- `preHandle` — до вызова контроллера; возвращение `false` прекращает дальнейшую обработку;
- `postHandle` — после контроллера, но до рендеринга view; не всегда вызывается при REST-ответе так, как ожидают, особенно при исключениях;
- `afterCompletion` — после завершения обработки, в том числе при наличии исключения, если interceptor был успешно запущен.

~~~java
@Component
public class ApiTimingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        request.setAttribute("startedAt", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        Object startedAt = request.getAttribute("startedAt");
        if (startedAt instanceof Long start) {
            long elapsed = System.nanoTime() - start;
            // Записать метрику, не логируя чувствительные данные.
        }
    }
}
~~~

Interceptor подключают через `WebMvcConfigurer#addInterceptors`. Он знает о выбранном MVC handler, но не является универсальным механизмом для всех servlet-запросов.

Не стоит использовать interceptor как единственный механизм защиты. Для аутентификации и авторизации обычно используют Spring Security.

#### Listeners и события

Слово *listener* охватывает несколько разных механизмов:

- Servlet listeners (например, `ServletContextListener`) реагируют на события servlet-контейнера.
- Spring application listeners реагируют на события контекста приложения.
- `@EventListener` позволяет слушать события Spring.
- Доменные события могут сообщать о факте, произошедшем в бизнес-процессе.

~~~java
public record OrderCreatedEvent(long orderId) {}

@Service
public class OrderService {
    private final ApplicationEventPublisher events;

    public OrderService(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void createOrder(long id) {
        // Сохранить заказ и выполнить бизнес-операции.
        events.publishEvent(new OrderCreatedEvent(id));
    }
}

@Component
public class OrderAuditListener {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // Например, записать событие в локальный аудит.
    }
}
~~~

Обычный `@EventListener` выполняется в рамках обычного механизма публикации событий; сам по себе он не означает асинхронность или гарантированную доставку. `@Async` меняет способ выполнения при соответствующей настройке, но не делает событие надёжным сообщением. Если событие должно гарантированно пережить сбой процесса и доставляться внешним системам, следует рассмотреть брокер сообщений и, например, transactional outbox.

**Как выбрать механизм:**
- нужно вмешаться в servlet request/response — Filter;
- нужна информация о выбранном MVC handler — Interceptor;
- нужно оборачивать вызовы методов Spring-бинов — AOP;
- нужно реагировать на опубликованное событие — Listener.

---

## 10. REST

### Что такое REST?

**REST (Representational State Transfer)** — архитектурный стиль построения распределённых систем. REST — не протокол и не синоним JSON API. На практике REST API обычно использует HTTP, ресурсы, URI и представления ресурсов.

Ключевые ограничения REST в классической формулировке:

1. **Client–Server** — клиент и сервер разделяют ответственность.
2. **Stateless** — каждый запрос содержит всю информацию, необходимую для его обработки; сервер не должен полагаться на сохранённый контекст конкретной клиентской сессии между запросами.
3. **Cacheable** — ответы должны явно или неявно определять возможность кэширования.
4. **Uniform Interface** — единообразный интерфейс взаимодействия с ресурсами.
5. **Layered System** — клиент может взаимодействовать с системой через промежуточные слои.
6. **Code on Demand** — необязательное ограничение, позволяющее серверу передавать исполняемый код клиенту.

REST не требует JSON, а HTTP API не становится автоматически RESTful только потому, что использует GET и POST.

### Ресурсы и URI

Обычно URI именует ресурс, а HTTP-метод описывает действие над ним.

~~~text
GET    /api/orders          — получить список заказов
POST   /api/orders          — создать заказ
GET    /api/orders/42       — получить заказ 42
PUT    /api/orders/42       — заменить состояние заказа 42
PATCH  /api/orders/42       — частично изменить заказ 42
DELETE /api/orders/42       — удалить заказ 42
~~~

Часто используют существительные во множественном числе: `/orders`, `/users`, `/payments`. Это соглашение, а не обязательное требование протокола. Вложенные URI вроде `/users/12/orders` могут обозначать связанные ресурсы, но чрезмерная глубина усложняет API.

### Основные HTTP-методы

| Метод | Назначение | Safe? | Идемпотентен? |
|---|---|---|---|
| `GET` | Получить представление ресурса | Да | Да |
| `HEAD` | Получить заголовки как для GET, без тела ответа | Да | Да |
| `POST` | Передать данные на обработку, часто создать ресурс | Нет | По умолчанию нет |
| `PUT` | Создать или полностью заменить состояние ресурса по URI | Нет | Да |
| `PATCH` | Частично изменить ресурс | Нет | Не обязательно |
| `DELETE` | Удалить ресурс | Нет | Да по семантике метода |
| `OPTIONS` | Узнать поддерживаемые параметры взаимодействия | Да | Да |

**Safe** означает, что клиент не запрашивает изменение состояния ресурса. Сервер при этом может записывать логи, метрики и другую внутреннюю информацию. Идемпотентность означает, что повтор одного и того же запроса имеет тот же предполагаемый эффект на состояние сервера, что и однократное выполнение; это не гарантирует идентичный статус-код или идентичное тело каждого ответа.

#### POST, PUT и PATCH

- **POST** — сервер определяет, как обработать представленные данные. Частый сценарий — создание ресурса с назначаемым сервером ID: `POST /orders`.
- **PUT** — клиент указывает URI ресурса, состояние которого нужно создать или заменить: `PUT /orders/42`. Повторная отправка того же представления должна приводить к тому же состоянию.
- **PATCH** — применяет частичные изменения. Операция может быть идемпотентной, но это зависит от конкретной семантики patch-документа. Например, «установить статус в PAID» может быть идемпотентным, а «увеличить баланс на 10» — нет.

### Из чего состоит HTTP request?

Упрощённо HTTP-запрос состоит из:

1. **Request line** в HTTP/1.1: метод, request-target, версия протокола.
2. **Headers** — метаданные: `Host`, `Accept`, `Content-Type`, `Authorization`, `If-Match` и другие.
3. **Пустая строка**, отделяющая заголовки от тела.
4. **Body** — необязательное тело запроса.

~~~http
POST /api/orders HTTP/1.1
Host: example.com
Content-Type: application/json
Accept: application/json
Authorization: Bearer <access-token>

{
  "productId": 17,
  "quantity": 2
}
~~~

В HTTP/2 и HTTP/3 представление на транспортном уровне отличается от текстовой request line HTTP/1.1, но логическая модель метода, цели запроса, заголовков и содержимого остаётся полезной.

Частые элементы:
- **Path** — путь ресурса, например `/api/orders/42`.
- **Query parameters** — параметры выборки или фильтрации, например `?status=PAID&page=0`.
- **Path variables** — часть пути, например `42`.
- **Headers** — метаданные и согласование формата.
- **Body** — данные, передаваемые в запросе.

В Spring MVC:
- `@PathVariable` связывает часть пути;
- `@RequestParam` связывает query-параметр;
- `@RequestHeader` читает заголовок;
- `@RequestBody` преобразует тело запроса в объект через `HttpMessageConverter`.

~~~java
@PostMapping("/api/orders")
public ResponseEntity<OrderDto> create(
        @RequestHeader("X-Request-Id") String requestId,
        @Valid @RequestBody CreateOrderRequest request) {
    OrderDto created = orderService.create(request);
    URI location = URI.create("/api/orders/" + created.id());
    return ResponseEntity.created(location).body(created); // 201 + Location
}
~~~

### Из чего состоит HTTP response?

Ответ включает:

1. **Status line** в HTTP/1.1: версия протокола, статус-код и пояснение.
2. **Headers** — метаданные ответа.
3. Пустую строку.
4. **Body** — необязательное тело.

~~~http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/orders/42

{
  "id": 42,
  "status": "CREATED"
}
~~~

Полезные заголовки:
- `Content-Type` — формат тела ответа;
- `Cache-Control`, `ETag`, `Last-Modified` — управление кэшированием и проверкой версии;
- `Location` — URI созданного ресурса или другого целевого ресурса;
- `Retry-After` — когда клиенту можно повторить запрос, если это применимо;
- `WWW-Authenticate` — параметры challenge при некоторых ответах 401.

### Основные HTTP status codes

| Код | Значение | Типичный сценарий |
|---|---|---|
| `200 OK` | Успешная обработка | GET или успешная операция с телом ответа |
| `201 Created` | Ресурс создан | POST, часто с `Location` |
| `202 Accepted` | Запрос принят, обработка ещё не завершена | Асинхронная задача |
| `204 No Content` | Успешно, тело отсутствует | DELETE или обновление без тела |
| `301/302` | Перенаправление | Redirect; точная семантика зависит от кода |
| `304 Not Modified` | Представление не изменилось | Условный GET при кэшировании |
| `400 Bad Request` | Некорректный запрос | Синтаксис или формат данных неверен |
| `401 Unauthorized` | Нет действительных credentials | Не аутентифицирован или токен недействителен |
| `403 Forbidden` | Доступ запрещён | Аутентификация есть, но прав недостаточно |
| `404 Not Found` | Ресурс не найден или не раскрывается | Неизвестный ID |
| `409 Conflict` | Конфликт с текущим состоянием | Дубликат, конфликт бизнес-состояния |
| `412 Precondition Failed` | Не выполнено предусловие | Например, не совпал `If-Match` |
| `415 Unsupported Media Type` | Неподдерживаемый тип тела | Неподдерживаемый `Content-Type` |
| `422 Unprocessable Content` | Синтаксически корректное, но семантически невалидное содержимое | Ошибка бизнес-валидации, если API использует этот статус |
| `429 Too Many Requests` | Слишком много запросов | Rate limiting |
| `500 Internal Server Error` | Неожиданная ошибка сервера | Необработанная внутренняя ошибка |
| `502/503/504` | Ошибка шлюза / сервис недоступен / timeout шлюза | Проблема в цепочке сервисов |

Не нужно возвращать `200 OK` на любую ситуацию. Статус должен честно описывать результат. Также `401` и `403` не взаимозаменяемы: 401 связан с аутентификацией, 403 — с запретом доступа; конкретные API могут сознательно не раскрывать существование ресурса и возвращать 404.

### Идемпотентность

Операция **идемпотентна**, если её повторение не меняет итоговый эффект после первого успешного выполнения.

Примеры:
- `PUT /users/42` с одинаковым представлением — итоговое состояние пользователя остаётся тем же;
- повторный `DELETE /users/42` — ресурс остаётся удалённым, хотя ответы могут различаться (например, первый 204, второй 404);
- `POST /payments` часто не идемпотентен: повтор может создать второй платёж.

Для финансовых и других чувствительных операций часто применяют **Idempotency-Key**. Клиент генерирует уникальный ключ для одной логической операции, а сервер атомарно связывает его с результатом. При повторе с тем же ключом сервер возвращает ранее сохранённый результат, а не повторяет побочный эффект.

Важно:
- ключ должен иметь определённую область уникальности и срок хранения;
- запись ключа и выполнение бизнес-операции должны быть согласованы транзакционно или иным надёжным способом;
- повтор с тем же ключом, но другим телом запроса должен обрабатываться как конфликт, а не как новая операция;
- один лишь заголовок `Idempotency-Key` ничего не гарантирует без серверной реализации.

Идемпотентность особенно важна при сетевых таймаутах: клиент не всегда знает, успел ли сервер выполнить операцию до потери ответа.

### Типичные проблемы при проектировании REST API

**1. Неподходящие статусы и ошибки.** Возвращайте понятные статус-коды и единый формат ошибки. Не раскрывайте stack trace, SQL, внутренние адреса и секреты.

**2. Валидация и контракт.** Проверяйте входные DTO через Bean Validation (`@Valid`, `@NotNull`, `@Positive` и т. п.) и возвращайте предсказуемые ошибки. Не принимайте напрямую JPA-сущности как публичный контракт.

**3. Пагинация и фильтрация.** Не возвращайте бесконечно большие коллекции. Ограничивайте размер страницы и поддерживайте сортировку с устойчивым порядком.

**4. Совместимость API.** Изменение имени/типа поля, обязательности поля или семантики endpoint может сломать клиентов. Продумывайте версионирование и обратную совместимость.

**5. Конкурентные обновления.** Два клиента могут прочитать старую версию ресурса и перезаписать изменения друг друга. Для защиты можно использовать optimistic locking, `ETag` и `If-Match` или версию в модели.

**6. Повторы и таймауты.** Повтор GET обычно безопасен; повтор POST может создать дубликат. Retry-политика должна учитывать идемпотентность, timeout и возможность частичного выполнения.

**7. Безопасность.** Проверяйте права на конкретный ресурс, а не только на endpoint; валидируйте входные данные; ограничивайте частоту запросов; не доверяйте ID, пришедшему от клиента.

**8. Слишком много запросов.** Для связанных данных возможны N+1 запросов в БД, чрезмерная болтливость API и большие payload. Помогают подходящая модель ресурсов, пагинация, batch-запросы и измерение производительности.

**9. Кэширование.** Кэшировать можно не всё. Персональные или чувствительные данные требуют корректной настройки `Cache-Control` и разделения кэша между пользователями.

**10. Документация и наблюдаемость.** Поддерживайте OpenAPI-контракт, correlation/request ID, метрики и структурированные логи, не записывая секреты и персональные данные без необходимости.

### REST endpoint в Spring: хороший минимальный пример

~~~java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDto created = service.create(request);
        URI location = URI.create("/api/orders/" + created.id());
        return ResponseEntity.created(location).body(created);
    }
}
~~~

В реальном приложении нужно также определить обработку невалидных DTO, отсутствие ресурса и бизнес-конфликтов — обычно через `@RestControllerAdvice` и согласованный формат ошибки.

**Короткий ответ на собеседовании:** REST моделирует ресурсы и их представления, а HTTP-методы задают семантику взаимодействия. Нужно понимать структуру request/response, статусы, безопасность, кэширование, конкуренцию и идемпотентность повторов.

---

## 11. Spring Security (кратко)

### Что такое Spring Security?

**Spring Security** — фреймворк для аутентификации, авторизации и защиты от распространённых атак. В servlet-приложениях его основная HTTP-интеграция реализована через цепочку servlet-фильтров.

Три понятия:
- **Authentication (аутентификация)** — кто пользователь и подтверждены ли его credentials.
- **Authorization (авторизация)** — разрешено ли этому пользователю выполнять конкретное действие.
- **SecurityContext** — контекст текущей аутентификации, обычно доступный в рамках обработки запроса.

Упрощённый поток:

~~~text
HTTP request
   |
   v
Spring Security Filter Chain
   |
   +--> извлечь credentials / проверить токен
   |
   +--> установить Authentication в SecurityContext
   |
   +--> проверить правила доступа
   |
   v
Controller / service
~~~

### Как задаются правила доступа?

В современных конфигурациях обычно объявляют бин `SecurityFilterChain`, а не наследуются от устаревшего `WebSecurityConfigurerAdapter`.

~~~java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }
}
~~~

Это лишь пример правил для приложения с form login; способ аутентификации выбирают под задачу. Для REST API с Bearer JWT часто настраивают OAuth2 Resource Server, а не form login.

### Роли и authorities

- `GrantedAuthority` — отдельное право, например `invoice:read`.
- Роль — распространённая договорённость для группы прав. В Spring `hasRole("ADMIN")` обычно проверяет authority `ROLE_ADMIN`.
- `hasAuthority("invoice:read")` проверяет конкретное право без автоматического добавления префикса роли.

Авторизацию полезно проверять не только на URL, но и на уровне методов/бизнес-операций, например с помощью method security и `@PreAuthorize`.

### Сессия, CSRF и JWT

- **Session-based authentication**: сервер хранит состояние сессии, клиент обычно передаёт session cookie.
- **JWT Bearer authentication**: клиент передаёт токен в заголовке `Authorization: Bearer ...`; resource server проверяет подпись, срок действия и необходимые claims.
- **CSRF** актуален прежде всего там, где браузер автоматически прикладывает credentials (например, cookies). Нельзя механически отключать CSRF во всех приложениях только потому, что API «REST». Для stateless API с Bearer-токеном, который браузер не прикладывает автоматически, модель угроз отличается.
- JWT — формат токена, а не сам по себе протокол входа. Токен нужно валидировать, защищать секреты/ключи, задавать срок жизни и продумывать отзыв или ротацию.

Дополнительно важно: хранить пароли в виде открытого текста нельзя; используют адаптивные password encoders, например BCrypt или Argon2. HTTPS обязателен для защиты credentials и токенов в пути.

**Короткий ответ на собеседовании:** Spring Security строит цепочку фильтров, которая обрабатывает credentials и применяет правила доступа. Нужно различать аутентификацию и авторизацию, понимать роли/права, сессии, JWT и CSRF.

---

## 12. Spring Boot

### Зачем нужен Spring Boot?

**Spring Boot** упрощает создание и запуск приложений на базе Spring. Он не заменяет Spring Framework, а собирает распространённые настройки и инфраструктуру в удобные соглашения.

Основные преимущества:

- **Auto-configuration** — пытается автоматически настроить компоненты на основании classpath, свойств и уже объявленных бинов.
- **Starters** — удобные наборы зависимостей для типовых задач.
- **Встроенный сервер** — для web-приложения обычно можно запускать приложение как самостоятельный процесс без отдельного развёртывания WAR в установленный контейнер.
- **Externalized configuration** — настройки можно задавать через properties/YAML, переменные окружения и аргументы запуска.
- **Production-ready features** — Actuator предоставляет health, metrics и другие operational endpoints.
- **Opinionated defaults** — разумные значения по умолчанию сокращают конфигурацию, но их можно переопределять.

Типичный entry point:

~~~java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
~~~

`@SpringBootApplication` объединяет `@SpringBootConfiguration`, `@EnableAutoConfiguration` и `@ComponentScan`.

### Как работает auto-configuration?

Автоконфигурация предлагает бины на основе условий: например, наличие определённых классов в classpath, заданных свойств или отсутствие пользовательского бина. Поэтому добавление зависимости может включить новую конфигурацию, но не означает, что Boot безусловно создаст все возможные компоненты.

Полезно знать:
- `@ConditionalOnClass` — класс присутствует в classpath;
- `@ConditionalOnMissingBean` — пользователь не объявил подходящий бин;
- `@ConditionalOnProperty` — включение зависит от свойства;
- `@ConfigurationProperties` — типизированное связывание внешних настроек с объектом.

При проблемах автоконфигурацию можно диагностировать, включая condition evaluation report (например, запуск с `--debug`).

### Что такое Spring Boot Actuator?

**Actuator** добавляет endpoints для наблюдения и управления состоянием приложения. Обычно подключается зависимостью `spring-boot-starter-actuator`.

Примеры endpoints (доступность зависит от версии и конфигурации):

- `/actuator/health` — health status и, при разрешённой детализации, состояние проверок;
- `/actuator/info` — сведения о приложении, если они настроены;
- `/actuator/metrics` — доступные метрики;
- `/actuator/prometheus` — формат метрик для Prometheus при наличии соответствующей зависимости и настройке;
- `/actuator/env`, `/actuator/configprops`, `/actuator/beans` — диагностическая информация, которую не следует открывать всем подряд.

Подключение зависимости само по себе не означает, что все endpoints доступны по HTTP. Обычно наружу публикуют только необходимый минимум, например health, а endpoints с конфигурацией и диагностическими данными защищают. Для Kubernetes часто используют liveness/readiness probes; **liveness** отвечает на вопрос, жив ли процесс, а **readiness** — готов ли он принимать трафик.

~~~properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.probes.enabled=true
~~~

Не размещайте Actuator бездумно на публичном интерфейсе: endpoint может раскрыть внутренние данные или предоставить опасные операции. Настройка сетевого доступа и Spring Security — часть конфигурации.

### Как написать собственный Spring Boot starter?

Стартер обычно делят на два артефакта:

1. **Autoconfigure module** — содержит конфигурационные классы, условия и создаваемые бины.
2. **Starter module** — подтягивает autoconfigure и зависимости, необходимые для типичного сценария использования.

В небольшом внутреннем проекте их иногда объединяют, но разделение делает назначение артефактов яснее.

#### Шаг 1. Создать properties-класс

~~~java
@ConfigurationProperties(prefix = "acme.greeting")
public class GreetingProperties {
    private String prefix = "Hello";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
~~~

#### Шаг 2. Написать компонент и автоконфигурацию

~~~java
public class GreetingService {
    private final GreetingProperties properties;

    public GreetingService(GreetingProperties properties) {
        this.properties = properties;
    }

    public String greet(String name) {
        return properties.getPrefix() + ", " + name + "!";
    }
}
~~~

~~~java
@AutoConfiguration
@EnableConfigurationProperties(GreetingProperties.class)
@ConditionalOnClass(GreetingService.class)
public class GreetingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    GreetingService greetingService(GreetingProperties properties) {
        return new GreetingService(properties);
    }
}
~~~

`@ConditionalOnMissingBean` позволяет приложению заменить стандартную реализацию собственным бином.

#### Шаг 3. Зарегистрировать автоконфигурацию

Для современного механизма Spring Boot в autoconfigure-модуле создают файл:

~~~text
src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
~~~

В нём указывают полное имя класса, например:

~~~text
com.acme.greeting.GreetingAutoConfiguration
~~~

Для актуальных версий Boot используют механизм `AutoConfiguration.imports`; не следует бездумно копировать старые примеры, которые регистрировали автоконфигурацию только через `spring.factories`. При разработке под конкретную версию Boot нужно сверяться с документацией именно этой версии.

#### Шаг 4. Собрать starter-артефакт

Starter должен подтягивать autoconfigure-модуль и нужные runtime-зависимости. Потребитель подключает стартер и задаёт свойства:

~~~properties
acme.greeting.prefix=Welcome
~~~

После этого Boot автоматически создаёт `GreetingService`, если условия выполнены и приложение не предоставило свой бин.

#### Шаг 5. Протестировать

Проверьте как минимум:
- автоконфигурация включается, когда нужные классы присутствуют;
- пользовательский бин заменяет стандартный;
- свойства связываются корректно;
- конфигурация не активируется, когда условие не выполнено.

Для тестирования автоконфигурации полезен `ApplicationContextRunner`. Важно также выбрать совместимые версии Spring Boot и библиотек, описать metadata для IDE и документировать свойства. Не называйте любой набор зависимостей «стартером с автоконфигурацией»: starter отвечает за подключение зависимостей, а поведение создаёт autoconfigure-модуль.

**Короткий ответ на собеседовании:** Boot даёт автоконфигурацию, стартеры, внешний конфиг и operational endpoints. Собственный стартер обычно включает модуль автоконфигурации с условиями и бинами, регистрацию через `AutoConfiguration.imports` и отдельный артефакт-зависимость.

---

## 13. Spring Data (кратко)

### Что такое Spring Data?

**Spring Data** — семейство проектов, упрощающее доступ к разным хранилищам: реляционным БД через JPA/JDBC, MongoDB, Redis, Elasticsearch и другим. Оно предоставляет единообразные подходы к репозиториям, запросам, пагинации и сортировке, но детали зависят от конкретного модуля.

Не путайте:
- **JPA** — Java-спецификация ORM;
- **Hibernate** — популярная реализация JPA;
- **Spring Data JPA** — интеграция Spring Data с JPA, которая упрощает создание репозиториев и выполнение запросов.

### Репозитории

В Spring Data JPA часто достаточно объявить интерфейс:

~~~java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByStatusAndCreatedAtAfter(
            UserStatus status,
            Instant createdAt);
}
~~~

Spring Data генерирует реализацию репозитория во время работы приложения. Имена методов могут описывать условия запроса. Для более сложных запросов используют `@Query`, спецификации, Criteria API или другие поддерживаемые механизмы.

Частые базовые интерфейсы:
- `Repository` — маркерный базовый интерфейс;
- `CrudRepository` — основные CRUD-операции;
- `PagingAndSortingRepository` — пагинация и сортировка (точная иерархия зависит от версии Spring Data);
- `JpaRepository` — дополнительные операции для JPA, включая возможности работы с batch/flush и списками.

### Пагинация и сортировка

~~~java
Page<User> findByStatus(
        UserStatus status,
        Pageable pageable);
~~~

Пример вызова:

~~~java
Pageable pageable = PageRequest.of(
        0,
        20,
        Sort.by(Sort.Direction.DESC, "createdAt"));
Page<User> page = userRepository.findByStatus(ACTIVE, pageable);
~~~

`Page` обычно содержит данные страницы и метаданные, включая общее число элементов, что может потребовать отдельного count-запроса. `Slice` позволяет узнать, есть ли следующая порция, и в ряде случаев избегает подсчёта общего числа записей.

### Что важно знать для собеседования?

- Derived queries удобны для простых условий, но слишком длинные имена методов ухудшают читаемость.
- `@Query` полезна для явно описанных запросов; параметры нужно связывать безопасно, не конкатенировать пользовательский ввод в SQL.
- Транзакционные границы нужно проектировать осознанно. Репозиторий не заменяет сервисный слой, где обычно координируются несколько операций.
- В JPA нужно понимать persistence context, dirty checking, lazy/eager loading, каскады, N+1 проблему и optimistic locking.
- `save()` не означает «всегда выполнить INSERT»: поведение зависит от состояния сущности и реализации. Также `save()` не обязательно немедленно отправляет SQL в БД — синхронизация может произойти позже, например при flush/commit.
- Транзакции Spring Data JPA обычно опираются на JPA transaction manager и границы транзакций Spring; важно, чтобы выбранный менеджер соответствовал ресурсу.
- Для интеграционных тестов проверяйте поведение на настоящей БД или Testcontainers, когда важны SQL, ограничения, блокировки и диалект.

**Короткий ответ на собеседовании:** Spring Data уменьшает шаблонный код доступа к данным. В Spring Data JPA достаточно объявить репозиторий, а фреймворк создаёт реализацию; при этом разработчик всё равно отвечает за транзакции, модель данных, запросы и производительность.

---

## 14. Spring Cloud (кратко)

### Зачем нужен Spring Cloud?

**Spring Cloud** — набор проектов для типичных задач распределённых систем и микросервисов: маршрутизации, обнаружения сервисов, внешней конфигурации, декларативных HTTP-клиентов, отказоустойчивости и наблюдаемости.

Компоненты Spring Cloud — не обязательный набор для любого микросервиса. Их выбирают под конкретную инфраструктуру. Нужно также согласовывать версии Spring Boot и Spring Cloud по официальной таблице совместимости.

### Spring Cloud Gateway

**Spring Cloud Gateway** — API Gateway на Spring, который принимает входящие запросы и маршрутизирует их к downstream-сервисам. Поддерживает predicates (условия маршрута) и filters (преобразование запроса/ответа).

Пример концепции маршрута:
- запросы `/api/users/**` направлять в user-service;
- добавить correlation ID;
- проверить или передать данные аутентификации;
- применить rate limiting или другие общие политики.

Gateway может централизовать cross-cutting concerns на границе системы, но не должен автоматически становиться местом для бизнес-логики каждого сервиса. У Spring Cloud Gateway существуют разные варианты/модели исполнения; при выборе учитывайте, нужен ли reactive/WebFlux вариант или MVC-вариант, доступный в используемой версии.

### Spring Cloud Netflix Eureka

**Eureka** — сервисный реестр и механизм service discovery. Инстансы регистрируются в Eureka Server, а клиенты узнают адреса доступных сервисов через реестр.

Зачем это нужно: в динамической среде адрес и число инстансов могут меняться, поэтому конфигурация не должна навсегда зависеть от фиксированного IP каждого сервиса.

Eureka не является балансировщиком сам по себе во всех сценариях; обнаружение сервисов обычно сочетается с клиентской или серверной балансировкой. В современной инфраструктуре альтернативой может быть service discovery платформы (например, Kubernetes DNS), поэтому Eureka нужна не всегда.

### Spring Cloud OpenFeign

**OpenFeign** — декларативный HTTP-клиент: разработчик описывает интерфейс клиента, а библиотека реализует вызовы удалённого HTTP API.

~~~java
@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @GetMapping("/api/products/{id}")
    ProductDto findById(@PathVariable("id") long id);
}
~~~

Это уменьшает boilerplate по сравнению с ручной сборкой HTTP-запросов. Но удалённый вызов всё равно может завершиться timeout, сетевой ошибкой или ошибкой сервиса. Нужно задавать разумные таймауты, учитывать сериализацию и не считать сетевой вызов эквивалентом обычного вызова метода.

Интеграция с service discovery и load balancing зависит от подключённых модулей и конфигурации. Не предполагайте, что одна аннотация `@FeignClient` автоматически обеспечивает все инфраструктурные функции.

### Spring Cloud Sleuth и tracing

**Spring Cloud Sleuth** исторически добавлял распределённую трассировку, correlation IDs и интеграции с системами tracing в Spring Cloud-приложениях. Однако Sleuth был предназначен для поколения Spring Boot 2.x и Spring Cloud 2021.0 и **не является стандартным решением для новых приложений на Spring Boot 3+**.

Для современных приложений обычно используют **Micrometer Tracing**, часто вместе с Micrometer Observation и экспортом через OpenTelemetry/OTLP либо совместимые backend-системы. Конкретный стек зависит от версии Boot и инфраструктуры.

Распределённая трассировка помогает связать одну операцию, проходящую через несколько сервисов, и увидеть span'ы, задержки и ошибки на каждом участке. Trace ID связывает трассу, а span описывает отдельный участок работы. Это не то же самое, что обычный лог: логи — отдельные записи событий, tracing — структура пути запроса через систему.

### Spring Cloud Circuit Breaker

**Circuit Breaker** защищает систему от повторных вызовов зависимого сервиса, который временно не справляется с нагрузкой или постоянно падает.

Упрощённые состояния:
- **CLOSED** — вызовы проходят, ошибки учитываются;
- **OPEN** — вызовы блокируются на период ожидания;
- **HALF_OPEN** — пропускаются пробные вызовы, чтобы проверить восстановление.

Circuit breaker отличается от retry:
- **Retry** повторяет неудачную операцию и может усилить нагрузку при массовом отказе.
- **Circuit breaker** прекращает новые вызовы при признаках отказа.
- **Timeout** ограничивает время ожидания.
- **Bulkhead** изолирует ресурсы и ограничивает влияние медленной зависимости.
- **Fallback** возвращает альтернативный результат, но он должен быть семантически корректным.

Spring Cloud Circuit Breaker предоставляет абстракцию над реализациями, например Resilience4j (в зависимости от выбранной интеграции). Не следует включать повторные попытки для неидемпотентных операций без дополнительной защиты от дубликатов.

### Spring Cloud Config

Вероятно, под «SC Confog» имеется в виду **Spring Cloud Config**. Это инфраструктура внешней конфигурации для распределённых приложений.

- **Config Server** предоставляет конфигурацию клиентским приложениям.
- Конфигурационные данные могут храниться, например, в Git-репозитории или другом поддерживаемом источнике.
- Клиенты получают настройки для приложения и профиля; подход к обновлению конфигурации зависит от версии и способа подключения.
- Секреты нельзя хранить в открытом виде в обычном Git-репозитории. Для секретов нужны подходящие защищённые механизмы и контроль доступа.

Преимущество — настройки можно централизовать и версионировать отдельно от кода сервисов. Риски — зависимость от доступности Config Server на этапе загрузки, ошибки конфигурации и неправильное обращение с секретами. Нужно продумывать отказоустойчивость и политику обновления.

### Как эти компоненты могут взаимодействовать?

~~~text
Client
  |
  v
Spring Cloud Gateway
  |
  +--> service discovery / load balancing
  |
  v
Order Service ---- OpenFeign ----> Catalog Service
  |                                  |
  +-------- trace context ------------+
  |
  +--> Circuit Breaker вокруг удалённых вызовов

Config Server --> конфигурация сервисов
Eureka         --> реестр сервисов (если используется)
Micrometer Tracing / OpenTelemetry --> трассировка запросов
~~~

Это возможная архитектура, а не обязательная схема. Например, Kubernetes может предоставить discovery и балансировку, поэтому Eureka может быть избыточной. А централизованная конфигурация может решаться платформой или другим конфигурационным сервисом.

### Что важно помнить о Spring Cloud?

1. Согласовывать release train Spring Cloud с версией Spring Boot.
2. Сетевые вызовы всегда могут задерживаться или завершаться ошибкой.
3. Настраивать timeout и отказоустойчивость, а не надеяться только на retry.
4. Следить за распространением trace context и корреляцией логов.
5. Не путать service discovery, gateway и load balancing: это разные обязанности.
6. Не использовать устаревший Sleuth в новых Boot 3+ проектах без понимания ограничений; изучить Micrometer Tracing.
7. Защищать Config Server и не публиковать секреты через конфигурационные endpoints.

**Короткий ответ на собеседовании:** Spring Cloud предоставляет интеграции для распределённых систем. Gateway маршрутизирует запросы, Eureka помогает обнаруживать сервисы, OpenFeign упрощает HTTP-клиенты, Circuit Breaker ограничивает влияние отказов, Config централизует конфигурацию, а для современных приложений tracing обычно строят на Micrometer Tracing/OpenTelemetry вместо Sleuth.

---

## Итог: как связать темы на собеседовании

- **MVC / Front Controller** объясняет путь HTTP-запроса внутри Spring MVC.
- **Filter / Interceptor / AOP / Listener** отличаются точкой расширения и уровнем, на котором работают.
- **REST** — это не просто JSON: важны семантика HTTP, статусы, контракты, безопасность и идемпотентность.
- **Spring Security** отвечает за аутентификацию и авторизацию.
- **Spring Boot** упрощает настройку приложения и эксплуатационную диагностику.
- **Spring Data** сокращает boilerplate доступа к данным, но не снимает ответственность за транзакции и производительность.
- **Spring Cloud** добавляет инфраструктурные паттерны для распределённых систем, которые следует выбирать осознанно.

