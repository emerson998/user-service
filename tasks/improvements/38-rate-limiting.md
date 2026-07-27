# Task 38 — Rate Limiting

Status: [x] Concluída

Depende de: Task 33.

## Objetivo

Aplicar rate limit especificamente no endpoint de login, para mitigar
força bruta.

## Arquivos

- criar: `src/main/java/com/solutis/dev/infrastructure/security/LoginRateLimiter.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/AuthController.java`
- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`
- criar: `src/main/java/com/solutis/dev/domain/exception/RateLimitExceededException.java`
- alterar: `src/main/resources/application.properties`

## Especificação

- Reaproveita a infraestrutura de cache (Caffeine, task 33/41): cache
  dedicado `loginAttempts` (`expireAfterWrite` = janela de rate limit,
  ex. 1 min), chave = IP do request (`HttpServletRequest.getRemoteAddr()`),
  valor = contador de tentativas.
- `LoginRateLimiter.checkAndIncrement(String ip)`: incrementa o contador
  da chave; se ultrapassar `app.auth.login-max-attempts` (default `5`)
  dentro da janela, lança `RateLimitExceededException`.
- `AuthController.login`: chama `loginRateLimiter.checkAndIncrement(...)`
  antes de delegar para `authUseCase.login(...)`.
- `GlobalExceptionHandler`: `@ExceptionHandler(RateLimitExceededException.class)`
  → `429` (`HttpStatus.TOO_MANY_REQUESTS`).
- `application.properties`: `app.auth.login-max-attempts=5`,
  `app.auth.login-rate-limit-window-seconds=60`.

## Critérios de aceite

- [x] Mais de `app.auth.login-max-attempts` tentativas de login do mesmo
      IP dentro da janela → `429` a partir da tentativa excedente.
- [x] Login legítimo dentro do limite continua funcionando.
- [x] Coberto por `LoginRateLimiterTest`/`AuthControllerTest`.
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.

## Nota de implementação

`LoginRateLimiter` mantém seu próprio `com.github.benmanes.caffeine.cache.Cache`
interno (construído diretamente no construtor via `Caffeine.newBuilder()`),
em vez de registrar um cache nomeado no `CacheManager`/`CacheConfig` da task
33 — evita que `CacheConfig` precise gerenciar dois caches com TTLs
diferentes (`authTokens` e `loginAttempts`) através da mesma
`CaffeineCacheManager.setCaffeine(...)`, que aplica uma única config a
todos os caches que gerencia. Mantém a mesma tecnologia (Caffeine), só não
passa pela abstração `org.springframework.cache`.
