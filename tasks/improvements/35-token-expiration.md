# Task 35 — Token Expiration

Status: [ ] Pendente

Depende de: Task 33.

## Objetivo

TTL configurável para o token UUID, devolvendo `401 Unauthorized` uma vez
expirado.

## Arquivos

- alterar: `src/main/resources/application.properties`
- criar: `src/main/java/com/solutis/dev/domain/exception/InvalidTokenException.java`
- alterar: `src/main/java/com/solutis/dev/application/service/AuthService.java`
- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`

## Especificação

- `application.properties`: `app.auth.token-ttl-seconds=1800` (30 min,
  default sugerido) — já consumido por `CacheConfig` (task 33) para
  configurar o `expireAfterWrite` do cache `authTokens`. O Caffeine expira
  a entrada automaticamente; esta task cobre o **comportamento visível
  pela API** quando isso acontece.
- `InvalidTokenException extends RuntimeException` — lançada quando um
  endpoint que exige token (ex.: `logout`, task 37) recebe um token
  ausente/expirado/inválido.
- `AuthService`: método auxiliar `requireValidToken(String token): Long`
  (retorna o `userId` ou lança `InvalidTokenException`) — reaproveitado
  por `logout` (task 37) e por qualquer endpoint protegido futuro (task
  39, RBAC).
- `GlobalExceptionHandler`: novo `@ExceptionHandler(InvalidTokenException.class)`
  → `401` (`HttpStatus.UNAUTHORIZED`).
- `GET /api/v1/auth/tokens/{token}` (task 34) **não** usa
  `requireValidToken` — continua respondendo `200` com `valid: false`,
  já que o próprio propósito do endpoint é consultar status, não exigir
  autenticação.

## Critérios de aceite

- [ ] Token expirado (TTL configurado baixo em teste) → endpoints que
      exigem token (task 37) devolvem `401`.
- [ ] `GET /api/v1/auth/tokens/{token}` continua respondendo `200` mesmo
      para token expirado (`valid: false`, não `401`).
- [ ] Coberto por `AuthServiceTest` com TTL reduzido via
      `@TestPropertySource`.
