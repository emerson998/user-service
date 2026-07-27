# Task 37 — Logout Endpoint

Status: [x] Concluída

Depende de: Task 33, 35.

## Objetivo

Endpoint para revogar/invalidar manualmente um token ativo.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/application/port/in/AuthUseCase.java`
- alterar: `src/main/java/com/solutis/dev/application/service/AuthService.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/AuthController.java`

## Especificação

- `AuthUseCase.logout(String token): void` — novo método.
- `AuthService.logout(...)`: `requireValidToken(token)` (task 35 — lança
  `InvalidTokenException`/`401` se o token já estiver
  ausente/expirado/invalidado); em seguida
  `tokenStorePort.invalidate(token)`.
- `AuthController`: `POST ApiRoutes.V1 + "/auth/logout"`, token recebido
  via header `Authorization: Bearer <token>` (extrair o valor após o
  prefixo `"Bearer "`).

## Critérios de aceite

- [x] `POST /api/v1/auth/logout` com token válido → `200`/`204`, e uma
      consulta seguinte em `GET /api/v1/auth/tokens/{token}` (task 34)
      passa a devolver `valid: false`.
- [x] `POST /api/v1/auth/logout` com token ausente/expirado → `401`.
- [x] Coberto por `AuthServiceTest` (token válido invalida, token
      ausente/expirado lança `InvalidTokenException`) e por
      `AuthControllerTest` (204 com `Bearer`, 401 sem header/header
      malformado/token inválido).
