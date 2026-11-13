# Spring Security — Part 2 (вопросы 4–7)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Продолжение первой части: Form Login, HTTP Basic, безопасное хранение паролей и декларативная авторизация на уровне методов. Предполагается знакомство с базовыми понятиями Spring Security: SecurityFilterChain, Authentication, AuthenticationProvider, UserDetailsService и SecurityContextHolder.

---

## 4. Аутентификация на основе форм (Form Login)

### Что такое Form Login и как он работает?

**Form Login** — механизм аутентификации, при котором пользователь вводит имя пользователя и пароль в HTML-форму. Spring Security обрабатывает отправленные credentials, выполняет аутентификацию и при успехе сохраняет контекст безопасности для последующих запросов.

Типичный сценарий:

1. Неаутентифицированный пользователь обращается к защищённой странице.
2. Spring Security перенаправляет его на страницу входа.
3. Пользователь отправляет форму с username и password.
4. Фильтр аутентификации извлекает credentials и передаёт запрос в AuthenticationManager.
5. AuthenticationManager делегирует проверку подходящему AuthenticationProvider.
6. При успехе создаётся аутентифицированный Authentication, который помещается в SecurityContext.
7. В session-based приложении контекст может сохраняться между запросами, а пользователь перенаправляется на исходную страницу или на заданный URL.

Упрощённо Form Login использует UsernamePasswordAuthenticationFilter. По умолчанию он обрабатывает POST-запрос на /login с параметрами username и password. Сам фильтр не должен самостоятельно обращаться к базе и сравнивать пароли: он передаёт запрос в общую инфраструктуру аутентификации.

### Как включить Form Login?

Включить механизм можно через DSL HttpSecurity:

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/css/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .build();
}
~~~

При такой конфигурации Spring Security предоставляет стандартную страницу входа, если приложение не определило собственную. Для большинства браузерных приложений этого достаточно на начальном этапе.

Важно разрешить доступ к странице входа и её статическим ресурсам без аутентификации. Иначе можно получить ситуацию, когда саму страницу входа тоже пытаются защитить и пользователь не может до неё добраться.

### Как подключить собственную страницу входа через loginPage()?

Если стандартная страница не подходит, можно указать собственный URL:

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/assets/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/perform_login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/home", false)
                    .failureUrl("/login?error")
                    .permitAll()
            )
            .build();
}
~~~

Здесь:

- loginPage("/login") задаёт URL страницы, которую приложение должно отобразить. Обычно это MVC endpoint, возвращающий представление.
- loginProcessingUrl("/perform_login") задаёт URL, который обрабатывает security-фильтр. Контроллер для него обычно не нужен.
- usernameParameter("email") и passwordParameter("password") задают имена параметров формы.
- defaultSuccessUrl("/home", false) задаёт URL после успешного входа, если нет сохранённого исходного запроса.
- failureUrl("/login?error") задаёт redirect после неудачной попытки.
- permitAll() разрешает неаутентифицированным пользователям обращаться к настроенным endpoint'ам Form Login.

Пример HTML-формы (в реальном приложении её отдаёт шаблонизатор или frontend):

~~~html
<form method="post" action="/perform_login">
    <input name="email" type="text" autocomplete="username">
    <input name="password" type="password" autocomplete="current-password">
    <input type="hidden" name="_csrf" value="...">
    <button type="submit">Войти</button>
</form>
~~~

При включённой CSRF-защите форма должна отправлять CSRF-токен. В шаблонизаторах, интегрированных со Spring Security, его можно автоматически добавить в форму. Не стоит отключать CSRF только ради того, чтобы форма начала отправляться.

Если указана собственная loginPage, Spring Security не генерирует её содержимое: страницу и endpoint, который её отображает, реализует приложение.

### Как обработать успешный и неуспешный вход?

Для кастомной логики после входа можно использовать handlers.

**AuthenticationSuccessHandler** вызывается после успешной аутентификации. Например, он может записать аудит-событие или выбрать URL перенаправления:

~~~java
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // Например, записать событие аудита.
        response.sendRedirect("/home");
    }
}
~~~

**AuthenticationFailureHandler** вызывается, если аутентификация завершилась неудачей:

~~~java
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        // Не следует записывать пароль или другие credentials в лог.
        response.sendRedirect("/login?error");
    }
}
~~~

Подключение:

~~~java
.formLogin(form -> form
        .loginPage("/login")
        .successHandler(loginSuccessHandler)
        .failureHandler(loginFailureHandler)
        .permitAll()
)
~~~

Handlers могут выполнять дополнительную логику, но не должны раскрывать клиенту лишние детали ошибки. Например, сообщение «пользователь существует, но пароль неверный» помогает злоумышленнику перебирать учётные записи. Обычно наружу возвращают обобщённое сообщение.

### Как работает logout и что означают logoutUrl и invalidateHttpSession?

По умолчанию Spring Security предоставляет logout endpoint /logout. Для servlet-приложений с Form Login это обычно POST-запрос: при включённой CSRF-защите он должен содержать CSRF-токен.

Настройка:

~~~java
.logout(logout -> logout
        .logoutUrl("/logout")
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID")
        .logoutSuccessUrl("/login?logout")
)
~~~

Основные настройки:

- logoutUrl("/logout") — URL, на который отправляется запрос выхода.
- invalidateHttpSession(true) — инвалидирует текущую HTTP-сессию.
- clearAuthentication(true) — очищает текущую аутентификацию.
- deleteCookies("JSESSIONID") — просит браузер удалить указанную cookie.
- logoutSuccessUrl(...) — URL перенаправления после выхода.

В стандартной конфигурации Spring Security уже выполняет очистку security-контекста и связанные действия выхода. Явно указывать все параметры обычно необязательно, но их полезно знать, когда требуется изменить поведение.

Удаление cookie само по себе не заменяет инвалидирование серверной сессии. И наоборот, инвалидирование сессии на сервере не всегда удаляет cookie в браузере. Это разные действия.

**Короткий ответ на собеседовании:** Form Login обрабатывается security-фильтром, который извлекает credentials из POST-формы и передаёт их в AuthenticationManager. Стандартную или собственную страницу входа настраивают через formLogin() и loginPage(). Успешный и неуспешный сценарии можно кастомизировать через AuthenticationSuccessHandler и AuthenticationFailureHandler. Logout обычно очищает контекст и инвалидирует сессию; при включённой CSRF-защите запрос выхода должен содержать CSRF-токен.

---

## 5. HTTP-аутентификация

### Что такое HTTP Basic Authentication?

**HTTP Basic Authentication** — простой механизм аутентификации, определённый стандартом HTTP. Клиент передаёт имя пользователя и пароль в заголовке Authorization в формате Basic.

Значение credentials формируется так:

1. Username и password объединяются через двоеточие: username:password.
2. Полученная строка кодируется в Base64.
3. В заголовок добавляется схема Basic.

Пример:

~~~http
Authorization: Basic dXNlcjpwYXNzd29yZA==
~~~

Base64 — это кодирование, а не шифрование. Любой, кто перехватит такой заголовок, сможет декодировать credentials. Поэтому HTTP Basic в реальном приложении необходимо использовать только поверх HTTPS.

### Как Spring Security обрабатывает HTTP Basic?

Включить механизм можно через httpBasic():

~~~java
@Bean
SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
}
~~~

В цепочке участвует BasicAuthenticationFilter. Он читает заголовок Authorization, извлекает credentials, формирует объект аутентификации и передаёт его в AuthenticationManager.

Дальше используется общий механизм Spring Security: подходящий AuthenticationProvider проверяет credentials, после успеха создаётся Authentication, а контекст безопасности становится доступен при обработке запроса.

Если credentials отсутствуют или неверны, доступ к защищённому ресурсу не предоставляется. Для HTTP-клиента обычно возвращается 401 Unauthorized с challenge-заголовком WWW-Authenticate, указывающим схему аутентификации. Для браузера это может привести к появлению встроенного диалога ввода логина и пароля.

### Чем HTTP Basic отличается от Form Login?

| Критерий | Form Login | HTTP Basic |
|---|---|---|
| Передача credentials | Поля HTML-формы | Заголовок Authorization |
| Типичный клиент | Браузерное web-приложение | API-клиент, скрипт, сервис |
| Поведение при отсутствии входа | Обычно redirect на login page | Обычно 401 и WWW-Authenticate |
| Сохранение входа | Часто session-based | Клиент обычно передаёт credentials в каждом запросе |
| CSRF | Важен при cookie/session-аутентификации | Зависит от того, как клиент передаёт credentials; браузерные сценарии требуют отдельного анализа |

HTTP Basic сам по себе не создаёт stateless-архитектуру автоматически. Stateless/session policy — отдельная настройка, и её следует выбирать осознанно. Клиент обычно повторно отправляет заголовок на каждый запрос, но Spring Security всё равно формирует SecurityContext для текущей обработки.

### Почему обязательны HTTPS и корректная настройка CORS?

**HTTPS** защищает канал связи: шифрует заголовок Authorization при передаче и помогает обеспечить целостность соединения и проверку сервера. Без HTTPS Base64 не защищает пароль от перехвата.

**CORS (Cross-Origin Resource Sharing)** — механизм браузера, который определяет, разрешено ли JavaScript-коду с одного origin обращаться к другому origin. Для cross-origin запроса с Authorization браузер обычно выполняет preflight-запрос OPTIONS, поэтому сервер должен корректно разрешить origin, метод и заголовок.

При этом CORS:

- не аутентифицирует пользователя;
- не заменяет авторизацию;
- не шифрует трафик;
- не является защитой от CSRF.

CORS — это политика доступа браузера к cross-origin ответам, а не универсальная защита API. Настраивать его нужно под конкретные доверенные origin'ы, а не разрешать все источники без необходимости.

**Короткий ответ на собеседовании:** HTTP Basic передаёт username:password, закодированные в Base64, в заголовке Authorization. Spring Security обрабатывает заголовок через BasicAuthenticationFilter и общий механизм AuthenticationManager / AuthenticationProvider. Base64 не шифрует данные, поэтому Basic нужно использовать только через HTTPS. CORS требуется учитывать для браузерных cross-origin запросов, но он не заменяет аутентификацию и авторизацию.

---

## 6. Безопасное хранение и управление паролями

### Почему пароли нельзя хранить в открытом виде или шифровать обратимым способом?

Пароль — секрет, который пользователь должен доказать при входе, но приложению не требуется восстанавливать его исходное значение. Поэтому обычно хранят не пароль, а результат специализированного **password hashing**.

Если база данных утечёт:

- открытые пароли сразу станут известны атакующему;
- обратимое шифрование создаст риск раскрытия всех паролей при компрометации ключа;
- быстрые хеш-функции позволяют очень быстро проверять огромное количество догадок.

Для хранения паролей используют алгоритмы, специально рассчитанные на защиту от перебора: они применяют соль и требуют заметных вычислительных ресурсов. Подходящие варианты включают BCrypt, Argon2, PBKDF2 и SCrypt.

### Что такое соль и почему хеширование должно быть медленным?

**Соль (salt)** — случайное значение, которое добавляется при вычислении хеша пароля. Она гарантирует, что одинаковые пароли не должны приводить к одинаковым сохранённым хешам, и мешает эффективно использовать заранее вычисленные таблицы.

Соль не обязана быть секретной: она обычно хранится вместе с хешем. Важно, чтобы она генерировалась корректно и была уникальной для каждого пароля.

**Медленный password hashing** увеличивает стоимость каждой попытки перебора. Это не делает слабый пароль неуязвимым, но повышает цену атаки при утечке базы.

Нельзя подменять password hashing обычным быстрым SHA-256 или MD5. Для паролей нужны специализированные алгоритмы с параметрами стоимости, которые можно настраивать с учётом возможностей сервера.

### Какую роль играет PasswordEncoder в Spring Security?

Интерфейс PasswordEncoder абстрагирует алгоритм хранения и проверки паролей. Основные методы:

- encode(rawPassword) — кодирует пароль для сохранения;
- matches(rawPassword, encodedPassword) — проверяет введённый пароль относительно сохранённого;
- upgradeEncoding(encodedPassword) — сообщает, стоит ли обновить сохранённый формат или параметры кодирования.

Важно: password hashing обычно не является обратимым. Поэтому при входе нельзя повторно вызвать encode() и сравнить две строки: алгоритм может использовать новую случайную соль. Для проверки предназначен matches().

### Как работает BCryptPasswordEncoder?

**BCrypt** — адаптивный алгоритм хеширования паролей. Он использует соль и параметр вычислительной стоимости (strength/work factor), влияющий на время вычисления.

Пример регистрации:

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// При регистрации:
String encodedPassword = passwordEncoder.encode(rawPassword);
userRepository.save(new User(email, encodedPassword));
~~~

Проверка пароля:

~~~java
boolean matches = passwordEncoder.matches(
        rawPassword,
        user.getPasswordHash()
);
~~~

Практические замечания:

- одинаковые исходные пароли при разных вызовах encode() обычно дают разные строки из-за случайной соли;
- matches() извлекает необходимые параметры и соль из сохранённого значения;
- strength нужно выбирать измерением на целевой инфраструктуре: слишком низкое значение ослабляет защиту, чрезмерно высокое может перегрузить приложение;
- BCrypt имеет ограничение на длину учитываемого пароля (традиционно 72 байта), поэтому при выборе алгоритма и проектировании политики паролей это ограничение нужно учитывать.

### Что такое DelegatingPasswordEncoder?

В реальном приложении алгоритм хранения паролей может меняться. Например, часть пользователей зарегистрировалась давно, когда применялся BCrypt с одними параметрами, а новые пароли уже хранятся с другим алгоритмом.

DelegatingPasswordEncoder позволяет хранить идентификатор алгоритма вместе с хешем. Типичный формат:

~~~text
{bcrypt}$2a$10$...
{pbkdf2}...
{argon2}...
~~~

Префикс в фигурных скобках сообщает, какой encoder нужно использовать для проверки конкретного значения.

Пример создания:

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
~~~

По умолчанию фабрика выбирает актуальный для данной версии Spring Security encoder для новых паролей и формирует значения с идентификатором. При проверке DelegatingPasswordEncoder выбирает delegate по сохранённому префиксу.

Не следует полагаться на то, что выбранный по умолчанию алгоритм всегда один и тот же во всех версиях Spring Security: при обновлении зависимостей полезно проверять актуальную документацию и явно фиксировать политику, если это важно для совместимости.

### Как мигрировать старые пароли на новый алгоритм?

Допустим, в базе уже хранятся BCrypt-хеши без префикса, а приложение переходит на DelegatingPasswordEncoder. Нельзя просто поменять encoder и ожидать, что старые значения автоматически распознаются: для старых записей может потребоваться отдельная стратегия совместимости.

Возможные варианты:

1. **Поддержать legacy-формат на время миграции.** Создать encoder, который умеет проверять старый формат, и использовать его для соответствующих записей.
2. **Мигрировать при успешном входе.** Если пароль успешно проверен старым алгоритмом, приложение уже имеет введённый пользователем пароль в памяти запроса. Можно пересчитать хеш новым encoder и сохранить его.
3. **Потребовать сброс пароля.** Это разумно, если старый алгоритм небезопасен, невозможно надёжно проверить старые значения или есть подозрение на компрометацию.

В Spring Security для постепенного обновления может использоваться upgradeEncoding(). В стандартном сценарии DaoAuthenticationProvider может работать с механизмом обновления пароля через UserDetailsPasswordService, если он настроен. Сама миграция требует продуманной совместимости форматов и обновления записи в БД.

Нельзя «перехешировать» старый хеш новым алгоритмом так, как будто это исходный пароль: результатом станет хеш от старого хеша, и обычный вход с исходным паролем перестанет работать, если отдельно не реализовать такую схему проверки.

### Какие практики хранения паролей стоит назвать на собеседовании?

- Хранить только специализированный password hash, никогда не открытый пароль.
- Использовать уникальную соль; современные password encoders обычно управляют ею самостоятельно.
- Подбирать параметры стоимости измерением на реальной инфраструктуре.
- Не писать пароли и credentials в логи, трассировки и сообщения об ошибках.
- Использовать TLS при передаче пароля от клиента к серверу.
- Иметь стратегию обновления алгоритмов и параметров, а также сброса скомпрометированных паролей.
- Не реализовывать собственный алгоритм хеширования, если можно использовать проверенную библиотеку.

**Короткий ответ на собеседовании:** пароли хранят не в открытом виде и не в обратимо зашифрованном виде, а в виде результата специализированного медленного password hashing с солью. В Spring Security за это отвечает PasswordEncoder; для BCrypt используется BCryptPasswordEncoder, а для поддержки нескольких форматов и постепенной миграции — DelegatingPasswordEncoder. При проверке вызывают matches(), а не сравнивают повторно вычисленный хеш.

---

## 7. Декларативный контроль доступа (Authorization)

### Что такое декларативная авторизация и зачем она нужна?

До этого мы настраивали доступ на уровне HTTP-запросов через authorizeHttpRequests. Но иногда недостаточно проверить только URL и роль пользователя.

Например, метод getInvoice(id) может быть доступен сотрудникам с правом чтения счетов, но конкретный пользователь должен видеть только счета своего подразделения. Такая проверка зависит от аргументов метода и бизнес-данных.

**Method Security** позволяет объявлять правила доступа непосредственно на методах Spring-бинов с помощью аннотаций. Это помогает держать правила рядом с защищаемой операцией и повторно применять их независимо от того, откуда вызван метод — из MVC-контроллера, другого сервиса или другого компонента.

Method Security дополняет URL-авторизацию, а не обязательно заменяет её. Часто используют оба уровня: HTTP-правила задают общий периметр, а методовые — более точные бизнес-ограничения.

### Как включить Method Security?

В современных версиях Spring Security для этого используют @EnableMethodSecurity:

~~~java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {
}
~~~

Включение этой аннотации регистрирует инфраструктуру перехвата вызовов методов и проверки правил.

По умолчанию @EnableMethodSecurity включает поддержку pre/post-аннотаций (@PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter). Поддержка @Secured и JSR-250 (@RolesAllowed) включается отдельными параметрами:

~~~java
@Configuration
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class MethodSecurityConfiguration {
}
~~~

@EnableGlobalMethodSecurity — старый способ конфигурации, который встречается в прежних проектах. В новых приложениях следует ориентироваться на @EnableMethodSecurity.

### Как использовать @PreAuthorize?

@PreAuthorize проверяет условие **до вызова метода**. В выражении можно обращаться к ролям, authorities, principal, аргументам метода и Spring-бинам.

Пример ограничения по роли:

~~~java
@Service
public class UserAdministrationService {

    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(Long userId) {
        // Отключение пользователя
    }
}
~~~

Пример проверки конкретного права:

~~~java
@PreAuthorize("hasAuthority('invoice:read')")
public Invoice getInvoice(Long id) {
    // Получение счёта
}
~~~

Пример проверки аргумента метода:

~~~java
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
public UserProfile getProfile(Long userId) {
    // Получение профиля
}
~~~

Здесь пользователь может получить собственный профиль, а администратор — профиль любого пользователя. Выражения обрабатываются через **SpEL (Spring Expression Language)**. Доступны специальные значения, например authentication, principal, а также аргументы метода.

Если проверка зависит от сложной бизнес-логики, выражение не нужно превращать в большой фрагмент SpEL. Лучше вынести проверку в отдельный Spring-компонент:

~~~java
@Component("invoiceAuthorization")
public class InvoiceAuthorization {

    public boolean canRead(Authentication authentication, Long invoiceId) {
        // Проверка владельца, подразделения и других бизнес-условий
        return /* результат проверки */;
    }
}
~~~

~~~java
@PreAuthorize("@invoiceAuthorization.canRead(authentication, #id)")
public Invoice getInvoice(Long id) {
    // Получение счёта
}
~~~

В реальном коде canRead должен выполнить полноценную проверку на основе доверенной identity и данных домена. Нельзя полагаться на идентификатор владельца, который клиент передал без проверки.

### Что делают @PostAuthorize, @PreFilter и @PostFilter?

Кроме @PreAuthorize, Spring Security предоставляет другие pre/post-аннотации:

- **@PostAuthorize** — проверяет условие после выполнения метода. В SpEL доступен returnObject, то есть возвращённый результат. Это полезно, когда решение о доступе зависит от объекта, который метод уже загрузил.
- **@PreFilter** — фильтрует входную коллекцию или массив до передачи в метод.
- **@PostFilter** — фильтрует коллекцию или массив, возвращаемые методом.

Пример:

~~~java
@PostAuthorize("returnObject.ownerEmail == authentication.name")
public Document getDocument(Long id) {
    return documentRepository.findById(id)
            .orElseThrow();
}
~~~

Метод сначала получает документ, затем результат проверяется по владельцу. Если условие не выполняется, доступ к результату запрещается исключением авторизации.

@PostAuthorize не отменяет того факта, что тело метода уже выполнилось. Поэтому её нельзя использовать как единственную защиту операций с побочными эффектами: запись в БД или внешнее действие могли произойти до проверки. Для запрета выполнения операции используйте предварительную проверку.

### Чем отличаются @Secured, @RolesAllowed и @PreAuthorize?

| Аннотация | Назначение | Особенности |
|---|---|---|
| @Secured | Ограничение доступа по перечисленным authority/ролям | Более простой механизм, без SpEL |
| @RolesAllowed | Проверка ролей по стандарту JSR-250 | Использует имена ролей; требуется jsr250Enabled = true |
| @PreAuthorize | Проверка выражения до вызова метода | Поддерживает SpEL, аргументы метода, principal, authorities и вызовы компонентов |

Примеры:

~~~java
@Secured("ROLE_ADMIN")
public void archiveFirmware(Long id) {
}

@RolesAllowed({"ADMIN", "SUPPORT"})
public Firmware getFirmware(Long id) {
    return firmwareRepository.findById(id).orElseThrow();
}

@PreAuthorize("hasRole('ADMIN') or hasAuthority('firmware:archive')")
public void archiveFirmware(Long id) {
}
~~~

У @Secured обычно указывают полное имя authority, например ROLE_ADMIN. У @RolesAllowed указывают имена ролей без префикса ROLE_. В @PreAuthorize можно использовать hasRole('ADMIN') или hasAuthority('ROLE_ADMIN') — в зависимости от принятой модели полномочий.

На практике @PreAuthorize наиболее гибкая и часто используется в новых приложениях. Но не нужно выбирать её автоматически для любого простого ограничения: простая аннотация может быть понятнее.

### Как SpEL помогает строить правила доступа?

**SpEL** — язык выражений Spring. В контексте Method Security он позволяет описывать условия доступа.

Часто используемые выражения:

~~~java
@PreAuthorize("isAuthenticated()")
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
@PreAuthorize("hasAuthority('firmware:read')")
@PreAuthorize("#ownerId == authentication.principal.id")
@PreAuthorize("@permissionService.canEdit(authentication, #id)")
~~~

Полезные элементы:

- isAuthenticated() — пользователь аутентифицирован;
- hasRole(...) / hasAnyRole(...) — проверка роли или одной из ролей;
- hasAuthority(...) / hasAnyAuthority(...) — проверка конкретного полномочия;
- authentication — текущий объект Authentication;
- principal — субъект аутентификации;
- #parameterName — аргумент метода;
- @beanName — обращение к Spring-бину.

Чтобы обращаться к параметрам по имени, приложение должно сохранять имена параметров в bytecode или использовать подходящую настройку/способ разрешения имён параметров. Если имя аргумента не распознаётся, выражение с #parameterName может не работать. Это стоит учитывать при сборке и тестировании.

### Как Method Security технически перехватывает вызовы?

Method Security основана на Spring AOP и механизме перехвата вызовов методов Spring-бинов. Вокруг вызова создаётся interceptor, который проверяет условие и только после успешной проверки передаёт управление целевому методу.

Упрощённая схема:

~~~text
Вызов метода Spring-бина
        |
        v
AOP proxy / Method Security interceptor
        |
        v
Проверка @PreAuthorize
        |
        +--> отказ: AccessDeniedException
        |
        v
Вызов целевого метода
        |
        v
Проверка @PostAuthorize (если задана)
        |
        v
Возврат результата
~~~

Отсюда следуют ограничения, которые важно понимать:

- Защита применяется к вызовам через Spring proxy. Вызов метода из другого метода того же объекта (this.someMethod()) обычно обходит proxy и может не запустить Method Security на вызываемом методе.
- Метод должен быть частью Spring-managed bean, чтобы инфраструктура могла его перехватить.
- Нельзя считать аннотацию на произвольном объекте гарантией безопасности.
- При проектировании лучше выносить защищаемую операцию в отдельный Spring-компонент и вызывать её через внедрённую зависимость, а не рассчитывать на self-invocation.
- Методовую авторизацию нужно тестировать интеграционно, проверяя как разрешённый, так и запрещённый сценарии.

### Где лучше проверять доступ: в HTTP-конфигурации или на методе?

Это не взаимоисключающие способы.

**HTTP-авторизация** подходит для правил на уровне endpoint'ов: публичный URL, доступ только для аутентифицированных пользователей, административный API.

**Method Security** подходит для правил на уровне бизнес-операций: доступ к конкретному объекту, проверка владельца, подразделения, состояния ресурса или сочетания нескольких полномочий.

Например:

~~~java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
)
~~~

А внутри сервиса:

~~~java
@PreAuthorize("@invoiceAuthorization.canRead(authentication, #id)")
public Invoice getInvoice(Long id) {
    // ...
}
~~~

Первый уровень ограничивает доступ к административным endpoint'ам, второй защищает бизнес-операцию с конкретным счётом. Даже если сервис будет вызван из другого контроллера или компонента, правило на методе продолжит действовать — при условии, что вызов проходит через Spring proxy.

**Короткий ответ на собеседовании:** Method Security позволяет защищать методы Spring-бинов декларативно. Её включают через @EnableMethodSecurity, а наиболее гибкая аннотация — @PreAuthorize, поддерживающая SpEL, роли, authorities, principal и аргументы метода. @Secured и @RolesAllowed подходят для более простых ролевых проверок. Механизм основан на AOP proxy, поэтому self-invocation может обходить проверку. Method Security дополняет HTTP-авторизацию и особенно полезна для бизнес-правил доступа к конкретным данным.

---

## Что разобрать в следующих частях

В этой части рассмотрены вопросы 4–7. В следующих частях остаются:

- **8. Реализация кастомной логики аутентификации и авторизации** — загрузка пользователей и ролей из БД, расширение UserDetails, настройка AuthenticationManager.
- **9. Работа с фильтрами безопасности** — OncePerRequestFilter, порядок фильтров и полная цепочка.
- **10. Управление пользовательскими сессиями** — session-based authentication, sessionManagement(), параллельные сессии и сохранение SecurityContext.
- **13. JWT** — структура токена, подпись, claims, срок действия, stateless-аутентификация и интеграция в filter chain.
