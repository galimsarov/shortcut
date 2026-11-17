# Spring Security — Part 3 (вопросы 8–10 и 13)

> Конспект для подготовки к техническому собеседованию на позицию Java-разработчика Middle / Middle+. Третья часть посвящена подключению собственного источника пользователей и кастомизации аутентификации, устройству security-фильтров, управлению HTTP-сессиями и аутентификации с помощью JWT. Предполагается знакомство с Authentication, AuthenticationProvider, SecurityContext, SecurityFilterChain и базовыми механизмами Spring Security из предыдущих частей.

---

## 8. Реализация кастомной логики аутентификации и авторизации

### Как Spring Security получает информацию о пользователях?

Spring Security не требует, чтобы пользователи обязательно хранились в конкретной базе данных или имели определённую структуру таблиц. Фреймворку нужен адаптер, который позволяет получить сведения о пользователе и проверить его credentials.

Один из основных контрактов для этого — **UserDetailsService**:

~~~java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
~~~

Метод получает логин (username) и должен вернуть объект UserDetails. Если пользователь не найден, обычно выбрасывается UsernameNotFoundException.

UserDetailsService отвечает именно за загрузку пользователя. Сам по себе он не сравнивает пароль с введённым значением и не принимает решение о доступе к конкретному endpoint'у. Проверкой credentials занимается AuthenticationProvider, а авторизацией — механизмы авторизации Spring Security.

### Как реализовать UserDetailsService для пользователей из БД?

Предположим, в приложении есть сущности User и Role. Пользователь хранится в таблице, а роли связаны с ним отношением many-to-many или отдельной таблицей связей.

Пример сервиса:

~~~java
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return new AppUserPrincipal(user);
    }
}
~~~

Здесь UserRepository — обычный Spring Data repository. Важно заранее продумать загрузку ролей и authorities: если они ленивые, то их нужно получить в рамках транзакции или использовать подходящий fetch-запрос.

Не следует возвращать из этого метода JPA-сущность напрямую только потому, что она содержит username и password. Лучше отделить модель хранения от security-представления пользователя.

### Что такое UserDetails и зачем нужна собственная реализация?

**UserDetails** — интерфейс Spring Security, который представляет пользователя с точки зрения механизма аутентификации и авторизации.

Он содержит, в частности, следующие сведения:

- username;
- password — хеш пароля, а не исходный пароль;
- authorities — выданные пользователю полномочия;
- accountNonExpired — не истёк ли срок действия учётной записи;
- accountNonLocked — не заблокирована ли учётная запись;
- credentialsNonExpired — не истёк ли срок действия credentials;
- enabled — активна ли учётная запись.

Если стандартного User недостаточно, можно создать собственную реализацию:

~~~java
public class AppUserPrincipal implements UserDetails {

    private final User user;

    public AppUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public Long getId() {
        return user.getId();
    }
}
~~~

Это иллюстрация: реальные флаги состояния лучше брать из модели пользователя, если приложение их поддерживает. Не нужно бездумно возвращать true для всех проверок, если в доменной модели есть блокировка, отключение или срок действия учётной записи.

**GrantedAuthority** — отдельное полномочие, которое участвует в проверках доступа. Например, ROLE_ADMIN или firmware:publish. Роли обычно представляются authority с префиксом ROLE_, а permissions могут быть более детальными строковыми значениями.

В Spring Security важно различать:

- hasRole("ADMIN") — проверяет authority ROLE_ADMIN, автоматически добавляя префикс ROLE_;
- hasAuthority("ROLE_ADMIN") — проверяет точное значение authority;
- hasAuthority("firmware:publish") — пример проверки отдельного permission.

Не стоит одновременно добавлять ROLE_ в базе и повторно добавлять его при преобразовании, если итоговое значение станет ROLE_ROLE_ADMIN.

### Как связаны UserDetailsService, PasswordEncoder и DaoAuthenticationProvider?

Для классической аутентификации по логину и паролю часто используется **DaoAuthenticationProvider**.

Его задача — получить пользователя через UserDetailsService и проверить введённый пароль с помощью PasswordEncoder.

Упрощённый процесс:

~~~text
UsernamePasswordAuthenticationToken
              |
              v
      AuthenticationManager
              |
              v
       ProviderManager
              |
              v
    DaoAuthenticationProvider
              |
              +----> UserDetailsService
              |          |
              |          v
              |     UserDetails
              |
              +----> PasswordEncoder.matches()
                         |
                         v
              Authentication result
~~~

Пример конфигурации:

~~~java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Bean
DaoAuthenticationProvider daoAuthenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
) {
    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
}

@Bean
AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration
) throws Exception {
    return configuration.getAuthenticationManager();
}
~~~

Конкретные конструкторы и доступные методы могут немного различаться между версиями Spring Security, поэтому пример следует сверять с используемой версией. Смысл конфигурации остаётся тем же: зарегистрировать источник пользователей, PasswordEncoder и провайдер, который умеет их использовать.

Пароль в БД хранится в виде стойкого хеша. При входе PasswordEncoder.matches(rawPassword, encodedPassword) сравнивает введённый пароль с сохранённым хешем. Нельзя сравнивать строки напрямую или хешировать пароль вручную тем же способом без учёта соли и параметров алгоритма.

### Как получить AuthenticationManager и зачем он нужен?

**AuthenticationManager** — точка входа в процесс аутентификации. Обычно приложение не должно самостоятельно вызывать UserDetailsService и проверять пароль: эту работу следует делегировать настроенному AuthenticationManager.

В Spring Boot/Spring Security часто получают его через AuthenticationConfiguration:

~~~java
@Bean
AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration
) throws Exception {
    return configuration.getAuthenticationManager();
}
~~~

Это бывает нужно, например, если приложение реализует собственный endpoint входа и хочет вручную передать credentials в общий механизм аутентификации:

~~~java
Authentication authentication =
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
~~~

Если проверка прошла успешно, возвращённый Authentication будет аутентифицированным. Если credentials неверны, механизм выбросит AuthenticationException.

Важно: сам факт вызова authenticate() ещё не означает, что результат автоматически сохранится для следующих HTTP-запросов. Для session-based сценария нужно корректно сохранить SecurityContext через соответствующий SecurityContextRepository или использовать стандартный механизм фильтра аутентификации. Для stateless JWT-сценария контекст обычно формируется заново на каждом запросе из проверенного токена.

### Когда нужен собственный AuthenticationProvider?

UserDetailsService и DaoAuthenticationProvider подходят для стандартной схемы username/password. Но иногда требуется другой способ проверки:

- одноразовый код;
- API key;
- внешний сервис идентификации;
- комбинация нескольких credentials;
- собственный протокол или нестандартные правила проверки.

В таких случаях можно реализовать **AuthenticationProvider**. Он определяет, какие типы Authentication поддерживает, и выполняет проверку:

~~~java
@Component
public class ApiKeyAuthenticationProvider
        implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String apiKey = (String) authentication.getCredentials();

        // Проверить ключ через подходящий сервис.
        // При неуспехе выбросить AuthenticationException.

        return new ApiKeyAuthenticationToken(
                authentication.getPrincipal(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiKeyAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
~~~

Это схематичный пример: тип токена должен быть реализован приложением, а сам ключ нельзя записывать в логи или возвращать в аутентифицированном объекте. В реальном коде также нужно корректно обрабатывать отсутствующий или некорректный ключ и не допускать случайного принятия неподдерживаемого типа Authentication.

**Короткий ответ на собеседовании:** для загрузки пользователей из БД обычно реализуют UserDetailsService, который возвращает UserDetails с username, хешем пароля, authorities и состоянием учётной записи. DaoAuthenticationProvider использует этот сервис и PasswordEncoder для проверки логина и пароля. AuthenticationManager координирует аутентификацию через AuthenticationProvider. Если стандартной проверки недостаточно, можно реализовать собственный AuthenticationProvider и собственный тип Authentication.

---

## 9. Работа с фильтрами безопасности

### Какую роль фильтры играют в Spring Security?

В servlet-приложении Spring Security обрабатывает запросы до попадания в DispatcherServlet. Для этого используется цепочка фильтров Servlet API.

Каждый фильтр может выполнить определённую работу до вызова следующего фильтра, после него или вообще завершить обработку запроса. Например:

- загрузить или подготовить SecurityContext;
- обработать CSRF-токен;
- извлечь credentials;
- выполнить аутентификацию;
- обработать исключение безопасности;
- проверить права доступа;
- добавить security headers.

Фильтры выполняются в определённом порядке. Этот порядок принципиален: например, авторизация должна выполняться после того, как механизм аутентификации получил возможность установить пользователя.

### Как устроена цепочка фильтров Spring Security?

Упрощённая схема прохождения запроса:

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
FilterChainProxy
    |
    v
SecurityFilterChain
    |
    +--> SecurityContextHolderFilter
    |
    +--> HeaderWriterFilter
    |
    +--> CsrfFilter
    |
    +--> LogoutFilter
    |
    +--> Authentication filters
    |
    +--> ExceptionTranslationFilter
    |
    +--> AuthorizationFilter
    |
    v
DispatcherServlet
    |
    v
Controller
~~~

Это концептуальная схема, а не гарантированный список и порядок фильтров для любой конфигурации. Реальный набор зависит от включённых механизмов и версии Spring Security.

Ключевые компоненты:

- **DelegatingFilterProxy** — servlet-фильтр, который передаёт запрос Spring-бину.
- **FilterChainProxy** — центральный фильтр Spring Security, выбирающий подходящую SecurityFilterChain.
- **SecurityFilterChain** — цепочка внутренних фильтров для определённой группы запросов.
- **SecurityContextHolderFilter** — обеспечивает доступ к контексту безопасности в рамках обработки запроса.
- **ExceptionTranslationFilter** — преобразует исключения безопасности в HTTP-поведение: например, запускает entry point для неаутентифицированного клиента или обращается к access denied handler.
- **AuthorizationFilter** — выполняет проверку авторизации для HTTP-запроса на основании настроенных правил.

Не нужно запоминать весь внутренний список фильтров наизусть. Для собеседования важнее понимать назначение основных этапов и уметь объяснить, почему порядок фильтров имеет значение.

### Что такое OncePerRequestFilter?

**OncePerRequestFilter** — базовый абстрактный класс Spring Web, предназначенный для фильтров, логика которых должна выполняться один раз в рамках соответствующего request dispatch.

Он часто используется для прикладных фильтров, например для извлечения JWT из заголовка Authorization и проверки токена.

Пример каркаса:

~~~java
@Component
public class ExampleFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Логика фильтра до передачи запроса дальше.

        filterChain.doFilter(request, response);

        // При необходимости логика после выполнения следующих фильтров.
    }
}
~~~

Важные моменты:

- filterChain.doFilter(request, response) передаёт управление следующему фильтру. Если его не вызвать, дальнейшая цепочка не выполнится.
- Код до doFilter() выполняется до следующих фильтров, код после — после возврата из цепочки.
- Фильтр может завершить обработку сам, например вернуть 401, но тогда нужно осознанно сформировать HTTP-ответ.
- OncePerRequestFilter не означает буквально один вызов на всё время жизни запроса при любых обстоятельствах: servlet async и error dispatch имеют отдельные особенности. Поведение можно уточнять через соответствующие методы класса.

Не следует путать OncePerRequestFilter с механизмом аутентификации. Это удобная основа для собственного фильтра, но проверка credentials, создание Authentication и обработка ошибок остаются ответственностью реализации.

### Как добавить собственный фильтр в SecurityFilterChain?

Spring Security позволяет встроить фильтр относительно известного фильтра:

~~~java
@Bean
SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        ExampleFilter exampleFilter
) throws Exception {
    return http
            .addFilterBefore(
                    exampleFilter,
                    UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated()
            )
            .build();
}
~~~

Основные методы:

- addFilterBefore(filter, ExistingFilter.class) — поставить фильтр перед указанным;
- addFilterAfter(filter, ExistingFilter.class) — поставить после;
- addFilterAt(filter, ExistingFilter.class) — поставить на позицию указанного фильтра.

Последний вариант требует особой осторожности: несколько фильтров на одной позиции не обязательно образуют предсказуемый порядок. Обычно лучше выбрать before/after относительно подходящего стандартного фильтра.

Выбор позиции зависит от задачи. Например, фильтр, который извлекает и проверяет JWT, должен выполниться до этапа авторизации, чтобы Authentication уже был доступен проверке доступа. Но нет универсального правила «все пользовательские фильтры ставить перед UsernamePasswordAuthenticationFilter»: нужно понимать, какие механизмы включены в конкретной цепочке и с какими из них фильтр взаимодействует.

### Почему опасно просто объявить фильтр как @Component?

Если фильтр является Spring-бином, Spring Boot может автоматически зарегистрировать его ещё и как обычный servlet-фильтр контейнера. Если одновременно добавить его через addFilterBefore() в Spring Security, он рискует выполниться дважды: отдельно в servlet chain и внутри SecurityFilterChain.

Это может привести к неожиданному порядку выполнения, повторной обработке запроса и проблемам с SecurityContext.

Один из вариантов — отключить servlet-регистрацию для такого фильтра:

~~~java
@Bean
FilterRegistrationBean<ExampleFilter> disableServletRegistration(
        ExampleFilter filter
) {
    FilterRegistrationBean<ExampleFilter> registration =
            new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
}
~~~

После этого фильтр остаётся Spring-бином и может быть добавлен в SecurityFilterChain, но не регистрируется контейнером как отдельный servlet-фильтр.

Другой подход — создавать экземпляр фильтра непосредственно в конфигурации, не делая его отдельным servlet-компонентом. Какой вариант выбрать, зависит от жизненного цикла и зависимостей фильтра.

### Как взаимодействуют фильтры аутентификации, обработки исключений и авторизации?

Упрощённо запрос проходит следующие этапы:

1. Фильтры контекста обеспечивают доступ к SecurityContext.
2. Фильтр аутентификации извлекает credentials и при необходимости вызывает AuthenticationManager.
3. При успехе Authentication становится доступен через SecurityContextHolder.
4. ExceptionTranslationFilter обрабатывает исключения, связанные с отказом в доступе, которые возникают ниже по цепочке.
5. AuthorizationFilter проверяет, разрешён ли текущему пользователю доступ к запрошенному ресурсу.
6. Если проверка прошла, запрос передаётся дальше, обычно в DispatcherServlet.

Если пользователь не аутентифицирован, механизм обработки исключений может инициировать authentication entry point — например, вернуть 401 для API или перенаправить браузер на страницу входа. Если пользователь аутентифицирован, но прав недостаточно, обычно возвращается 403 Forbidden.

**Короткий ответ на собеседовании:** Spring Security использует FilterChainProxy, который выбирает подходящую SecurityFilterChain. Внутри неё фильтры выполняют загрузку контекста, аутентификацию, обработку исключений и авторизацию. Для собственных servlet-фильтров часто используют OncePerRequestFilter, а в цепочку безопасности добавляют их через addFilterBefore/After. Важно не допустить двойной регистрации фильтра и правильно выбрать его позицию относительно стандартных фильтров.

---

## 10. Управление пользовательскими сессиями

### Как Spring Security использует HTTP-сессии?

В классическом браузерном приложении после успешной аутентификации сервер может сохранить состояние пользователя между HTTP-запросами.

Обычно схема выглядит так:

1. Пользователь отправляет логин и пароль.
2. Spring Security аутентифицирует пользователя.
3. В SecurityContext сохраняется объект Authentication.
4. Контекст сохраняется в HTTP-сессии через механизм SecurityContextRepository.
5. В последующих запросах браузер отправляет cookie с идентификатором сессии.
6. Сервер находит сессию и восстанавливает контекст безопасности.

**SecurityContextHolder** предоставляет доступ к SecurityContext в текущем потоке выполнения. Сам контекст содержит Authentication, а Authentication — principal и authorities.

Важно различать SecurityContextHolder и хранилище контекста. SecurityContextHolder — API доступа к текущему контексту, а SecurityContextRepository отвечает за его загрузку и сохранение между запросами.

В современных версиях Spring Security поведение сохранения контекста зависит от используемых фильтров и конфигурации. При ручной аутентификации нельзя считать, что одного вызова SecurityContextHolder.getContext().setAuthentication(...) достаточно для сохранения входа на следующие запросы: при необходимости нужно сохранить контекст через SecurityContextRepository.

### Что такое SessionCreationPolicy?

Политика создания сессии задаётся через sessionManagement():

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .build();
}
~~~

Основные варианты:

| Политика | Значение |
|---|---|
| ALWAYS | Сессия создаётся всегда |
| IF_REQUIRED | Сессия создаётся, когда она нужна |
| NEVER | Spring Security не создаёт сессию, но может использовать уже существующую |
| STATELESS | Spring Security не создаёт сессию и не использует её для получения SecurityContext |

Для обычного session-based web-приложения часто подходит IF_REQUIRED. Для API, где каждый запрос аутентифицируется независимо, обычно выбирают STATELESS.

Важная оговорка: STATELESS в Spring Security не означает, что никакой компонент приложения физически не может создать HttpSession. Это означает, что Spring Security не использует HTTP-сессию для сохранения security-контекста в рамках этой политики. Само приложение и другие компоненты всё ещё должны соблюдать выбранную архитектуру.

### Как настроить ограничение количества одновременных сессий?

Spring Security умеет ограничивать число активных сессий одного пользователя. Например, можно разрешить только один одновременный вход:

~~~java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .sessionManagement(session -> session
                    .maximumSessions(1)
            )
            .build();
}
~~~

Поведение при превышении лимита зависит от настроек:

- по умолчанию более старая сессия может быть инвалидирована, чтобы освободить место для новой;
- можно запретить новый вход, пока уже существует максимально допустимое число сессий, используя maxSessionsPreventsLogin(true).

Пример:

~~~java
.sessionManagement(session -> session
        .maximumSessions(1)
        .maxSessionsPreventsLogin(true)
)
~~~

Для корректного учёта сессий могут понадобиться SessionRegistry и публикация событий завершения HTTP-сессии. В servlet-приложениях часто регистрируют HttpSessionEventPublisher:

~~~java
@Bean
HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
~~~

Он позволяет SessionRegistry получать события создания и уничтожения сессий от servlet-контейнера. Конкретная конфигурация может отличаться в зависимости от версии Spring Security и способа хранения сессий.

При нескольких экземплярах приложения локальный SessionRegistry сам по себе не становится распределённым хранилищем. Для кластера нужно отдельно продумать общее хранилище сессий и согласованный учёт активных входов.

### Что такое защита от session fixation?

**Session fixation** — атака, при которой злоумышленник пытается заставить пользователя аутентифицироваться с идентификатором сессии, который злоумышленнику уже известен.

Если идентификатор сессии не меняется после входа, атакующий потенциально может использовать его для доступа к уже аутентифицированной сессии.

Spring Security предоставляет защиту от session fixation. В servlet-приложении при успешной аутентификации идентификатор сессии обычно меняется или сессия пересоздаётся в зависимости от стратегии и возможностей контейнера.

Поведение можно настраивать:

~~~java
.sessionManagement(session -> session
        .sessionFixation(fixation -> fixation.changeSessionId())
)
~~~

Распространённые стратегии:

- changeSessionId() — изменить идентификатор существующей сессии;
- newSession() — создать новую сессию, не перенося в неё все атрибуты старой;
- migrateSession() — создать новую сессию и перенести атрибуты;
- none() — не применять защитную стратегию (обычно не рекомендуется без веской причины).

Для современных servlet-контейнеров changeSessionId() обычно является подходящим вариантом.

### Как завершить сессию пользователя и что происходит при logout?

При выходе из приложения важно не просто удалить cookie в браузере, а завершить серверную сессию и очистить связанные данные безопасности.

В стандартном сценарии Spring Security logout выполняет очистку SecurityContext, инвалидирует HTTP-сессию и очищает связанные cookies в соответствии с конфигурацией.

Пример настройки:

~~~java
.logout(logout -> logout
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID")
)
~~~

Удаление cookie и инвалидирование сессии — разные действия. Cookie хранит идентификатор, а серверная сессия содержит состояние. Если удалить только cookie, серверная сессия может продолжить существовать до истечения срока действия или явной очистки.

Для административного завершения сессий других пользователей обычно используют SessionRegistry или механизм управления сессиями, соответствующий инфраструктуре приложения.

### Как сессии соотносятся с CSRF?

Если браузер автоматически отправляет cookie сессии, сервер может принимать аутентифицированные запросы, инициированные с другого сайта. Это один из сценариев CSRF.

Поэтому в session-based приложениях CSRF-защита обычно важна: небезопасные операции должны требовать CSRF-токен, который атакующий сайт не может просто получить и подставить.

Для stateless API нельзя автоматически заключать, что CSRF не нужен. Если credentials автоматически прикладываются браузером (например, cookie), риск сохраняется. Если же клиент явно передаёт bearer-токен в Authorization и браузер не прикладывает его автоматически, типичный CSRF-риск существенно отличается. Решение принимают исходя из способа передачи credentials, а не только из слова stateless.

**Короткий ответ на собеседовании:** в session-based приложении SecurityContext с Authentication может сохраняться между запросами в HTTP-сессии. SessionCreationPolicy определяет, создаёт ли Spring Security сессию и использует ли её для контекста. Можно ограничить число одновременных сессий через maximumSessions(), настроить поведение при превышении лимита и защиту от session fixation. При logout нужно очистить контекст и завершить серверную сессию. CSRF особенно важен, когда браузер автоматически отправляет session cookie.

---

## 13. JWT (JSON Web Tokens) для аутентификации

### Что такое JWT и из каких частей он состоит?

**JWT (JSON Web Token)** — компактный формат представления набора claims, то есть утверждений о субъекте или контексте авторизации. В типичном API JWT используется как bearer-токен: клиент предъявляет его в запросах, а сервер проверяет подпись и допустимость токена.

Подписанный JWT обычно состоит из трёх частей, разделённых точками:

~~~text
header.payload.signature
~~~

1. **Header** — метаданные токена: тип и алгоритм подписи.
2. **Payload** — claims: сведения о субъекте и другие атрибуты.
3. **Signature** — криптографическая подпись, позволяющая проверить целостность и подлинность токена.

Пример условного содержимого:

~~~json
{
  "alg": "RS256",
  "typ": "JWT"
}
~~~

~~~json
{
  "sub": "user-123",
  "iss": "https://auth.example.com",
  "aud": "firmware-api",
  "iat": 1794900000,
  "exp": 1794900900,
  "roles": ["ROLE_AUTHOR"]
}
~~~

В компактном представлении header и payload кодируются Base64URL, затем подписывается соответствующая последовательность байтов. Подписанный JWT не зашифрован автоматически: содержимое payload можно декодировать и прочитать. Поэтому туда нельзя помещать пароли, секреты и чувствительные персональные данные.

### Чем отличаются подпись и шифрование JWT?

Подписанный JWT обычно является **JWS (JSON Web Signature)**. Подпись позволяет получателю убедиться, что содержимое не изменили и токен сформирован обладателем соответствующего ключа.

Подпись не скрывает содержимое. Base64URL — это кодирование, а не шифрование.

**JWE (JSON Web Encryption)** — формат зашифрованного представления, который обеспечивает конфиденциальность содержимого при корректном использовании криптографических алгоритмов и ключей.

В типичной архитектуре access token часто подписывают, а не шифруют: клиенту и resource server может быть полезно прочитать claims, но подлинность и неизменность проверяются подписью. Если содержимое должно быть скрыто от клиента или промежуточных участников, рассматривают JWE либо другой подход к передаче чувствительных данных.

### Какие claims бывают в JWT?

Claims — это утверждения, передаваемые в payload.

К стандартным зарегистрированным claims относятся:

- **iss (issuer)** — кто выпустил токен;
- **sub (subject)** — субъект, к которому относится токен, часто идентификатор пользователя;
- **aud (audience)** — для какого получателя или API предназначен токен;
- **exp (expiration time)** — время истечения срока действия;
- **nbf (not before)** — токен не должен приниматься до указанного времени;
- **iat (issued at)** — время выпуска;
- **jti (JWT ID)** — уникальный идентификатор токена.

Приложение может добавлять собственные claims, например tenant_id, roles или scope. Но claims нужно проектировать осознанно:

- не помещать в токен секреты и данные, которые не должны быть доступны его владельцу;
- не делать токен чрезмерно большим;
- не считать любое поле payload достоверным до проверки подписи;
- не использовать claims из токена без проверки issuer, audience и назначения токена;
- учитывать, что роли и права внутри токена могут устареть до окончания его срока действия.

JWT — это формат контейнера для claims, а не готовая система ролей и разрешений.

### Как работают access token и refresh token?

В распространённой схеме используются два токена:

- **Access token** — короткоживущий токен, который клиент предъявляет при обращении к API.
- **Refresh token** — более долгоживущий credential, который используется для получения новой пары токенов после истечения access token.

Упрощённый сценарий:

~~~text
Client
  |
  | username + password
  v
Authorization Server
  |
  | access token + refresh token
  v
Client
  |
  | Authorization: Bearer <access_token>
  v
Resource Server / API
  |
  | validate token
  v
Protected resource

Access token expired:
Client ---- refresh token ----> Authorization Server
Client <--- new token pair ---- Authorization Server
~~~

Access token обычно делают короткоживущим, чтобы уменьшить окно риска при компрометации. Refresh token требует более строгой защиты: его можно хранить в серверном хранилище в виде хеша, ротировать при использовании, отзывать и связывать с конкретной сессией или устройством.

JWT не требует обязательного наличия refresh token. Это отдельный механизм жизненного цикла токенов, который выбирается архитектурой приложения.

### Как устроена stateless-аутентификация с JWT?

При session-based аутентификации сервер хранит состояние пользователя в HTTP-сессии. При stateless-подходе resource server не использует HTTP-сессию для восстановления аутентификации: клиент передаёт bearer-токен с каждым запросом, а сервер проверяет его.

Обычно запрос выглядит так:

~~~http
GET /api/profile HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOi...
~~~

Общий процесс:

1. Клиент получает access token у сервера аутентификации.
2. При каждом запросе к API отправляет токен в заголовке Authorization.
3. API извлекает bearer token.
4. Проверяет подпись, срок действия и необходимые claims.
5. Если токен валиден, создаёт Authentication и помещает его в SecurityContext на время обработки запроса.
6. Механизм авторизации проверяет права и принимает решение о доступе.

Stateless не означает отсутствие состояния вообще. Сервер может хранить refresh tokens, список отозванных токенов, пользовательские данные, аудит и другие сведения. Stateless здесь означает отсутствие необходимости хранить HTTP-сессию для аутентификации каждого API-запроса.

### Как подключить JWT-аутентификацию в Spring Security?

Для типичного API, принимающего JWT, предпочтительно использовать готовую поддержку **OAuth2 Resource Server**, а не писать собственную криптографическую проверку и JWT-фильтр с нуля.

В проект обычно добавляют зависимость:

~~~xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
~~~

Далее задают источник открытых ключей или issuer. Например, при наличии discovery endpoint:

~~~yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com
~~~

Если discovery недоступен или конфигурация задаётся иначе, можно указать jwk-set-uri. Конкретные настройки зависят от authorization server и используемой схемы ключей.

SecurityFilterChain:

~~~java
@Bean
SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    return http
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults())
            )
            .build();
}
~~~

Resource Server подключает необходимую инфраструктуру обработки bearer-токенов. В частности, BearerTokenAuthenticationFilter извлекает токен, а JwtDecoder проверяет его и преобразует claims в Jwt. Затем создаётся Authentication, доступный механизмам авторизации.

При использовании issuer-uri Spring Security может проверять подпись и стандартные временные claims, а также issuer. Проверку audience и дополнительные бизнес-требования при необходимости настраивают отдельно. Нельзя считать, что одна лишь успешная проверка подписи означает, что токен предназначен именно этому API.

### Как связать JWT claims с ролями и authorities?

По умолчанию Resource Server преобразует scopes в authorities с префиксом SCOPE_. Например, claim scope со значением firmware:read может стать authority SCOPE_firmware:read.

Если в токене используются собственные claims, например roles, можно настроить JwtAuthenticationConverter и JwtGrantedAuthoritiesConverter.

Схематичный пример:

~~~java
@Bean
JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    authoritiesConverter.setAuthoritiesClaimName("roles");
    authoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

    return converter;
}
~~~

Здесь предполагается, что claim roles уже содержит полноценные authority, например ROLE_ADMIN. Если в токене лежат значения ADMIN и AUTHOR, нужно преобразовать их в ROLE_ADMIN и ROLE_AUTHOR либо использовать другой согласованный формат.

Подключение converter:

~~~java
.oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
        )
)
~~~

Формат claims должен быть согласован между authorization server и API. Не стоит одновременно полагаться на scopes и roles, не определив, чем они отличаются и где именно проверяются.

### Как реализовать собственный JWT-фильтр и когда он действительно нужен?

Если готовая поддержка Resource Server не подходит, можно реализовать собственный фильтр на основе OncePerRequestFilter. Его типичная ответственность:

1. Прочитать Authorization.
2. Проверить, что схема — Bearer.
3. Извлечь токен.
4. Передать токен специализированному компоненту, который проверяет подпись и claims.
5. При успехе создать Authentication и установить его в SecurityContext.
6. Продолжить цепочку фильтров.

Упрощённый каркас:

~~~java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            jwtTokenService.validateAndGetAuthentication(token)
                    .ifPresent(authentication -> {
                        SecurityContext context =
                                SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);
                    });
        }

        filterChain.doFilter(request, response);
    }
}
~~~

Это только каркас, а не готовая реализация JWT-проверки. В реальном проекте нужно корректно различать отсутствие токена и невалидный токен, возвращать согласованный ответ 401, не пропускать запрос с некорректным bearer token как анонимный без осознанного решения и не писать токен в логи.

Проверку подписи нельзя заменять простым декодированием payload. Нельзя доверять алгоритму, который клиент указал в header, без строгой серверной политики. Нужно проверять разрешённый алгоритм, ключ, подпись, exp, nbf, issuer, audience и назначение токена. Следует также продумать ротацию ключей и допустимое расхождение часов.

Если фильтр вручную устанавливает SecurityContext, нужно понимать используемую модель сохранения контекста. В stateless-сценарии контекст создаётся для текущего запроса и не должен сохраняться в HTTP-сессию. При ручной аутентификации в session-based приложении дополнительно требуется корректно сохранить контекст через SecurityContextRepository.

### Какие ограничения и риски есть у JWT?

JWT упрощает передачу проверяемых утверждений между сервисами, но не решает все задачи безопасности автоматически.

Основные риски и меры:

- **Кража токена.** Bearer token может использовать любой, кто им завладел. Нужны HTTPS, безопасное хранение на клиенте и короткий срок жизни access token.
- **Невозможность мгновенного отзыва без дополнительного механизма.** Уже выданный токен может оставаться валидным до exp. Для отзыва применяют короткий TTL, denylist/jti, ротацию ключей или другие механизмы с учётом их стоимости.
- **Устаревшие роли.** Если права пользователя изменились, claims в уже выданном токене не обновятся сами. Срок жизни токена и стратегия обновления прав должны учитывать это.
- **Непроверенные claims.** Проверки подписи недостаточно: важны issuer, audience, exp, nbf и назначение токена.
- **Небезопасная реализация криптографии.** Не нужно писать собственные алгоритмы подписи, шифрования и проверки, если можно использовать поддерживаемые библиотеки и Spring Security Resource Server.
- **Утечка через логи и URL.** Токен не следует передавать в query-параметрах или записывать в access/application logs.

JWT — это формат токена, а не синоним OAuth 2.0. OAuth 2.0 описывает протокол авторизации, OpenID Connect — слой аутентификации поверх OAuth 2.0, а JWT может использоваться как формат access token или ID token в соответствующих системах.

**Короткий ответ на собеседовании:** JWT обычно состоит из header, payload и signature. Подписанный токен позволяет проверить целостность и подлинность, но не скрывает payload. В stateless API клиент передаёт access token в Authorization: Bearer, сервер проверяет подпись, срок действия, issuer и audience, после чего формирует Authentication для текущего запроса. В Spring Security предпочтительно использовать OAuth2 Resource Server с JwtDecoder и JwtAuthenticationConverter. Собственный OncePerRequestFilter нужен только при особых требованиях, а проверку токена нельзя сводить к декодированию Base64URL.

---

## Что разобрать далее

В этой части рассмотрены вопросы 8–10 и 13. Основные темы Spring Security из текущего плана теперь охвачены; при дальнейшем углублении можно отдельно разобрать OAuth 2.0 / OpenID Connect, архитектуру authorization server и resource server, тестирование security-конфигурации и типичные ошибки интеграции.
