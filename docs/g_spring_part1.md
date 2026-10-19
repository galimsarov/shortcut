# Spring Framework — Part 1 (вопросы 1–7)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Разбираем основы Spring, управление бинами, конфигурацию, жизненный цикл и скоупы бинов, транзакции и обработку исключений в веб-слое.

---

## 1. Spring Framework. Основы

### Что такое Spring и какую проблему он решает?

**Spring Framework** — фреймворк для разработки Java-приложений, предоставляющий инфраструктуру для управления объектами, связывания компонентов, транзакций, веб-разработки и доступа к данным.

Главная идея — отделить бизнес-логику от инфраструктурного кода. Классы не должны самостоятельно создавать всю цепочку зависимостей: контейнер Spring создаёт объекты и связывает их.

Без DI сервис жёстко привязан к реализации:

~~~java
public class UserService {
    private final UserRepository repository = new JdbcUserRepository();
}
~~~

Со Spring зависимость передаётся извне:

~~~java
@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
~~~

Теперь сервис зависит от абстракции, а подходящую реализацию выбирает контейнер. Это упрощает замену реализаций и unit-тестирование.

Основные части экосистемы — это не один неделимый модуль. В терминах Maven/Gradle зависимости разделены на артефакты и проекты, хотя некоторые из них относятся к одному большому проекту Spring.

- **Core Container** — базовые модули самого Spring Framework: в частности, `spring-core`, `spring-beans`, `spring-context` и `spring-expression`. Они обеспечивают IoC/DI, конфигурацию и работу контейнера.
- **AOP** — модуль Spring Framework `spring-aop`. Для аспектов также может понадобиться библиотека AspectJ — в зависимости от используемого механизма.
- **Transactions** — модуль Spring Framework `spring-tx`, который содержит абстракции транзакционного управления. Для работы с конкретной технологией нужны соответствующие интеграции, например JDBC/JPA и transaction manager.
- **Web MVC** — модули Spring Framework, прежде всего `spring-web` и `spring-webmvc`.
- **Spring Security** — отдельный проект экосистемы со своими модулями, например `spring-security-core`, `spring-security-config` и `spring-security-web`.
- **Spring Data** — тоже отдельный проект экосистемы: например, `spring-data-jpa` для интеграции с JPA и `spring-data-mongodb` для MongoDB.
- **Spring Boot** — отдельный проект, предоставляющий автоконфигурацию и стартеры, которые помогают согласованно подключать нужные зависимости.

То есть Core, AOP и Transaction — отдельные артефакты внутри Spring Framework; Web MVC — другие артефакты того же Framework; Security, Data и Boot — отдельные проекты со своими артефактами. Они не обязательно подключаются все сразу.

Например, в обычном Spring Boot Web-приложении зависимости часто добавляют через стартеры:

~~~groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
~~~

В таком случае стартеры подтягивают транзитивные зависимости. Для транзакций и AOP в приложении могут понадобиться соответствующие модули, но не нужно вручную добавлять каждый артефакт, если его уже приносит выбранный стартер. Точный набор зависит от версии Spring Boot и задачи приложения.

Важно различать **модуль Framework**, **отдельный проект экосистемы** и **Spring Boot starter**: стартер — удобная точка подключения зависимостей, а не сам механизм IoC или MVC.

### Основные компоненты контейнера

#### Bean

**Bean** — объект, жизненным циклом которого управляет Spring IoC-контейнер. Это может быть сервис, репозиторий, контроллер, клиент внешнего API или другой зарегистрированный компонент.

Бин обычно создаётся через component scanning и стереотипные аннотации, метод с <code>@Bean</code> или программную регистрацию. Объект, созданный через обычный <code>new</code> внутри произвольного метода, сам по себе контейнеру Spring неизвестен.

#### BeanFactory

**BeanFactory** — базовый интерфейс IoC-контейнера. Он предоставляет механизм получения бинов, разрешения зависимостей и управления ими. Создание бина может быть отложено до первого обращения, если это допускает конфигурация.

#### ApplicationContext

**ApplicationContext** расширяет возможности <code>BeanFactory</code> и является стандартным контейнером в большинстве приложений Spring. Помимо управления бинами, он поддерживает публикацию событий, интернационализацию через <code>MessageSource</code>, работу с ресурсами и интеграцию с инфраструктурой Spring.

В типичном приложении разработчик работает с <code>ApplicationContext</code>, а не напрямую с <code>BeanFactory</code>. Утверждение «BeanFactory всегда ленивый, а ApplicationContext всегда eager» слишком упрощённое: поведение зависит от конфигурации и типа бина. Обычные singleton-бины стандартного ApplicationContext обычно создаются при инициализации контекста.

#### BeanPostProcessor

**<code>BeanPostProcessor</code>** — расширение контейнера, позволяющее обработать экземпляр бина до и/или после initialization callbacks.

- <code>postProcessBeforeInitialization(bean, beanName)</code> вызывается перед обычными init-callbacks.
- <code>postProcessAfterInitialization(bean, beanName)</code> вызывается после них.

Процессор может вернуть исходный объект или обёртку. Некоторые постпроцессоры действительно участвуют в создании AOP-прокси. Прокси — объект-посредник, через который проходит вызов метода; он позволяет выполнить дополнительную логику до и/или после вызова целевого объекта, не добавляя эту логику в сам бизнес-класс.

Например, транзакционная инфраструктура Spring обычно использует прокси. Когда другой бин вызывает метод с `@Transactional`, вызов проходит через прокси: тот начинает или присоединяет транзакцию, вызывает целевой метод, а затем фиксирует транзакцию либо откатывает её по правилам транзакционного менеджера.

~~~java
@Service
public class OrderService {
    @Transactional
    public void createOrder() {
        // бизнес-логика и изменения в БД
    }
}
~~~

Упрощённо цепочка выглядит так:

~~~text
Другой бин
   |
   v
OrderService proxy
   |  открыть/присоединить транзакцию
   v
Реальный OrderService.createOrder()
   |  успех -> commit
   |  подходящее исключение -> rollback
   v
Возврат вызывающему коду
~~~

Класс `OrderService` не обязан сам открывать транзакцию и вызывать `commit()`: это делает инфраструктура вокруг метода. Аналогично прокси могут реализовывать другие сквозные задачи — например, проверки доступа или кэширование.

Важный нюанс: в стандартном proxy-based AOP вызов должен пройти через прокси. Если метод того же объекта вызывает другой его метод через `this`, такой внутренний вызов обычно обходит прокси, поэтому советующая логика (например, `@Transactional`) на втором методе может не сработать. Не каждый BeanPostProcessor создаёт прокси.

Не путайте:
- **BeanFactoryPostProcessor** работает с метаданными определений бинов (<code>BeanDefinition</code>) до обычного создания экземпляров.
- **BeanPostProcessor** работает с экземплярами бинов в процессе их создания.

### IoC и DI — в чём разница?

**IoC (Inversion of Control, инверсия управления)** — общий принцип: управление созданием объектов и их связями передаётся контейнеру или инфраструктуре, а не остаётся целиком в прикладном коде.

**DI (Dependency Injection, внедрение зависимостей)** — один из способов реализовать IoC: объект получает зависимости извне, вместо того чтобы самостоятельно создавать их.

То есть IoC — более широкая идея, DI — один из конкретных механизмов реализации IoC.

**Dependency Inversion Principle (DIP, принцип инверсии зависимостей)** — это уже принцип проектирования из SOLID, а не механизм контейнера. Он говорит, что высокоуровневые модули не должны зависеть от низкоуровневых деталей напрямую: оба должны зависеть от абстракций. Абстракции не должны зависеть от деталей — детали должны зависеть от абстракций.

Например, бизнес-сервис должен зависеть от интерфейса `PaymentGateway`, а не создавать внутри себя конкретный `StripePaymentGateway`. Конкретная реализация тоже реализует этот интерфейс. Spring DI может внедрить нужную реализацию в сервис, но сам DIP можно применять и без Spring.

~~~java
public interface PaymentGateway {
    void pay(BigDecimal amount);
}

@Service
public class CheckoutService {
    private final PaymentGateway paymentGateway;

    public CheckoutService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}

@Component
public class BankPaymentGateway implements PaymentGateway {
    @Override
    public void pay(BigDecimal amount) {
        // интеграция с банком
    }
}
~~~

Как соотносятся три термина:
- **DIP** — принцип проектирования: зависимости в коде направляются к абстракциям.
- **IoC** — общий принцип передачи управления созданием/связыванием объектов внешнему механизму.
- **DI** — способ реализации IoC, при котором зависимости передаются объекту извне.

Они связаны, но не являются синонимами. DI с помощью Spring часто помогает соблюдать DIP, но само по себе наличие контейнера не гарантирует хорошую архитектуру.

Spring поддерживает внедрение через конструктор, setter и поле. Для обязательных зависимостей обычно предпочтительно **конструкторное внедрение**: оно позволяет использовать <code>final</code>, гарантирует получение зависимостей при создании объекта и облегчает unit-тестирование. Если конструктор один, <code>@Autowired</code> на нём обычно не нужен.

~~~java
@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }
}
~~~

Setter injection уместен для действительно необязательных зависимостей. Field injection короче, но скрывает обязательные зависимости и усложняет тестирование объекта без Spring, поэтому обычно его избегают.

**Ответ на собеседовании:** Spring управляет бинами, связывает зависимости через DI и предоставляет инфраструктуру вроде AOP, транзакций и MVC. Это уменьшает связанность и отделяет бизнес-логику от инфраструктуры.

---

## 2. Создание и внедрение бинов. Базовые аннотации

### Как Spring находит и регистрирует бины?

Есть два распространённых подхода.

**1. Component scanning.** Spring сканирует заданные пакеты и регистрирует классы со стереотипными аннотациями.

~~~java
@Component
public class UuidGenerator {
    public UUID generate() {
        return UUID.randomUUID();
    }
}

@Service
public class OrderService {
    private final UuidGenerator generator;

    public OrderService(UuidGenerator generator) {
        this.generator = generator;
    }
}
~~~

**2. Java-конфигурация через <code>@Configuration</code> и <code>@Bean</code>.** Подходит для сторонних классов и объектов, создание которых требует явной настройки.

~~~java
@Configuration
public class ClientConfiguration {
    @Bean
    public RestClient externalRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.example.com")
                .build();
    }
}
~~~

Метод <code>@Bean</code> объявляет объект, которым управляет контейнер. По умолчанию именем бина становится имя метода — здесь <code>externalRestClient</code>.

### Основные аннотации

#### <code>@ComponentScan</code>

Указывает пакеты для поиска компонентов. В Spring Boot <code>@SpringBootApplication</code> включает <code>@ComponentScan</code>. По умолчанию сканирование начинается с пакета главного класса и охватывает его подпакеты, поэтому главный класс обычно размещают в корневом пакете приложения.

#### <code>@Component</code>

Универсальная аннотация для класса, который должен регистрироваться как бин. Подходит для компонентов, не относящихся к более специализированной роли.

#### <code>@Service</code>

Стереотип для сервисного слоя, обычно содержащего бизнес-логику. С точки зрения обнаружения бинов это специализация <code>@Component</code>. Сама по себе аннотация не включает транзакции и не добавляет бизнес-поведение автоматически.

#### <code>@Repository</code>

Стереотип слоя доступа к данным. Помимо обозначения роли компонента, участвует в механизме перевода некоторых исключений технологий хранения данных в иерархию Spring <code>DataAccessException</code>, если соответствующая инфраструктура настроена.

#### <code>@Controller</code> и <code>@RestController</code>

<code>@Controller</code> обозначает компонент Spring MVC, который обрабатывает веб-запросы; обычно его методы возвращают имя представления, например HTML-шаблона.

<code>@RestController</code> — сочетание <code>@Controller</code> и <code>@ResponseBody</code>. Возвращаемое значение метода записывается в тело HTTP-ответа, например сериализуется в JSON настроенным <code>HttpMessageConverter</code>.

~~~java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public UserDto findById(@PathVariable long id) {
        return new UserDto(id, "Alex");
    }
}
~~~

В реальном приложении контроллер обычно возвращает DTO, а не JPA-сущность напрямую.

#### <code>@Bean</code>

Размещается на методе конфигурационного класса и объявляет возвращаемый объект бином Spring. Особенно полезна для типов из внешних библиотек, которые нельзя или не нужно размечать аннотациями.

~~~java
@Configuration
public class TimeConfiguration {
    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}
~~~

#### <code>@Autowired</code>

Просит Spring разрешить зависимость и внедрить подходящий бин. Можно применять к конструктору, setter или полю, но для обязательных зависимостей обычно выбирают конструктор.

Если подходящий бин один, Spring обычно выбирает его по типу. Если кандидатов несколько, нужно устранить неоднозначность, например с помощью <code>@Qualifier</code> или <code>@Primary</code>.

#### <code>@Primary</code>

Отмечает предпочтительный бин среди нескольких кандидатов одного типа.

~~~java
public interface MessageSender {
    void send(String message);
}

@Component
@Primary
public class EmailMessageSender implements MessageSender {
    @Override
    public void send(String message) {
        // отправка email
    }
}

@Component
public class SmsMessageSender implements MessageSender {
    @Override
    public void send(String message) {
        // отправка SMS
    }
}
~~~

Если зависимость типа <code>MessageSender</code> внедряется без уточнений, будет выбран <code>EmailMessageSender</code>. Это не запрещает явно выбрать другой бин.

#### <code>@Qualifier</code>

Позволяет указать, какой именно бин требуется в конкретной точке внедрения.

~~~java
@Service
public class AlertService {
    private final MessageSender sender;

    public AlertService(
            @Qualifier("smsMessageSender") MessageSender sender) {
        this.sender = sender;
    }
}
~~~

По умолчанию имя бина обычно образуется из имени класса с маленькой первой буквой. Имя можно задать явно, например <code>@Component("smsSender")</code>.

**<code>@Primary</code> и <code>@Qualifier</code> решают смежные, но разные задачи:** первый задаёт предпочтение по умолчанию, второй уточняет выбор в конкретной точке внедрения.

### Когда использовать scanning, а когда <code>@Bean</code>?

- Стереотипы — для собственных компонентов приложения.
- <code>@Bean</code> — для явного создания и настройки объектов, в особенности типов из внешних библиотек.
- Не следует без причины регистрировать один объект одновременно сканированием и методом <code>@Bean</code>: это может привести к нескольким кандидатам и неоднозначности.

---

## 3. Условия создания бинов: <code>@Conditional</code> и <code>@Profile</code>

Компонент иногда нужен только в определённой среде или при заданной конфигурации. Например, локально приложение использует заглушку внешнего сервиса, а в production — реальный клиент.

### <code>@Profile</code>

<code>@Profile</code> регистрирует компонент или конфигурацию только при активном профиле.

~~~java
public interface PaymentGateway {
    void pay(BigDecimal amount);
}

@Component
@Profile("dev")
public class FakePaymentGateway implements PaymentGateway {
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("Fake payment: " + amount);
    }
}

@Component
@Profile("prod")
public class RemotePaymentGateway implements PaymentGateway {
    @Override
    public void pay(BigDecimal amount) {
        // вызов платёжного провайдера
    }
}
~~~

Профиль можно задать конфигурацией или, например, переменной окружения <code>SPRING_PROFILES_ACTIVE</code>. В Spring Boot активный профиль также часто указывают в настройках запуска.

Если активные профили не заданы, Spring Boot обычно использует профиль <code>default</code> для компонентов, явно помеченных <code>@Profile("default")</code>. Это не означает, что компоненты с произвольными именованными профилями становятся активными.

Профили удобны для различий между окружениями: реализаций интеграций, настроек и тестовой конфигурации. Но они не являются системой управления секретами — пароли и ключи нужно хранить и передавать через защищённые механизмы.

### <code>@Conditional</code>

<code>@Conditional</code> позволяет зарегистрировать конфигурацию или бин, только если выполнено условие. Условие задаётся классом, реализующим интерфейс <code>Condition</code>.

~~~java
public class LinuxCondition implements Condition {
    @Override
    public boolean matches(
            ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        return context.getEnvironment()
                .getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("linux");
    }
}

@Configuration
public class PlatformConfiguration {
    @Bean
    @Conditional(LinuxCondition.class)
    public PlatformTool platformTool() {
        return new PlatformTool();
    }
}
~~~

Пример демонстрационный: в приложениях обычно лучше опираться на явные свойства конфигурации, а не на название ОС.

Spring Boot предоставляет готовые условия:
- <code>@ConditionalOnProperty</code> — зависит от значения свойства;
- <code>@ConditionalOnClass</code> и <code>@ConditionalOnMissingClass</code> — от наличия класса в classpath;
- <code>@ConditionalOnBean</code> и <code>@ConditionalOnMissingBean</code> — от наличия или отсутствия бина;
- <code>@ConditionalOnWebApplication</code> — от типа приложения.

~~~java
@Configuration
public class NotificationConfiguration {
    @Bean
    @ConditionalOnProperty(
            name = "notifications.enabled",
            havingValue = "true")
    public NotificationService notificationService() {
        return new NotificationService();
    }
}
~~~

В примере бин создаётся только при значении <code>notifications.enabled=true</code>. Эти аннотации Spring Boot находятся в пакете <code>org.springframework.boot.autoconfigure.condition</code>.

### Чем отличаются <code>@Profile</code> и <code>@Conditional</code>?

- <code>@Profile</code> — специализированный удобный механизм выбора компонентов по активному профилю.
- <code>@Conditional</code> — общий механизм для произвольных условий.
- Профили подходят прежде всего для окружений; условные аннотации Boot — для свойств, classpath, других бинов и условий автоконфигурации.

---

## 4. Жизненный цикл бина

Жизненный цикл описывает путь бина от регистрации определения до создания, настройки, использования и уничтожения.

Для обычного singleton-бина, создаваемого при запуске контекста, упрощённая последовательность такова:

1. Spring регистрирует <code>BeanDefinition</code> — описание создания бина.
2. Контейнер создаёт экземпляр объекта.
3. Внедряются зависимости и выполняется настройка свойств.
4. Вызываются awareness-callbacks, если бин реализует соответствующие интерфейсы, например <code>BeanNameAware</code>.
5. Вызывается <code>BeanPostProcessor.postProcessBeforeInitialization(...)</code>.
6. Выполняются initialization callbacks: метод с <code>@PostConstruct</code>, затем <code>InitializingBean.afterPropertiesSet()</code>, затем пользовательский init-метод, если он задан.
7. Вызывается <code>BeanPostProcessor.postProcessAfterInitialization(...)</code>.
8. Бин готов к использованию; при необходимости контейнер может предоставить прокси вместо исходного объекта.
9. При штатном закрытии контекста выполняются callbacks уничтожения: <code>@PreDestroy</code>, <code>DisposableBean.destroy()</code> и пользовательский destroy-метод.

Это схема для понимания основных этапов, а не универсальный порядок абсолютно для любого расширения контейнера. Например, <code>BeanFactoryPostProcessor</code> обрабатывает определения бинов на более раннем этапе.

### Пример init- и destroy-callbacks

~~~java
@Component
public class CacheWarmup {
    @PostConstruct
    public void init() {
        // Проверить конфигурацию, подготовить локальный кэш
    }

    @PreDestroy
    public void shutdown() {
        // Освободить собственные ресурсы
    }
}
~~~

В современных приложениях аннотации импортируют из пакета <code>jakarta.annotation</code>. Нельзя рассчитывать на вызов destroy-callback при аварийном завершении процесса или принудительном завершении JVM.

Можно задать методы в конфигурации, не меняя класс сторонней библиотеки:

~~~java
@Configuration
public class ClientConfiguration {
    @Bean(initMethod = "connect", destroyMethod = "disconnect")
    public LegacyClient legacyClient() {
        return new LegacyClient();
    }
}
~~~

### Для чего нужен BeanPostProcessor?

Он позволяет вмешиваться в создание подходящих бинов: анализировать аннотации, добавлять обёртки или реализовывать инфраструктурное поведение. Методы before/after initialization располагаются до и после init-callbacks соответственно. Постпроцессор должен быть зарегистрирован достаточно рано, чтобы обработать нужные бины.

Не стоит использовать <code>BeanPostProcessor</code> как обычный сервис для бизнес-логики: это низкоуровневый механизм расширения контейнера.

### Что означает singleton в Spring?

Singleton — один экземпляр бина **на один контейнер Spring**, а не один экземпляр на всю JVM. Такой бин обычно используется многими потоками, поэтому его изменяемое состояние должно быть спроектировано с учётом многопоточности.

---

## 5. Скоупы бинов

**Scope** определяет, сколько экземпляров бина создаёт контейнер и как долго они существуют в контексте.

| Scope | Поведение |
|---|---|
| <code>singleton</code> | Один экземпляр бина на контейнер. Значение по умолчанию. |
| <code>prototype</code> | Новый экземпляр при каждом запросе бина из контейнера. |
| <code>request</code> | Один экземпляр на HTTP-запрос. |
| <code>session</code> | Один экземпляр на HTTP-сессию пользователя. |
| <code>application</code> | Один экземпляр на <code>ServletContext</code> веб-приложения. |
| <code>websocket</code> | Один экземпляр на WebSocket-сессию. |

Веб-скоупы доступны при наличии соответствующей веб-инфраструктуры. Также можно регистрировать собственные скоупы.

### Как объявить scope?

~~~java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportBuilder {
    // состояние для построения одного отчёта
}
~~~

Для singleton обычно не требуется явно писать <code>@Scope("singleton")</code>, потому что это значение по умолчанию.

### Singleton и prototype: важные нюансы

- Singleton создаётся один раз на контейнер и используется многими потоками. Не храните в нём изменяемое состояние конкретного запроса без продуманной синхронизации.
- Prototype создаётся контейнером при каждом запросе такого бина. После передачи объекта потребителю Spring обычно не управляет его полным жизненным циклом; в частности, контейнер не обязан автоматически вызывать destroy-callback для каждого prototype.
- Если prototype напрямую внедрить в singleton через конструктор, экземпляр будет получен при создании singleton и останется его зависимостью. Он не будет автоматически создаваться заново при каждом вызове метода singleton-сервиса.

Если singleton нужен новый prototype при каждой операции, можно использовать <code>ObjectProvider&lt;T&gt;</code>:

~~~java
@Service
public class ReportService {
    private final ObjectProvider<ReportBuilder> builderProvider;

    public ReportService(ObjectProvider<ReportBuilder> builderProvider) {
        this.builderProvider = builderProvider;
    }

    public void createReport() {
        ReportBuilder builder = builderProvider.getObject();
        // каждый вызов provider получает новый prototype-бин
    }
}
~~~

Также применяют <code>Provider&lt;T&gt;</code>, lookup method injection или scoped proxy — в зависимости от задачи.

### Что если singleton зависит от request-scoped бина?

Жизненные циклы различаются: singleton создаётся при запуске приложения, а request-scoped бин должен существовать в рамках HTTP-запроса. Для корректного разрешения зависимости обычно используют scoped proxy или провайдер.

~~~java
@Component
@RequestScope
public class RequestContext {
    // данные текущего HTTP-запроса
}
~~~

<code>@RequestScope</code> — составная аннотация для request scope, поддерживающая scoped proxy.

### Частые вопросы про сочетание scope

#### 1. Что будет, если в prototype внедрить prototype?

При создании каждого экземпляра внешнего prototype Spring разрешит его зависимость и внедрит новый экземпляр внутреннего prototype. Но этот внутренний экземпляр создаётся **один раз на создание внешнего объекта**, а не заново при каждом вызове метода.

Например, при каждом `provider.getObject()` будет создаваться новый `ReportJob`, и в него будет внедряться новый `ReportBuffer`:

~~~java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportBuffer {
}

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportJob {
    private final ReportBuffer buffer;

    public ReportJob(ReportBuffer buffer) {
        this.buffer = buffer;
    }
}
~~~

Если получить два объекта `ReportJob`, у них будут разные `ReportBuffer`. Но если один и тот же `ReportJob` использовать много раз, его поле `buffer` само по себе не заменится.

#### 2. Что будет, если в singleton внедрить prototype?

Singleton создаётся один раз. Во время его создания Spring разрешит зависимость и внедрит **один экземпляр** prototype. При последующих обращениях к singleton этот экземпляр останется тем же — автоматического обновления зависимости не происходит.

~~~java
@Component
public class ReportFacade {
    private final ReportBuffer buffer;

    public ReportFacade(ReportBuffer buffer) {
        this.buffer = buffer;
    }
}
~~~

Несмотря на prototype-scope у `ReportBuffer`, поле в `ReportFacade` будет ссылаться на один и тот же объект на протяжении жизни этого singleton.

Если нужен новый prototype на каждую операцию, внедряют, например, `ObjectProvider<ReportBuffer>` и вызывают `getObject()` в момент, когда новый объект действительно нужен:

~~~java
@Component
public class ReportFacade {
    private final ObjectProvider<ReportBuffer> bufferProvider;

    public ReportFacade(ObjectProvider<ReportBuffer> bufferProvider) {
        this.bufferProvider = bufferProvider;
    }

    public void buildReport() {
        ReportBuffer buffer = bufferProvider.getObject();
        // новый prototype для этой операции
    }
}
~~~

#### 3. Что будет, если в prototype внедрить singleton?

Spring внедрит ссылку на общий singleton-бин. Если создаются несколько экземпляров внешнего prototype, каждый из них будет ссылаться на **тот же singleton**.

~~~java
@Component
public class SharedClock {
}

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Job {
    private final SharedClock clock;

    public Job(SharedClock clock) {
        this.clock = clock;
    }
}
~~~

Здесь у каждого `Job` будет собственный экземпляр `Job`, но общий объект `SharedClock`. Если singleton содержит изменяемое состояние, доступ к нему из нескольких объектов/потоков нужно проектировать аккуратно.

Краткое правило:
- prototype → prototype: новый вложенный prototype при создании каждого внешнего prototype;
- singleton → prototype: один prototype, полученный при создании singleton, если не использовать provider/proxy;
- prototype → singleton: ссылка на общий singleton.

**На собеседовании:** scope задаёт время жизни и правила получения экземпляра, а не потокобезопасность. Prototype сам по себе не гарантирует корректность работы с данными.

---

## 6. Транзакции в Spring и <code>@Transactional</code>

### Что такое транзакция?

Транзакция — группа операций с данными, которая должна выполняться как единое целое. Классические свойства ACID:

- **Atomicity** — либо все операции фиксируются, либо изменения откатываются.
- **Consistency** — транзакция сохраняет ограничения и инварианты данных.
- **Isolation** — параллельные транзакции не приводят к недопустимым аномалиям; гарантии зависят от уровня изоляции.
- **Durability** — зафиксированные изменения сохраняются согласно гарантиям СУБД.

Spring не заменяет транзакционный механизм базы данных. Он предоставляет абстракцию и управляет границами транзакций через соответствующий transaction manager.

### Что делает <code>@Transactional</code>?

Аннотация задаёт правила транзакционного выполнения метода или класса. В стандартном сценарии Spring применяет транзакции через AOP-прокси: вызов перехватывается, открывается новая транзакция или используется существующая, затем выполняется commit либо rollback.

~~~java
@Service
public class TransferService {
    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(long fromId, long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.withdraw(amount);
        to.deposit(amount);

        accountRepository.save(from);
        accountRepository.save(to);
    }
}
~~~

Если операция выбросит исключение, подходящее под правила rollback, изменения в транзакции откатятся. Предполагается, что операции участвуют в одном транзакционном контексте и используют ресурсы соответствующего transaction manager.

### Основные параметры

- <code>propagation</code> — поведение, если транзакция уже существует.
- <code>isolation</code> — уровень изоляции.
- <code>readOnly</code> — подсказка инфраструктуре, что транзакция предназначена для чтения; это не универсальная гарантия запрета записи.
- <code>timeout</code> — допустимое время транзакции, если поддерживается менеджером.
- <code>rollbackFor</code> и <code>noRollbackFor</code> — настройка правил отката по типам исключений.

#### Propagation

- **`REQUIRED`** — значение по умолчанию. Использовать существующую транзакцию или создать новую, если её нет.
- **`REQUIRES_NEW`** — приостановить текущую транзакцию и выполнить метод в новой независимой транзакции, если это поддерживает менеджер.
- **`SUPPORTS`** — присоединиться к транзакции, если она есть; иначе работать без неё.
- **`MANDATORY`** — требовать существующую транзакцию, иначе выбросить исключение.
- **`NOT_SUPPORTED`** — выполнить без транзакции, при необходимости приостановив текущую.
- **`NEVER`** — требовать отсутствия транзакции.
- **`NESTED`** — вложенная транзакционная область, если её поддерживает менеджер; часто реализуется через savepoint и не эквивалентна независимой транзакции.

Не применяйте `REQUIRES_NEW` автоматически «для надёжности»: новая транзакция может зафиксироваться, даже если внешняя позже откатится, а вложенные вызовы могут увеличить потребление соединений.

### Наглядный пример: внешний метод вызывает транзакционный метод

Представим, что внешний метод проходит по списку записей и для каждой вызывает метод другого Spring-бина, который сохраняет запись. В середине цикла возникает unchecked exception (`RuntimeException`). Для наглядности исключение выбрасывается **во внешнем методе после нескольких успешных сохранений**.

~~~java
@Service
public class ItemWriter {
    private final ItemRepository repository;

    public ItemWriter(ItemRepository repository) {
        this.repository = repository;
    }

    @Transactional // propagation = REQUIRED по умолчанию
    public void save(Item item) {
        repository.save(item);
    }
}

@Service
public class ImportService {
    private final ItemWriter itemWriter;

    public ImportService(ItemWriter itemWriter) {
        this.itemWriter = itemWriter;
    }

    @Transactional // тоже REQUIRED по умолчанию
    public void importItems(List<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            itemWriter.save(items.get(i));

            if (i == 1) {
                throw new IllegalStateException("Ошибка импорта");
            }
        }
    }
}
~~~

Предполагается, что оба метода вызваны через Spring-прокси, используют один транзакционный менеджер и один совместимый транзакционный ресурс (например, одну БД). Также предполагается, что `save` не перехватывает исключения и не выполняет независимый commit.

**Вариант A — у обоих методов `REQUIRED` (значение по умолчанию).**

1. `importItems()` открывает транзакцию.
2. Каждый вызов `itemWriter.save()` присоединяется к той же транзакции — отдельного commit внутри `save()` нет.
3. После нескольких сохранений внешний метод выбрасывает `IllegalStateException`.
4. Исключение выходит из внешнего транзакционного метода, и вся транзакция откатывается.

**Результат: ни одна запись этой операции не останется в БД.** Даже если внутренний метод успел выполнить `repository.save()`, его изменения принадлежат общей транзакции.

**Вариант B — у внутреннего метода `REQUIRES_NEW`.**

Изменим только аннотацию:

~~~java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void save(Item item) {
    repository.save(item);
}
~~~

Каждый вызов `save()` приостанавливает внешнюю транзакцию и выполняется в собственной транзакции. Когда `save()` успешно завершается, его транзакция фиксируется, после чего управление возвращается во внешний метод и внешняя транзакция возобновляется.

Если затем `importItems()` выбросит исключение, внешняя транзакция откатится, **но записи, сохранённые успешно в отдельных `REQUIRES_NEW`-транзакциях, останутся в БД**. В примере это первые две записи.

Если же исключение возникнет **внутри самого** `save()` до успешного завершения, текущая `REQUIRES_NEW`-транзакция тоже откатится. Уже зафиксированные предыдущие вызовы при этом останутся.

| Сценарий | Что останется в БД после ошибки во внешнем методе? |
|---|---|
| Внешний `REQUIRED`, внутренний `REQUIRED` | Ничего из общей транзакции: всё откатится. |
| Внешний `REQUIRED`, внутренний `REQUIRES_NEW` | Успешно сохранённые внутренние транзакции останутся; внешняя транзакция откатится. |

**А если внешний метод перехватит исключение?** В примере выше исключение выходит из внешнего метода, поэтому поведение однозначно. Если внешний метод поймает исключение внутри себя и продолжит работу, результат зависит от того, где возникло исключение и была ли транзакция помечена rollback-only. При ошибке, которая вышла из внутреннего `REQUIRED`-метода через его транзакционный прокси, общая транзакция обычно помечается rollback-only; даже если исключение поймать, попытка commit может завершиться `UnexpectedRollbackException`. Не стоит рассчитывать, что простое `try/catch` «отменит» уже установленный флаг отката.

Это работает именно потому, что `itemWriter` — отдельный Spring-бин и вызов проходит через прокси. Если вызвать транзакционный метод через `this.save(...)` внутри того же объекта, прокси не будет пересечён и ожидаемая propagation-семантика может не примениться.

#### Isolation

Часто встречаются уровни <code>READ_UNCOMMITTED</code>, <code>READ_COMMITTED</code>, <code>REPEATABLE_READ</code> и <code>SERIALIZABLE</code>. Точные гарантии зависят от СУБД. Например, PostgreSQL трактует <code>READ UNCOMMITTED</code> как <code>READ COMMITTED</code>, а поведение <code>REPEATABLE READ</code> отличается от некоторых других СУБД.

### Rollback: checked и unchecked exceptions

По умолчанию Spring откатывает транзакцию при <code>RuntimeException</code> и <code>Error</code>, но не при checked exceptions. Для checked exception правило можно изменить:

~~~java
@Transactional(rollbackFor = IOException.class)
public void importData() throws IOException {
    // при IOException транзакция должна откатиться
}
~~~

Если исключение перехвачено внутри метода и наружу не выходит, транзакционный interceptor может не увидеть ошибку и выполнить commit. Если после перехвата нужен rollback, нужно явно выразить это: например, повторно выбросить исключение или пометить транзакцию rollback-only подходящим механизмом.

### Ограничения proxy-based транзакций

1. **Self-invocation.** Вызов одного метода того же объекта через <code>this</code> не проходит через внешний Spring-прокси, поэтому аннотация на вызываемом методе обычно не активирует транзакционное поведение.

   ~~~java
   @Service
   public class ImportService {
       public void runImport() {
           saveBatch(); // внутренний вызов: proxy не пересекается
       }

       @Transactional
       public void saveBatch() {
           // ...
       }
   }
   ~~~

   Обычно транзакционную границу выносят в отдельный Spring-бин и вызывают его через внедрённую зависимость.

2. **Доступность метода.** Для предсказуемого поведения в proxy mode обычно размещают <code>@Transactional</code> на публичных методах сервисного слоя и вызывают их извне через Spring-бин. Точные ограничения зависят от вида прокси и конфигурации.

3. **Объект должен управляться Spring.** Если создать сервис вручную через <code>new</code>, транзакционный прокси к нему автоматически не применится.

4. **Транзакция не делает внешние системы атомарными.** HTTP-вызов, email или сообщение брокеру не откатываются автоматически вместе с транзакцией БД. Для согласования БД и сообщений часто применяют transactional outbox, а для распределённых бизнес-процессов — saga-подход.

5. **Длительные транзакции нежелательны.** Долгие сетевые вызовы внутри транзакции удерживают ресурсы и могут ухудшать конкуренцию за блокировки.

### Где размещать <code>@Transactional</code>?

Чаще всего на сервисном слое, вокруг бизнес-операции, которая должна быть атомарной. Репозитории выполняют доступ к данным, но сервис обычно определяет границы бизнес-транзакции.

**На собеседовании:** <code>@Transactional</code> — декларативная настройка транзакций, обычно реализованная через AOP-прокси. Нужно знать propagation, isolation, rollback rules и ограничения self-invocation.

---

## 7. Обработка исключений в контроллерах Spring MVC

### Как Spring MVC обрабатывает исключения?

Если контроллер или вызываемый им код выбрасывает исключение, Spring MVC передаёт обработку цепочке механизмов разрешения исключений (<code>HandlerExceptionResolver</code>). В частности, доступны <code>@ExceptionHandler</code>, <code>@ControllerAdvice</code> и <code>@RestControllerAdvice</code>, а также стандартные механизмы фреймворка.

В REST API обработчик должен вернуть клиенту понятный HTTP-статус и стабильное описание ошибки, не раскрывая внутренние детали приложения.

### <code>@ExceptionHandler</code>

Метод с <code>@ExceptionHandler</code> обрабатывает указанный тип исключения. Если он объявлен внутри контроллера, обработка локальна для этого контроллера.

~~~java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        return userService.getById(id); // может выбросить исключение
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            UserNotFoundException exception) {
        ApiError error = new ApiError(
                "USER_NOT_FOUND", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
~~~

### <code>@ControllerAdvice</code> и <code>@RestControllerAdvice</code>

Если одинаковые правила обработки нужны нескольким контроллерам, их выносят в отдельный компонент.

- <code>@ControllerAdvice</code> — общий advice для контроллеров; используется с <code>@ExceptionHandler</code>, <code>@InitBinder</code> и <code>@ModelAttribute</code>.
- <code>@RestControllerAdvice</code> — сочетание <code>@ControllerAdvice</code> и <code>@ResponseBody</code>, поэтому возвращаемые объекты записываются в тело ответа.

~~~java
public record ApiError(String code, String message) {
}
~~~

~~~java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("USER_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        // Полную информацию об исключении записываем в лог.
        // Клиенту не раскрываем детали реализации.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "INTERNAL_ERROR", "Внутренняя ошибка сервера"));
    }
}
~~~

Обработчик общего <code>Exception</code> — последний уровень защиты. Не следует бездумно возвращать клиенту <code>exception.getMessage()</code> для любой ошибки: сообщение может раскрыть SQL, внутренние пути, имена классов или чувствительные детали. Неожиданные ошибки нужно логировать с контекстом и trace/correlation ID, не записывая секреты.

### Какие HTTP-статусы выбирать?

- **400 Bad Request** — запрос некорректен, например не проходит валидацию.
- **401 Unauthorized** — отсутствует или не пройдена аутентификация.
- **403 Forbidden** — доступ запрещён; обычно пользователь аутентифицирован, но не имеет нужных прав.
- **404 Not Found** — ресурс не найден.
- **409 Conflict** — конфликт с текущим состоянием ресурса, например дубликат уникального бизнес-идентификатора.
- **500 Internal Server Error** — непредвиденная ошибка сервера.

Не стоит возвращать <code>200 OK</code> с телом ошибки, если операция фактически завершилась неуспешно. HTTP-статус — часть контракта API.

### Обработка ошибок валидации

Входные DTO часто валидируют через Jakarta Bean Validation:

~~~java
public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email) {
}
~~~

~~~java
@PostMapping
public ResponseEntity<UserDto> create(
        @Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.create(request));
}
~~~

При нарушении ограничений Spring MVC может выбросить <code>MethodArgumentNotValidException</code>; для других видов валидации и сигнатур возможны другие исключения. Их стоит обрабатывать отдельно и возвращать структурированный список ошибок полей, а не stack trace.

В современных версиях Spring Framework есть унифицированные механизмы обработки ошибок, включая <code>ProblemDetail</code> (RFC 9457). Конкретный формат ответа должен быть единообразным и соответствовать версии Spring и требованиям API.

### <code>ResponseStatusException</code> и <code>@ResponseStatus</code>

- <code>ResponseStatusException</code> позволяет выбросить исключение с заданным HTTP-статусом, например если ресурс не найден.
- <code>@ResponseStatus</code> позволяет связать статус с типом исключения или обработчиком.

Для сложного API централизованный <code>@RestControllerAdvice</code> часто помогает поддерживать единый формат ошибок.

### Практические рекомендации

1. Не смешивать обработку HTTP-ошибок с бизнес-логикой. Сервис может выбрасывать доменное исключение, а веб-слой преобразует его в HTTP-ответ.
2. Использовать осмысленные исключения для ожидаемых бизнес-ситуаций, например <code>UserNotFoundException</code>.
3. Централизовать общие правила в <code>@RestControllerAdvice</code>.
4. Разделять ожидаемые ошибки клиента и неожиданные ошибки сервера.
5. Не отправлять клиенту stack trace, SQL и внутренние детали.
6. Логировать неожиданные ошибки на подходящем уровне, добавляя контекст запроса.
7. Покрывать обработчики тестами: проверять статус, формат тела и отсутствие утечки внутренних сообщений.

**На собеседовании:** <code>@ExceptionHandler</code> обрабатывает исключения, а <code>@RestControllerAdvice</code> позволяет вынести обработку на общий уровень. Веб-слой преобразует исключение в корректный HTTP-ответ и сохраняет единый контракт ошибок.

---

## Краткая шпаргалка

| Тема | Что нужно уметь объяснить |
|---|---|
| IoC / DI | Контейнер управляет объектами; зависимости передаются извне. |
| BeanFactory / ApplicationContext | Базовый контейнер и расширенный стандартный контекст. |
| BeanPostProcessor | Обработка экземпляров до/после init-callbacks; может участвовать в создании прокси. |
| Стереотипы | <code>@Component</code>, <code>@Service</code>, <code>@Repository</code>, <code>@Controller</code> обозначают роли компонентов. |
| <code>@Bean</code> | Явная регистрация объекта из метода конфигурации. |
| <code>@Primary</code> / <code>@Qualifier</code> | Выбор между несколькими кандидатами для внедрения. |
| <code>@Profile</code> / <code>@Conditional</code> | Условная регистрация компонентов. |
| Lifecycle | Создание → DI → callbacks и BeanPostProcessor → использование → уничтожение. |
| Scope | <code>singleton</code> — один на контейнер; <code>prototype</code> — новый при запросе; веб-скоупы привязаны к контексту запроса/сессии. |
| <code>@Transactional</code> | Транзакционные границы, propagation, isolation, rollback и ограничения прокси. |
| Exception handling | <code>@ExceptionHandler</code>, <code>@RestControllerAdvice</code>, HTTP-статусы и безопасный формат ошибки. |

---

## Что могут спросить дополнительно

- Чем <code>BeanFactoryPostProcessor</code> отличается от <code>BeanPostProcessor</code>?
- Почему конструкторное внедрение обычно предпочтительнее field injection?
- Что произойдёт, если есть два бина одного типа и ни <code>@Primary</code>, ни <code>@Qualifier</code>?
- Почему вызов <code>@Transactional</code>-метода из другого метода того же класса может не открыть транзакцию?
- Чем <code>REQUIRES_NEW</code> отличается от <code>NESTED</code>?
- Почему <code>readOnly = true</code> не является универсальным запретом на запись?
- Чем <code>@ControllerAdvice</code> отличается от локального <code>@ExceptionHandler</code>?
- Почему не стоит возвращать stack trace клиенту?

> Во второй части можно разобрать пункты 8–14 исходного плана: MVC и Front Controller, AOP и web-фильтры/interceptors/listeners, REST, Spring Security, Spring Boot, Spring Data и Spring Cloud.
