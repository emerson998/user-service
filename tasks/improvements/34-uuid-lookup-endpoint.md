# Task 34 — UUID Lookup Endpoint

Status: [x] Concluída

Depende de: Task 33.

## Objetivo

Endpoint para inspecionar/consultar o status e os detalhes de um token
UUID ativo.

## Arquivos

- criar: `src/main/java/com/solutis/dev/application/dto/auth/TokenStatusResponse.java`
- alterar: `src/main/java/com/solutis/dev/application/port/in/AuthUseCase.java`
- alterar: `src/main/java/com/solutis/dev/application/service/AuthService.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/AuthController.java`

## Especificação

- `TokenStatusResponse` (record): `boolean valid`, `Long userId`
  (nulo se `valid = false`).
- `AuthUseCase.lookup(String token): TokenStatusResponse` — novo método.
- `AuthService.lookup(...)`: `tokenStorePort.resolve(token)` — presente →
  `TokenStatusResponse(true, userId)`; ausente (nunca existiu ou já
  expirou/foi invalidado, tasks 35/37) → `TokenStatusResponse(false, null)`.
- `AuthController`: `GET ApiRoutes.V1 + "/auth/tokens/{token}"`.

## Critérios de aceite

- [x] Token recém-emitido (task 33) → `GET .../auth/tokens/{token}`
      devolve `valid: true` e o `userId` correspondente.
- [x] Token inexistente/já expirado devolve `valid: false`.
- [x] Coberto por `AuthServiceTest`.
