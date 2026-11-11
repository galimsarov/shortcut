# Spring Security — Part 1 (вопросы 1–7)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Первая часть посвящена архитектуре Spring Security, аутентификации и авторизации, базовой конфигурации, form login, HTTP Basic, хранению паролей и method security.

---

## 1. Введение в Spring Security

### Для чего нужен Spring Security?

**Spring Security** — отдельный проект экосистемы Spring, предоставляющий инфраструктуру для аутентификации, авторизации и защиты приложений от распространённых атак. Он помогает установить личность пользователя, определить его права, защитить HTTP-запросы и методы приложения, управлять сессиями и применять защиту от CSRF.

Spring Security не заменяет HTTPS, безопасное хранение секретов, валидацию входных данных или бизнес-проверки доступа. Это инфраструктурный слой, который необходимо настроить под конкретное приложение.

### Основные понятия архитектуры

- **SecurityFilterChain** — набор фильтров Spring Security, применяемый к запросам, соответствующим заданному matcher'у.
- **Filter** — компонент Servlet API, который может проверить или изменить запрос/ответ и передать управление дальше.
- **Authentication** — объект, представляющий запрос или результат аутентификации: principal, credentials и authorities.
- **AuthenticationManager** — интерфейс, принимающий запрос на аутентификацию и возвращающий результат.
- **AuthenticationProvider** — компонент, который умеет проверять определённый тип Authentication, например логин и пароль.
- **SecurityContext** — контекст безопасности, в котором хранится результат аутентификации.
- **AuthorizationManager** — механизм принятия решения о доступе к ресурсу или методу на основании текущей аутентификации и правил.

Типичная схема для входа по логину и паролю:

~~~text
HTTP request
    |
    v
SecurityFilterChain
    |
    v
AuthenticationFilter
    |
    v
AuthenticationManager
    |
    v
AuthenticationProvider
    |
    +--> UserDetailsService: загрузить пользователя
    |
    +--> PasswordEncoder: проверить пароль
    |
    v
Authentication (authenticated = true)
    |
    v
SecurityContextHolder
~~~

Это упрощённая схема: конкретные фильтры и провайдеры зависят от способа аутентификации. Например, form login и HTTP Basic используют разные фильтры, но могут делегировать проверку одному AuthenticationManager.

### Как работает Security Filter Chain?

На уровне Servlet-контейнера запрос сначала проходит через обычную цепочку Servlet-фильтров. В неё интегрирован **DelegatingFilterProxy**, который передаёт управление Spring-бину **FilterChainProxy**.

FilterChainProxy выбирает подходящую SecurityFilterChain. Если настроено несколько цепочек, обычно используется первая цепочка, чей RequestMatcher совпал с запросом. Поэтому порядок и область действия цепочек имеют значение. Внутри выбранной цепочки последовательно выполняются фильтры Spring Security.

~~~text
Servlet container
    |
    v
DelegatingFilterProxy
    |
    v
FilterChainProxy
    |
    +--> SecurityFilterChain #1 (matcher)
    |
    +--> SecurityFilterChain #2 (matcher)
    |
    v
Filters of the selected chain
    |
    v
DispatcherServlet / application
~~~

Важно различать два механизма:

- **securityMatcher(...)** определяет, для каких запросов применяется конкретная SecurityFilterChain;
- **requestMatchers(...)** внутри authorizeHttpRequests задаёт правила авторизации для запросов уже внутри выбранной цепочки.

Если ни одна настроенная цепочка не подходит, запрос не получает защиту от этих цепочек. При нескольких цепочках нужно продумать покрытие всех маршрутов и порядок с помощью @Order.

### Интеграция со Spring Boot и автоконфигурация

Для Servlet-приложения обычно подключают стартер:

~~~groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
~~~

Стартер подтягивает необходимые модули Spring Security. Spring Boot выполняет автоконфигурацию на основании classpath и пользовательских бинов.

Если добавить стартер и не определить собственную конфигурацию безопасности, Boot предоставляет базовую защиту: запросы требуют аутентификации, а для входа доступна автоматически настроенная форма. Для типичной конфигурации создаётся временный пользователь с паролем, который генерируется при запуске и выводится в лог. Это поведение для разработки, а не готовая production-конфигурация.

Когда приложение объявляет собственный бин SecurityFilterChain, разработчик берёт на себя настройку правил HTTP-безопасности. Автоконфигурация при этом обычно отступает в соответствующей части.

### Типовой стек технологий

В Spring Boot REST-приложении набор может включать:

- **spring-boot-starter-security** — фильтры, аутентификация и авторизация;
- **spring-boot-starter-web** — Servlet/MVC-инфраструктура;
- **spring-boot-starter-data-jpa** — если пользователи и роли хранятся в реляционной БД;
- **PasswordEncoder** — проверка паролей;
- **UserDetailsService** или собственный механизм загрузки пользователя;
- JWT-библиотека или OAuth2 Resource Server — если приложение принимает bearer-токены;
- HTTPS на уровне reverse proxy, ingress или самого приложения.

Не обязательно использовать всё перечисленное. Например, OAuth2 Resource Server и JWT — отдельный сценарий, который выбирают осознанно.

---

## 2. Ядро безопасности: аутентификация и авторизация

### Чем отличаются аутентификация и авторизация?

**Аутентификация (authentication)** отвечает на вопрос: «Кто вы?». Пользователь предъявляет логин и пароль, сертификат или bearer-токен, а приложение проверяет их корректность.

**Авторизация (authorization)** отвечает на вопрос: «Что вам разрешено?». После установления личности система проверяет, может ли пользователь открыть endpoint, вызвать метод или выполнить операцию над конкретным ресурсом.

Успешный вход не означает, что пользователь имеет право удалить чужую учётную запись. Для этого нужна отдельная проверка авторизации.

### Что такое Authentication и Principal?

**Authentication** — центральный интерфейс Spring Security, представляющий запрос или результат аутентификации. Важные методы:

- **getPrincipal()** — идентификатор или объект аутентифицированного субъекта; для username/password это часто UserDetails;
- **getCredentials()** — предъявленные учётные данные, например пароль; после аутентификации они обычно очищаются или становятся недоступными;
- **getAuthorities()** — набор выданных прав;
- **isAuthenticated()** — признак успешной аутентификации.

До проверки Authentication может содержать введённые credentials и иметь isAuthenticated() == false. После успешной проверки возвращается аутентифицированный объект.

**Principal** — субъект, от имени которого выполняется запрос. В Java Servlet API есть java.security.Principal, а в Spring Security Authentication предоставляет principal через getPrincipal(). Principal не обязательно является строкой с логином: это может быть объект с идентификатором и другими данными пользователя.

### Как работают AuthenticationManager и AuthenticationProvider?

AuthenticationManager — интерфейс с методом:

~~~java
Authentication authenticate(Authentication authentication);
~~~

Наиболее распространённая реализация — **ProviderManager**. Она содержит список AuthenticationProvider и ищет провайдер, который поддерживает переданный тип Authentication.

Провайдер проверяет credentials и при успехе возвращает аутентифицированный Authentication. Если он не поддерживает этот тип, обработку может выполнить следующий провайдер. Неверные credentials обычно приводят к исключению аутентификации.

Например, **DaoAuthenticationProvider** предназначен для аутентификации по имени пользователя и паролю. Он использует UserDetailsService, чтобы загрузить пользователя, и PasswordEncoder, чтобы сравнить введённый пароль с сохранённым хешем.

Один AuthenticationManager может обслуживать несколько способов аутентификации, если настроены соответствующие провайдеры.

### Что такое GrantedAuthority и чем роль отличается от права?

**GrantedAuthority** — интерфейс, представляющий выданное пользователю полномочие. Обычно это строковое значение, например:

- ROLE_ADMIN;
- ROLE_USER;
- firmware:read;
- firmware:upload.

**Роль** — распространённый способ группировать полномочия по смыслу. В Spring Security выражения hasRole("ADMIN") и hasAuthority("ROLE_ADMIN") обычно проверяют одно и то же значение ROLE_ADMIN.

У hasRole("ADMIN") есть соглашение о префиксе ROLE_: его не нужно добавлять в аргумент. В hasAuthority(...) передаётся полное имя authority. Не следует передавать ROLE_ADMIN в hasRole, иначе можно получить повторный префикс.

~~~java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/firmware/**").hasAuthority("firmware:read")
    .anyRequest().authenticated()
)
~~~

Роли удобны для грубого разграничения доступа, а отдельные authorities — для более точных разрешений. Оба подхода могут использоваться вместе.

### Где хранится SecurityContextHolder?

**SecurityContextHolder** — статический фасад для доступа к SecurityContext. Сам контекст содержит Authentication текущего выполнения.

~~~java
Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

String username = authentication.getName();
~~~

По умолчанию Spring Security использует стратегию хранения на основе **ThreadLocal**: в рамках обработки запроса контекст доступен текущему потоку. Фильтры устанавливают контекст перед выполнением приложения и очищают его после обработки запроса, чтобы данные одного запроса не попали в другой.

Для асинхронного выполнения нельзя безоговорочно рассчитывать, что обычный ThreadLocal автоматически перенесётся в другой поток. Для таких сценариев существуют специальные механизмы распространения SecurityContext и интеграции с асинхронным исполнением.

В Servlet-приложениях контекст может сохраняться между запросами, например в HTTP-сессии, в зависимости от конфигурации. В stateless API с bearer-токенами его обычно восстанавливают на каждом запросе.

---

## 3. Настройка безопасности в приложении

### Что даёт spring-boot-starter-security?

Подключение стартера добавляет Spring Security и запускает автоконфигурацию. Без собственной настройки приложение получает базовую защиту: запросы требуют аутентификации, а для входа используется стандартная форма. В логе можно увидеть сгенерированный пароль пользователя user.

Это полезное начальное поведение, но для реального приложения необходимо явно определить пользователей, способы аутентификации и правила доступа.

### Как создать SecurityFilterChain?

В современных конфигурациях используют Java-конфигурацию с бином SecurityFilterChain, а не наследование от WebSecurityConfigurerAdapter (этот подход устарел и удалён в новых версиях).

Пример для простого REST API:

~~~java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
~~~

В примере:

- CSRF отключён, поскольку предполагается stateless API, которое не использует автоматически отправляемые браузером credentials (например, session cookie). Это не универсальное правило: если аутентификация основана на cookie или сессии, отключать CSRF без анализа рисков нельзя;
- STATELESS означает, что Spring Security не использует HTTP-сессию для сохранения SecurityContext между запросами;
- публичные маршруты доступны без аутентификации;
- /api/admin/** доступен только пользователям с ролью ADMIN;
- остальные запросы требуют аутентификации;
- включён HTTP Basic. В production его следует использовать только поверх HTTPS.

@EnableWebSecurity включает интеграцию Spring Security с Servlet web-приложением. В Spring Boot она обычно подключается автоконфигурацией, поэтому явная аннотация в типичном Boot-приложении часто не обязательна.

### Как кодируются пароли: PasswordEncoder

**PasswordEncoder** — интерфейс для преобразования пароля в хеш и проверки введённого значения. Основные методы:

~~~java
String encode(CharSequence rawPassword);
boolean matches(CharSequence rawPassword, String encodedPassword);
~~~

Пароли нельзя хранить в открытом виде или шифровать обратимым способом ради последующего восстановления. Для проверки входа достаточно сравнить введённый пароль с сохранённым хешем через matches.

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
~~~

При регистрации:

~~~java
String encodedPassword = passwordEncoder.encode(request.password());
user.setPassword(encodedPassword);
~~~

При входе не нужно повторно вызывать encode и сравнивать строки: используйте passwordEncoder.matches(rawPassword, user.getPassword()). BCrypt использует соль и вычислительно затратную функцию, поэтому одинаковые пароли обычно дают разные хеши.

Если требуется поддерживать несколько форматов хешей, используют DelegatingPasswordEncoder; подробнее это рассмотрено в вопросе 6.

### Для чего нужен UserDetailsService?

**UserDetailsService** — интерфейс Spring Security для загрузки пользователя по username:

~~~java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
~~~

Типичная реализация обращается к репозиторию и возвращает UserDetails, содержащий имя пользователя, хеш пароля, authorities и флаги состояния учётной записи.

~~~java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
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

UserDetailsService сам по себе не проверяет пароль и не выполняет всю аутентификацию. Он только загружает сведения о пользователе. В стандартном сценарии DaoAuthenticationProvider использует его вместе с PasswordEncoder.

---

## 4. Аутентификация на основе форм (Form Login)

### Как работает formLogin()?

Form login — механизм аутентификации, при котором пользователь вводит логин и пароль в HTML-форму. Spring Security предоставляет фильтр UsernamePasswordAuthenticationFilter, который обрабатывает запрос на вход, создаёт объект Authentication и передаёт его в AuthenticationManager.

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/css/**").permitAll()
                    .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .build();
}
~~~

Если не указывать собственную страницу входа, Spring Security может предоставить стандартную сгенерированную страницу. После успешной аутентификации пользователь обычно перенаправляется на сохранённый до входа URL или на настроенную целевую страницу.

### Как подключить свою страницу входа?

Для собственной HTML-страницы используют loginPage(...):

~~~java
.formLogin(form -> form
    .loginPage("/login")
    .loginProcessingUrl("/perform_login")
    .usernameParameter("email")
    .passwordParameter("password")
    .defaultSuccessUrl("/home", true)
    .permitAll()
)
~~~

Приложение должно само предоставить GET-обработчик или страницу /login. Форму нужно отправлять на /perform_login с параметрами email и password. По умолчанию Spring Security ожидает POST-запрос; CSRF-токен должен быть передан, если CSRF-защита включена.

loginProcessingUrl — endpoint, который обрабатывает фильтр Spring Security. Обычно отдельный MVC-контроллер для него писать не нужно.

### Что делают successHandler и failureHandler?

successHandler задаёт поведение после успешной аутентификации, failureHandler — после неудачной.

~~~java
.formLogin(form -> form
    .successHandler((request, response, authentication) -> {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\\"message\\":\\"Authenticated\\"}");
    })
    .failureHandler((request, response, exception) -> {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\\"error\\":\\"Invalid credentials\\"}");
    })
)
~~~

Для классического web-приложения обычно удобны redirect-ы. Для REST API чаще возвращают JSON и HTTP-статусы, но form login с JSON-ответами нужно настраивать осознанно: браузерный сценарий перенаправлений и API-сценарий имеют разные ожидания.

Не следует отдавать клиенту подробности вроде «пользователь не найден» или «пароль неверный»: это может облегчить перебор существующих учётных записей.

### Как работает logout?

По умолчанию Spring Security предоставляет logout endpoint /logout. Для Servlet-приложения с CSRF-защитой logout обычно выполняется POST-запросом с CSRF-токеном.

~~~java
.logout(logout -> logout
    .logoutUrl("/logout")
    .invalidateHttpSession(true)
    .clearAuthentication(true)
    .deleteCookies("JSESSIONID")
    .logoutSuccessUrl("/login?logout")
)
~~~

- logoutUrl задаёт URL, который обрабатывает logout-фильтр;
- invalidateHttpSession(true) инвалидирует HTTP-сессию;
- clearAuthentication(true) очищает текущую аутентификацию;
- deleteCookies(...) просит браузер удалить указанные cookies;
- logoutSuccessUrl задаёт адрес перенаправления после выхода.

При собственной logout-логике важно не ограничиваться удалением cookie на клиенте: серверная сессия или refresh token также должны быть отозваны/инвалидированы в соответствии с архитектурой приложения.

---

## 5. HTTP-аутентификация

### Что такое HTTP Basic Authentication?

HTTP Basic — простой механизм HTTP-аутентификации. Клиент передаёт имя пользователя и пароль в заголовке:

~~~http
Authorization: Basic dXNlcjpwYXNzd29yZA==
~~~

Значение после Basic — Base64-кодирование строки username:password. **Base64 не является шифрованием**: данные легко декодировать. Поэтому Basic Authentication допустимо использовать только через HTTPS, который защищает передаваемые данные на транспортном уровне.

### Как включить httpBasic() в Spring Security?

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
}
~~~

При получении запроса с заголовком Authorization: Basic ... фильтр Spring Security извлекает credentials, создаёт объект запроса на аутентификацию и передаёт его в AuthenticationManager. Проверка может выполняться через DaoAuthenticationProvider, UserDetailsService и PasswordEncoder.

Если credentials отсутствуют или неверны, сервер отвечает отказом в аутентификации (обычно 401 Unauthorized) и может вернуть заголовок WWW-Authenticate.

Basic удобен для простых интеграций, внутренних инструментов и тестирования, но браузерный клиент может повторно отправлять credentials с запросами. Для публичных API обычно рассматривают более подходящие механизмы, например OAuth 2.0 / OpenID Connect или короткоживущие bearer-токены.

### Почему важны HTTPS и CORS?

**HTTPS обязателен**, потому что Base64 не скрывает пароль. Без TLS логин и пароль можно перехватить в сети. Кроме того, Basic credentials повторяются в каждом запросе, поэтому их компрометация особенно опасна.

**CORS (Cross-Origin Resource Sharing)** — механизм браузера, определяющий, разрешено ли web-странице с одного origin обращаться к ресурсу другого origin. CORS не является механизмом аутентификации и не заменяет авторизацию.

При использовании браузерного клиента с Basic Authentication запрос с Authorization может вызвать preflight-запрос OPTIONS. Сервер должен корректно обработать CORS и разрешить нужные origin, методы и заголовки, в частности Authorization. В Spring Security CORS обычно настраивают через http.cors(...) и CorsConfigurationSource или интеграцию с MVC-конфигурацией.

Не стоит разрешать * без необходимости, особенно если используются credentials. CORS ограничивает поведение браузеров, но не мешает произвольному HTTP-клиенту обратиться к API напрямую.

---

## 6. Безопасное хранение и управление паролями

### Почему пароли хранят в виде хеша?

Пароль — секрет, который нельзя хранить в открытом виде. В базе данных должен находиться результат специализированной password-hashing-функции, а не исходный пароль.

Для паролей используют алгоритмы, рассчитанные на проверку пароля и устойчивость к перебору: BCrypt, Argon2, scrypt, PBKDF2. Обычные быстрые хеш-функции вроде MD5 или SHA-256 без специальной схемы не подходят: злоумышленник может очень быстро перебирать варианты на GPU.

Password hashing отличается от обычного хеширования тем, что функция намеренно требует вычислительных ресурсов и использует соль. Соль не обязана быть секретной и обычно хранится вместе с хешем.

### BCryptPasswordEncoder

**BCryptPasswordEncoder** — реализация PasswordEncoder, основанная на BCrypt. Она автоматически генерирует случайную соль и включает её в сохранённое представление хеша.

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
~~~

Число 12 — strength/cost factor. Увеличение cost повышает вычислительную стоимость проверки пароля; конкретное значение нужно выбирать измерениями на целевом окружении и периодически пересматривать.

~~~java
String hash = passwordEncoder.encode("secret");
boolean matches = passwordEncoder.matches("secret", hash);
~~~

Повторный вызов encode("secret") обычно даст другое значение из-за случайной соли. Поэтому сравнивать два результата encode через equals неправильно — для этого существует matches.

### Как мигрировать старые пароли?

Предположим, в старой системе пароли хранились в виде SHA-256-хеша, а новая система использует BCrypt. Нельзя просто передать старый хеш в BCrypt и считать, что это эквивалент исходного пароля: BCrypt захеширует строку старого хеша, а не исходный пароль.

Варианты миграции:

1. **Миграция при успешном входе.** Система проверяет введённый пароль по старому алгоритму. Если проверка успешна, пароль сразу хешируется современным алгоритмом и новая запись сохраняется в БД.
2. **Принудительный сброс пароля.** Подходит, если старые хеши небезопасны, алгоритм неизвестен или невозможно надёжно реализовать проверку старого формата.
3. **Временная поддержка нескольких форматов.** Система определяет формат старого хеша, проверяет пароль соответствующим способом и постепенно переводит пользователей на новый алгоритм.

Нельзя восстановить исходный пароль из корректного одностороннего хеша. Миграция без участия пользователя возможна только при наличии исходного пароля или другого доверенного механизма повторной установки credentials.

### DelegatingPasswordEncoder

**DelegatingPasswordEncoder** позволяет поддерживать несколько форматов паролей одновременно. Обычно сохранённая строка содержит идентификатор алгоритма в префиксе:

~~~text
{bcrypt}$2a$12$...
{pbkdf2}...
{noop}...
~~~

При проверке encoder читает идентификатор и делегирует операцию соответствующей реализации. Это удобно для постепенной миграции: новые пароли кодируются выбранным современным алгоритмом, а старые ещё некоторое время могут проверяться прежним encoder.

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
~~~

Фабрика выбирает encoder для новых паролей и создаёт формат с префиксом. При миграции старых данных важно настроить допустимые идентификаторы и реализации, соответствующие фактическим форматам в БД.

**{noop}** означает хранение/проверку без хеширования и не подходит для реальных паролей. Его нельзя оставлять как способ обхода миграции в production.

---

## 7. Декларативный контроль доступа (Method Security)

### Чем URL-авторизация отличается от Method Security?

HTTP-авторизация задаёт правила на уровне запросов и маршрутов:

~~~java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
~~~

**Method Security** позволяет защищать отдельные методы приложения независимо от того, каким путём до них добрались: через REST-контроллер, другой сервис или внутренний вызов из компонента.

Обычно URL-правила и Method Security дополняют друг друга. Проверка только на уровне URL может оказаться недостаточной, если бизнес-метод вызывается из другого места.

### Как включить Method Security?

В современных версиях Spring Security используется **@EnableMethodSecurity**:

~~~java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {
}
~~~

По умолчанию @EnableMethodSecurity включает поддержку @PreAuthorize, @PostAuthorize, @PreFilter и @PostFilter. Поддержку @Secured и JSR-250-аннотаций (@RolesAllowed) нужно включить соответствующими параметрами:

~~~java
@Configuration
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class MethodSecurityConfiguration {
}
~~~

### @PreAuthorize и SpEL

**@PreAuthorize** проверяет выражение до вызова защищаемого метода. Выражение пишется на Spring Expression Language (SpEL) и может обращаться к ролям, authorities, аргументам метода и bean-компонентам.

~~~java
@Service
public class FirmwareService {

    @PreAuthorize("hasRole('ADMIN')")
    public void archive(Long firmwareId) {
        // Архивация прошивки
    }

    @PreAuthorize("hasAuthority('firmware:read')")
    public FirmwareDto findById(Long id) {
        // Получение прошивки
        return ...;
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public UserDto findUser(Long userId) {
        // Пользователь может получить собственные данные,
        // администратор — данные любого пользователя
        return ...;
    }
}
~~~

Часто используемые выражения:

- hasRole('ADMIN') — проверяет роль ROLE_ADMIN;
- hasAnyRole('ADMIN', 'SUPPORT') — одна из перечисленных ролей;
- hasAuthority('firmware:read') — конкретное полномочие;
- isAuthenticated() — пользователь аутентифицирован;
- isAnonymous() — пользователь анонимен;
- permitAll() / denyAll() — разрешить или запретить доступ;
- #id == authentication.principal.id — сравнить аргумент метода с идентификатором текущего пользователя.

В SpEL можно использовать имена аргументов метода. Для надёжного разрешения имён параметров при компиляции проекта обычно включают сохранение имён параметров (-parameters) или используют доступный механизм метаданных параметров. Не следует строить критичную авторизацию на предположении, что имя аргумента всегда будет доступно.

### Другие аннотации

**@PostAuthorize** проверяет условие после выполнения метода. Это полезно, например, когда нужно проверить владельца уже загруженного объекта:

~~~java
@PostAuthorize("returnObject.ownerId == authentication.principal.id or hasRole('ADMIN')")
public Document findDocument(Long id) {
    return repository.findById(id).orElseThrow();
}
~~~

Метод уже выполнился, поэтому @PostAuthorize не подходит для предотвращения побочных эффектов. Если метод изменяет состояние, проверку доступа лучше выполнять до операции.

**@PreFilter** фильтрует элементы входной коллекции до вызова метода, а **@PostFilter** — элементы возвращаемой коллекции после выполнения метода. Их следует применять осмотрительно: фильтрация в памяти может быть неэффективной, а для больших выборок лучше ограничивать данные на уровне запроса к БД.

**@Secured** — более простой механизм, проверяющий наличие одной из указанных authorities/ролей:

~~~java
@Secured("ROLE_ADMIN")
public void deleteUser(Long id) {
    // ...
}
~~~

**@RolesAllowed** — аннотация JSR-250:

~~~java
@RolesAllowed({"ADMIN", "SUPPORT"})
public void viewReports() {
    // ...
}
~~~

Для @RolesAllowed Spring Security по умолчанию использует соглашение с префиксом ROLE_, то есть ADMIN соответствует authority ROLE_ADMIN.

На практике @PreAuthorize часто оказывается наиболее гибким вариантом, потому что поддерживает SpEL и проверки аргументов метода.

### Как работает Method Security и какие есть ограничения?

Method Security реализован через Spring AOP: при вызове защищённого метода через Spring-бин вызов перехватывается interceptor'ом, который выполняет проверку доступа.

Отсюда следует важное ограничение — **self-invocation**. Если один метод класса вызывает другой защищённый метод того же объекта через this, вызов обычно обходит Spring-прокси, поэтому методная проверка может не сработать:

~~~java
@Service
public class ReportService {

    public void generate() {
        exportInternal(); // self-invocation: вызов минует proxy
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void exportInternal() {
        // ...
    }
}
~~~

Обычно решение — вынести защищённую операцию в отдельный Spring-бин и вызывать её через внедрённую зависимость. Proxy-based AOP перехватывает вызовы через прокси, а не произвольные прямые вызовы объекта.

Method Security не отменяет необходимость проверять бизнес-правила. Наличие роли USER ещё не означает, что пользователь имеет право изменить конкретную запись: часто нужно дополнительно проверять владельца ресурса, состояние сущности и другие условия.

---

## Что важно уметь объяснить на собеседовании

1. Запрос проходит через FilterChainProxy, который выбирает подходящую SecurityFilterChain; внутри неё фильтры выполняют аутентификацию и другие проверки.
2. AuthenticationManager делегирует проверку подходящему AuthenticationProvider. Для username/password-сценария типичная связка — DaoAuthenticationProvider, UserDetailsService и PasswordEncoder.
3. Аутентификация устанавливает личность пользователя, авторизация проверяет его права. Результат аутентификации доступен через SecurityContextHolder.
4. SecurityFilterChain — основная точка современной Java-конфигурации HTTP-безопасности. securityMatcher выбирает цепочку, а requestMatchers задаёт правила внутри неё.
5. Form login и HTTP Basic — разные способы передачи credentials, но оба могут использовать общий AuthenticationManager.
6. Пароли хранятся не в открытом виде, а в виде хешей специализированных password-hashing-алгоритмов. matches проверяет пароль, DelegatingPasswordEncoder помогает поддерживать несколько форматов.
7. URL-авторизация защищает HTTP-маршруты, а Method Security — методы приложения. @PreAuthorize позволяет выразить сложные правила через SpEL, но proxy-based подход имеет ограничение self-invocation.
