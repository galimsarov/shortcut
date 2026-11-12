# Spring Security — Part 1 (вопросы 1–3)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Первая часть изучения Spring Security: назначение и архитектура, базовые механизмы аутентификации и авторизации, подключение и настройка безопасности в Spring Boot. Form Login, HTTP Basic, управление паролями, method security, собственные фильтры и JWT будут разобраны в следующих частях.

---

## 1. Введение в Spring Security

### Для чего нужен Spring Security?

**Spring Security** — фреймворк экосистемы Spring, который отвечает за безопасность приложения. Он предоставляет инфраструктуру для аутентификации, авторизации и защиты от типичных атак на веб-приложения.

Основные задачи:

- **Аутентификация (authentication)** — установить, кто выполняет запрос.
- **Авторизация (authorization)** — проверить, разрешено ли этому субъекту выполнить конкретное действие.
- **Защита веб-уровня** — обработка запросов через цепочки фильтров, защита от CSRF, настройка security headers и интеграция с механизмами веб-аутентификации.
- **Интеграция с источниками пользователей** — база данных, LDAP, in-memory хранилище, внешние identity providers и другие варианты.
- **Защита методов приложения** — возможность ограничивать вызовы сервисных методов, а не только HTTP endpoint'ов.

Spring Security не заменяет бизнес-проверки. Например, проверка того, что пользователь может редактировать именно свой заказ, может требовать обращения к данным заказа и должна быть выражена на соответствующем уровне приложения.

### Где Spring Security находится в архитектуре Spring-приложения?

В servlet-приложении Spring Security обычно подключается на уровне Servlet API — до того, как запрос попадёт в Spring MVC и **DispatcherServlet**.

Упрощённая схема:

~~~text
HTTP request
    |
    v
Servlet container
    |
    v
DelegatingFilterProxy
    |
    v
FilterChainProxy (Spring Security)
    |
    v
Подходящая SecurityFilterChain
    |
    +--> security filters
    |      - загрузка/сохранение SecurityContext
    |      - аутентификация
    |      - обработка исключений безопасности
    |      - авторизация
    |
    v
DispatcherServlet (Spring MVC)
    |
    v
Controller -> Service -> Repository
    |
    v
HTTP response
~~~

Это типичная servlet-архитектура. В WebFlux используется другой стек — реактивная инфраструктура Spring Security, а не servlet-фильтры.

### Как Spring Security встраивается в Servlet Filter Chain?

Servlet-контейнер умеет вызывать фильтры, реализующие **jakarta.servlet.Filter**. Spring Security использует эту точку расширения, но не регистрирует каждый свой внутренний фильтр как отдельный servlet-фильтр.

Ключевые элементы:

- **DelegatingFilterProxy** — servlet-фильтр-мост между Servlet-контейнером и Spring. Он делегирует обработку Spring-бину с заданным именем; в типичной конфигурации это **springSecurityFilterChain**.
- **FilterChainProxy** — центральный Spring Security-фильтр, который управляет внутренними цепочками безопасности.
- **SecurityFilterChain** — описание набора security-фильтров, применяемых к определённым запросам.
- **Security filters** — отдельные фильтры, выполняющие конкретные задачи: например, обработку CSRF, аутентификацию, обработку исключений и авторизацию.

Важно: **FilterChainProxy** может иметь несколько **SecurityFilterChain**. Они выбираются по порядку: используется первая цепочка, чей matcher подходит к запросу. Поэтому порядок цепочек и корректность matcher'ов имеют значение. Если ни одна цепочка не подходит, запрос не получает защиту от Spring Security через эти цепочки.

Условный пример двух цепочек:

~~~java
@Bean
@Order(1)
SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    return http
            .securityMatcher("/api/**")
            // настройки API
            .build();
}

@Bean
@Order(2)
SecurityFilterChain webChain(HttpSecurity http) throws Exception {
    return http
            // настройки остальных запросов
            .build();
}
~~~

Это иллюстрация выбора цепочки, а не готовая полная конфигурация. В реальном приложении нужно явно продумать, какие запросы покрывает каждая цепочка и что происходит с остальными URL.

### Что такое менеджеры и провайдеры аутентификации?

Spring Security разделяет координацию процесса аутентификации и проверку конкретных учётных данных.

- **AuthenticationManager** — интерфейс, принимающий объект **Authentication** и пытающийся аутентифицировать его. Основная реализация — **ProviderManager**.
- **ProviderManager** — менеджер, который перебирает доступные **AuthenticationProvider** и передаёт им запрос на аутентификацию.
- **AuthenticationProvider** — компонент, умеющий проверять определённый тип аутентификации. Например, один провайдер проверяет логин и пароль, другой — токен или другой тип credentials.
- **Authentication** — объект, представляющий запрос на аутентификацию или результат успешной аутентификации. Подробнее — в следующем вопросе.

Упрощённый поток:

~~~text
Authentication request
        |
        v
AuthenticationManager
        |
        v
ProviderManager
        |
        +--> AuthenticationProvider #1
        |
        +--> AuthenticationProvider #2
        |
        v
Authentication result
~~~

Провайдер может вернуть успешно аутентифицированный объект **Authentication**, сообщить, что не поддерживает данный тип запроса, или завершить проверку исключением. Конкретное поведение зависит от типа провайдера и результата проверки.

### Как Spring Boot интегрируется со Spring Security?

В Spring Boot обычно достаточно добавить стартер:

~~~xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
~~~

Стартер подтягивает необходимые зависимости, а Boot и Spring Security настраивают инфраструктуру автоматически. В частности, для servlet-приложения создаётся цепочка безопасности, если приложение не предоставляет собственную конфигурацию соответствующей инфраструктуры.

Автоконфигурация — это не «магия» и не отдельный механизм авторизации: Boot анализирует classpath, настройки и зарегистрированные бины, после чего применяет подходящие конфигурационные классы. Разработчик может переопределить поведение собственными бинами и настройками.

**Короткий ответ на собеседовании:** Spring Security в servlet-приложении работает преимущественно через фильтры. Servlet-контейнер вызывает **DelegatingFilterProxy**, тот передаёт запрос в **FilterChainProxy**, а он выбирает подходящую **SecurityFilterChain**. Внутри цепочки находятся фильтры аутентификации, обработки контекста, исключений и авторизации. **AuthenticationManager** координирует проверку, а конкретную проверку выполняет **AuthenticationProvider**.

---

## 2. Ядро безопасности: аутентификация и авторизация

### Чем аутентификация отличается от авторизации?

**Аутентификация** отвечает на вопрос: «Кто ты?». Приложение проверяет предъявленные credentials — например, пароль, токен или сертификат — и устанавливает личность субъекта.

**Авторизация** отвечает на вопрос: «Что тебе разрешено?». После установления личности приложение проверяет, хватает ли у субъекта полномочий для запрошенного действия.

Пример:

1. Пользователь отправляет логин и пароль.
2. Spring Security проверяет их и устанавливает, что пользователь — alex.
3. Приложение получает его authorities, например ROLE_USER.
4. При обращении к административному endpoint проверяется наличие ROLE_ADMIN.
5. Если полномочий нет, доступ запрещается.

Успешная аутентификация не означает, что пользователю разрешено всё.

### Что такое Authentication?

**Authentication** — центральный интерфейс Spring Security, который представляет информацию об аутентификации.

У него есть несколько важных методов:

- **getPrincipal()** — субъект аутентификации. Часто это объект пользователя (**UserDetails**), но тип зависит от механизма аутентификации.
- **getCredentials()** — credentials, использованные для аутентификации. Например, пароль до или во время проверки. После успешной аутентификации чувствительные credentials обычно очищаются, если объект это поддерживает.
- **getAuthorities()** — выданные субъекту полномочия (**GrantedAuthority**).
- **getDetails()** — дополнительные сведения о запросе, например детали web-аутентификации.
- **isAuthenticated()** — признак того, что объект представляет успешно аутентифицированный результат. Не следует считать произвольный объект с **isAuthenticated() == true** доверенным: такие объекты должны создаваться и устанавливаться доверенной инфраструктурой.

Один и тот же интерфейс используется на разных этапах. В начале это может быть запрос с введёнными credentials и **isAuthenticated() == false**; после успешной проверки — результат с principal и authorities.

Пример упрощённого получения текущей аутентификации:

~~~java
Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

Object principal = authentication.getPrincipal();
Collection<? extends GrantedAuthority> authorities =
        authentication.getAuthorities();
~~~

В прикладном коде не стоит без необходимости разбирать principal как конкретный внутренний тип. Лучше определить понятный контракт получения текущего пользователя и адаптировать его к используемому механизму аутентификации.

### Что такое Principal и чем он отличается от Authentication?

**Principal** — более общий Java-интерфейс, представляющий идентичность субъекта. У него есть, в частности, метод **getName()**.

**Authentication** — более специализированное понятие Spring Security: кроме информации о субъекте, он содержит credentials, authorities, details и состояние аутентификации. Сам **Authentication** наследует **Principal**.

Иными словами, **Principal** отвечает прежде всего на вопрос «кто субъект», а **Authentication** описывает контекст аутентификации в Spring Security.

### Как работает AuthenticationProvider?

**AuthenticationProvider** выполняет проверку конкретного типа запроса аутентификации. Основные методы:

- **supports(Class<?> authentication)** — сообщает, поддерживает ли провайдер данный тип **Authentication**.
- **authenticate(Authentication authentication)** — выполняет проверку и возвращает результат аутентификации либо **null**, если провайдер не может обработать этот запрос.

Например, **DaoAuthenticationProvider** предназначен для аутентификации по имени пользователя и паролю. Он использует **UserDetailsService**, чтобы загрузить пользователя, и **PasswordEncoder**, чтобы проверить пароль.

Упрощённая последовательность:

~~~text
UsernamePasswordAuthenticationToken (не аутентифицирован)
        |
        v
AuthenticationManager / ProviderManager
        |
        v
DaoAuthenticationProvider
        |
        +--> UserDetailsService.loadUserByUsername(...)
        |
        +--> PasswordEncoder.matches(rawPassword, encodedPassword)
        |
        v
Успешный Authentication с principal и authorities
~~~

Важное различие: **UserDetailsService** загружает данные пользователя, но сам по себе не является менеджером аутентификации и обычно не проверяет пароль. Проверка credentials выполняется провайдером с использованием **PasswordEncoder**.

### Что такое GrantedAuthority и чем authority отличается от роли?

**GrantedAuthority** — интерфейс, представляющий конкретное полномочие субъекта. Чаще всего это строка, возвращаемая методом **getAuthority()**.

Например:

- **ROLE_ADMIN**
- **ROLE_USER**
- **invoice:read**
- **invoice:approve**

**Роль** — распространённый способ группировать полномочия на более высоком уровне. В Spring Security роль обычно представляется тем же **GrantedAuthority**, но по соглашению имеет префикс **ROLE_**.

Отсюда различие в проверках:

~~~java
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/invoices/**").hasAuthority("invoice:read")
~~~

**hasRole("ADMIN")** — удобная проверка роли: Spring Security по умолчанию добавляет префикс **ROLE_** и фактически проверяет authority **ROLE_ADMIN**. В **hasAuthority("ROLE_ADMIN")** нужно указать полное значение самостоятельно.

Не следует смешивать роль и право на конкретное действие. Например, роль **ROLE_MANAGER** может включать authorities **invoice:read** и **invoice:approve**. Для сложных систем явные authorities часто дают более гибкую модель доступа.

### Где хранится текущий контекст безопасности?

**SecurityContext** содержит текущий объект **Authentication**.

Доступ к нему предоставляет **SecurityContextHolder**:

~~~java
SecurityContext context = SecurityContextHolder.getContext();
Authentication authentication = context.getAuthentication();
~~~

По умолчанию в servlet-приложении Spring Security использует стратегию хранения контекста на основе **ThreadLocal**. Это позволяет коду в рамках одного потока обработки запроса получить текущую аутентификацию без передачи её через каждый метод.

Упрощённая связь:

~~~text
SecurityContextHolder
        |
        v
SecurityContext
        |
        v
Authentication
   |       |          |
principal authorities credentials
~~~

Важные нюансы:

- **SecurityContextHolder** — это holder/точка доступа, а не хранилище пользователей и не база данных.
- Контекст обычно устанавливается security-инфраструктурой после успешной аутентификации.
- В типичном servlet-сценарии Spring Security очищает контекст после завершения обработки запроса, чтобы данные одного пользователя не попали в следующий запрос на том же потоке.
- При асинхронном выполнении и переходе на другие потоки контекст нельзя бездумно считать доступным автоматически. Для таких сценариев существуют специальные механизмы передачи контекста.
- В stateless-сценариях контекст может создаваться заново для каждого запроса на основании предъявленного токена; это не означает, что **SecurityContextHolder** перестаёт использоваться во время обработки запроса.

**Короткий ответ на собеседовании:** аутентификация устанавливает личность пользователя, авторизация проверяет его полномочия. Результат аутентификации представлен объектом **Authentication**: в нём есть principal, authorities и другие сведения. **AuthenticationManager** координирует процесс, **AuthenticationProvider** выполняет конкретную проверку. Полномочия представлены **GrantedAuthority**, а текущий **Authentication** доступен через **SecurityContextHolder** и **SecurityContext**.

---

## 3. Настройка безопасности в приложении

### Что происходит после подключения spring-boot-starter-security?

Подключение стартера включает security-инфраструктуру и автоконфигурацию Spring Boot. Если приложение не задаёт собственную конфигурацию, Boot применяет безопасные настройки по умолчанию.

Для типичного servlet-приложения это означает:

- запросы по умолчанию требуют аутентификации;
- используется стандартная форма входа и поддерживается HTTP Basic;
- для CSRF включена защита в типичной конфигурации;
- применяются стандартные security headers;
- при отсутствии собственного источника пользователей Boot может создать пользователя с именем **user** и сгенерировать пароль для разработки, который выводится в лог при старте.

Конкретные детали поведения зависят от версии Spring Boot/Spring Security и типа приложения. Сгенерированный пароль нельзя использовать как постоянную учётную запись в production. Это удобство для локального запуска, а не полноценное управление пользователями.

### Как изменить настройки по умолчанию?

Основной современный способ конфигурации servlet-приложения — объявить бин **SecurityFilterChain** и настроить его через DSL **HttpSecurity**.

Пример минимальной конфигурации:

~~~java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
~~~

Здесь:

- **@Configuration** объявляет класс конфигурации Spring.
- **@Bean** регистрирует **SecurityFilterChain** в контексте.
- **HttpSecurity** — объект DSL для настройки веб-безопасности.
- **authorizeHttpRequests** задаёт правила доступа к HTTP-запросам.
- **permitAll()** разрешает запрос без аутентификации.
- **hasRole("ADMIN")** требует authority **ROLE_ADMIN**.
- **authenticated()** требует успешной аутентификации.
- **anyRequest()** задаёт правило для всех запросов, которые не совпали с предыдущими matcher'ами.
- **httpBasic()** включает HTTP Basic.

Правила проверяются в объявленном порядке: более конкретные правила следует размещать раньше общих. В примере **/api/admin/** защищён ролью ADMIN, а остальные запросы требуют аутентификации.

**@EnableWebSecurity** явно включает web-security-конфигурацию. В Spring Boot со стартером многие компоненты настраиваются автоматически, поэтому в типичном Boot-приложении часто достаточно объявить собственный **SecurityFilterChain**; аннотация обычно не обязательна для такого сценария.

### Что меняется, когда мы объявляем собственный SecurityFilterChain?

Это важный момент для собеседования. Если приложение предоставляет собственный бин **SecurityFilterChain**, Boot отступает от своей стандартной web-конфигурации и использует пользовательскую цепочку вместо автоматически настроенной цепочки по умолчанию.

Это не означает, что Spring Security перестаёт работать или что все её фильтры нужно создавать вручную. Вы настраиваете правила и механизмы через **HttpSecurity**, а Spring Security формирует нужную цепочку фильтров.

Но ответственность за полноту правил переходит к разработчику. Например, если написать только:

~~~java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()
)
~~~

не стоит считать, что все остальные endpoint'ы автоматически защищены именно так, как задумано. Для безопасной конфигурации обычно задают завершающее правило **.anyRequest().authenticated()** либо явно описывают все необходимые matcher'ы.

Если объявлено несколько **SecurityFilterChain**, нужно учитывать **securityMatcher** и порядок **@Order**: первая подходящая цепочка обрабатывает запрос. Не следует путать **securityMatcher** (выбор всей цепочки) и **requestMatchers** внутри **authorizeHttpRequests** (правила доступа внутри выбранной цепочки).

### Как настраиваются пользователи и пароли?

Для аутентификации по логину и паролю обычно нужны два компонента:

- **UserDetailsService** — загружает пользователя по идентификатору (часто username или email) и возвращает **UserDetails**.
- **PasswordEncoder** — проверяет введённый пароль относительно сохранённого кодированного значения и при необходимости кодирует новый пароль.

**UserDetails** содержит имя пользователя, пароль и authorities, а также признаки состояния учётной записи: активна ли она, не истёк ли срок действия учётной записи и не заблокирована ли она.

Упрощённый пример реализации загрузки пользователя:

~~~java
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getAuthorities())
                .disabled(!user.isEnabled())
                .build();
    }
}
~~~

Пример иллюстрирует идею: приложение загружает свою сущность из БД и преобразует её в security-модель **UserDetails**. В реальном проекте названия методов и модель authorities будут соответствовать доменной модели приложения.

При стандартной username/password-аутентификации **DaoAuthenticationProvider** использует **UserDetailsService** для загрузки пользователя и **PasswordEncoder** для проверки пароля. Не нужно самостоятельно сравнивать пароль в контроллере.

### Зачем нужен PasswordEncoder?

Пароли нельзя хранить в базе в открытом виде. Для хранения используют специализированные password hashing algorithms — алгоритмы хеширования паролей с солью и вычислительной стоимостью.

Интерфейс **PasswordEncoder** предоставляет основные операции:

- **encode(rawPassword)** — получить значение для хранения;
- **matches(rawPassword, encodedPassword)** — проверить введённый пароль относительно сохранённого значения;
- **upgradeEncoding(encodedPassword)** — определить, стоит ли обновить старый формат/параметры кодирования при успешной аутентификации.

Пример с BCrypt:

~~~java
@Configuration
public class PasswordConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
~~~

При регистрации:

~~~java
String passwordHash = passwordEncoder.encode(rawPassword);
userRepository.save(new AppUser(email, passwordHash));
~~~

При входе пароль проверяется через **matches**, а не сравнением строк:

~~~java
passwordEncoder.matches(rawPassword, user.getPasswordHash());
~~~

На практике проверку обычно выполняет **AuthenticationProvider**, а не код контроллера или сервиса регистрации.

**Почему нельзя просто использовать SHA-256?** Быстрые универсальные хеш-функции предназначены для других задач и позволяют очень быстро перебирать пароли. Password hashing algorithms специально делают вычисление дорогим и используют соль. В Spring Security распространены BCrypt, Argon2, PBKDF2 и SCrypt — выбор зависит от требований и доступных ресурсов.

Для приложений, которым нужно поддерживать разные форматы паролей или мигрировать со старого формата, существует **DelegatingPasswordEncoder**: он хранит идентификатор алгоритма вместе с закодированным паролем и выбирает нужный encoder при проверке. Подробное сравнение стратегий и миграцию паролей разберём в отдельной части.

### Как связаны UserDetailsService, PasswordEncoder и AuthenticationProvider?

Это три разных ответственности:

| Компонент | Ответственность |
|---|---|
| **UserDetailsService** | Найти пользователя и загрузить его данные и authorities |
| **PasswordEncoder** | Безопасно кодировать пароль и проверять совпадение |
| **AuthenticationProvider** | Организовать проверку конкретного типа credentials и вернуть результат аутентификации |

Типичный сценарий входа по логину и паролю:

1. Клиент отправляет credentials.
2. Фильтр аутентификации формирует неаутентифицированный **Authentication**.
3. Запрос передаётся в **AuthenticationManager**.
4. **ProviderManager** выбирает подходящий **AuthenticationProvider**.
5. **DaoAuthenticationProvider** загружает пользователя через **UserDetailsService**.
6. Провайдер проверяет пароль через **PasswordEncoder** и проверяет состояние учётной записи.
7. При успехе формируется аутентифицированный **Authentication** с principal и authorities.
8. Security-инфраструктура устанавливает контекст безопасности, после чего правила авторизации могут использовать полученные authorities.

Упрощённая схема:

~~~text
Login request
     |
     v
Authentication filter
     |
     v
AuthenticationManager
     |
     v
DaoAuthenticationProvider
     |
     +--> UserDetailsService --> UserDetails
     |
     +--> PasswordEncoder.matches(...)
     |
     v
Authenticated Authentication
     |
     v
SecurityContext
~~~

### Что важно помнить о CSRF и безопасности конфигурации?

При настройке безопасности нельзя механически отключать защитные механизмы «потому что API stateless».

**CSRF (Cross-Site Request Forgery)** — атака, при которой браузер пользователя отправляет запрос к сайту, где пользователь уже аутентифицирован, например автоматически прикладывая session cookie. Злоумышленник пытается заставить браузер выполнить нежелательное действие от имени пользователя.

Защита CSRF особенно важна, когда браузер автоматически прикладывает credentials, например session cookie. Для API, где credentials передаются явно в заголовке **Authorization** и не прикладываются браузером автоматически, модель риска может быть другой. Решение об отключении CSRF нужно принимать исходя из реального способа аутентификации и клиентской архитектуры, а не только из наличия REST endpoint'ов или **SessionCreationPolicy.STATELESS**.

Также важно:

- использовать HTTPS, чтобы credentials и токены не передавались открытым текстом;
- не хранить пароли в открытом виде;
- не помещать секреты и чувствительные данные в логи;
- явно закрывать endpoint'ы по принципу минимально необходимых привилегий;
- не считать CORS заменой аутентификации, авторизации или CSRF-защиты.

Подробно form login, HTTP Basic, logout, CORS и CSRF будут рассмотрены в следующих частях.

**Короткий ответ на собеседовании:** после подключения **spring-boot-starter-security** Spring Boot включает базовую защиту приложения. Для своей конфигурации объявляют бин **SecurityFilterChain** и задают правила через **HttpSecurity**. Для username/password-аутентификации **UserDetailsService** загружает пользователя, **PasswordEncoder** проверяет пароль, а **AuthenticationProvider** координирует проверку. При создании собственной цепочки важно явно определить правила доступа и не отключать защитные механизмы без анализа модели угроз.

---

## Что разобрать в следующих частях

В этой части рассмотрены только первые три вопроса плана. Остальные темы намеренно оставлены для следующих файлов:

- Form Login и обработчики успешного/неуспешного входа, logout.
- HTTP Basic и особенности передачи credentials.
- Стратегии хранения паролей, BCrypt и **DelegatingPasswordEncoder**.
- Method Security: **@PreAuthorize**, **@Secured**, **@RolesAllowed**, SpEL.
- Кастомная аутентификация, **UserDetailsService** и **AuthenticationManager**.
- Собственные security-фильтры и порядок фильтров.
- Сессии, JWT и stateless-аутентификация.
