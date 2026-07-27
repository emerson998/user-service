# Task 33 — Basic Login Endpoint

Status: [x] Concluída

Depende de: Task 18, baseline CRUD (`User`, `UserRepository`).

## Objetivo

Login básico: recebe um id de usuário e devolve um token UUID válido para
autenticar as próximas requisições. Decisão registrada (ver `README.md` §
Decisões em aberto): o token fica armazenado no cache do Spring
(Caffeine), com TTL configurável — mesma infraestrutura de cache
introduzida pela task 41, adiantada aqui porque o login depende dela.

## Arquivos

- alterar: `pom.xml` (`spring-boot-starter-cache`, `com.github.ben-manes.caffeine:caffeine`)
- criar: `src/main/java/com/solutis/dev/infrastructure/config/CacheConfig.java`
- criar: `src/main/java/com/solutis/dev/application/dto/auth/LoginRequest.java`
- criar: `src/main/java/com/solutis/dev/application/dto/auth/LoginResponse.java`
- criar: `src/main/java/com/solutis/dev/application/port/out/TokenStorePort.java`
- criar: `src/main/java/com/solutis/dev/infrastructure/security/CacheTokenStoreAdapter.java`
- criar: `src/main/java/com/solutis/dev/application/port/in/AuthUseCase.java`
- criar: `src/main/java/com/solutis/dev/application/service/AuthService.java`
- criar: `src/main/java/com/solutis/dev/web/controller/AuthController.java`
- alterar: `src/main/resources/application.properties`

## Especificação

- `CacheConfig`: `@EnableCaching`, `CaffeineCacheManager` com um cache
  nomeado `authTokens`, `Caffeine.newBuilder().expireAfterWrite(ttl)` —
  TTL lido de `app.auth.token-ttl-seconds` (task 35 detalha o valor e o
  `401` na expiração; aqui só a infraestrutura de cache já nasce com TTL).
- `LoginRequest(Long userId)`, `LoginResponse(String token, Instant expiresAt)`.
- `TokenStorePort`: `String issue(Long userId)` (gera `UUID.randomUUID()`,
  guarda no cache `authTokens` com chave = token, valor = `userId`,
  devolve o token), `Optional<Long> resolve(String token)`,
  `void invalidate(String token)` (usado pela task 37).
- `CacheTokenStoreAdapter implements TokenStorePort`: injeta `CacheManager`,
  usa `cacheManager.getCache("authTokens")` para `put`/`get`/`evict`.
- `AuthUseCase.login(LoginRequest request): LoginResponse`.
- `AuthService.login(...)`: `userRepository.findById(userId)` →
  `ResourceNotFoundException` se não existir; gera token via
  `tokenStorePort.issue(userId)`; devolve `LoginResponse`.
- `AuthController`: `POST ApiRoutes.V1 + "/auth/login"`.

## Nota de implementação — confirmar antes de codificar

O requisito original pede login "recebendo um id de usuário" — sem senha.
Isso é deliberadamente simplificado (não é uma autenticação real por
credenciais). Confirmar com o usuário se este comportamento literal é
suficiente ou se deveria validar `password`/`passwordHash` do `User`
antes de emitir o token.

**Resolvido com o usuário**: comportamento literal confirmado — login
recebe só `userId`, sem senha, mesmo sendo deliberadamente simplificado.

## Critérios de aceite

- [x] `POST /api/v1/auth/login` com um `userId` existente devolve `200`
      com um token UUID.
- [x] `userId` inexistente devolve `404`.
- [x] Coberto por `AuthServiceTest`.
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.
