# Task 30 — Validation — Empty Array

Status: [ ] Pendente

Depende de: Task 29.

## Objetivo

Validar que o array de ids de usuário recebido no endpoint de ativação em
lote (task 29) não vem vazio nem nulo.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/application/dto/user/BulkNotificationActivationRequest.java`

## Especificação

- `BulkNotificationActivationRequest.userIds`: anotar com
  `@NotEmpty(message = "userIds não pode ser vazio")` (jakarta.validation,
  já disponível via `spring-boot-starter-validation`).
- Nenhuma mudança de código em `UserBulkNotificationController`/`Service`
  — o `@Valid` já existente (task 29) passa a rejeitar o payload antes de
  chegar no service, e `GlobalExceptionHandler.handleValidation` (já
  existente) já mapeia para `400`.

## Critérios de aceite

- [ ] `POST /api/v1/users/notifications/activate` com `userIds: []` ou
      `userIds` ausente devolve `400`.
- [ ] `POST` com pelo menos um id continua funcionando normalmente.
- [ ] Coberto por `UserBulkNotificationControllerTest`.
