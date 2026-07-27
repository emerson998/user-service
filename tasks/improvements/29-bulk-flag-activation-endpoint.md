# Task 29 — Bulk Flag Activation Endpoint

Status: [x] Concluída

Depende de: Task 18, 28.

## Objetivo

Endpoint para ativar a flag de notificação em lote, recebendo um array de
ids de usuário no corpo da requisição.

## Arquivos

- criar: `src/main/java/com/solutis/dev/application/dto/user/BulkNotificationActivationRequest.java`
- criar: `src/main/java/com/solutis/dev/application/dto/user/BulkActionResult.java`
- criar: `src/main/java/com/solutis/dev/application/port/in/UserBulkNotificationUseCase.java`
- criar: `src/main/java/com/solutis/dev/application/service/UserBulkNotificationService.java`
- criar: `src/main/java/com/solutis/dev/web/controller/UserBulkNotificationController.java`

## Especificação

- `BulkNotificationActivationRequest` (record): `List<Long> userIds`,
  `boolean dryRun` (default tratado no service, já que `record` não tem
  valor default — controller/DTO recebe o campo, `null`/ausente vira
  `false` na leitura; ver task 32 para o comportamento do `dryRun`).
- `BulkActionResult` (record): `int activatedCount`, `List<Long> skippedIds`
  (populado pelas validações das tasks 30/31 — pode ficar vazio nesta
  task).
- `UserBulkNotificationUseCase.activate(BulkNotificationActivationRequest request): BulkActionResult`.
- `UserBulkNotificationService.activate(...)`:
  1. Para cada id em `userIds`, `userRepository.findById(id)` →
     `user.enableNotifications()` → `save`.
  2. Retorna `BulkActionResult` com a contagem de ativados.
- `UserBulkNotificationController`:
  `POST ApiRoutes.V1 + "/users/notifications/activate"`, `@Valid @RequestBody`,
  resposta `200` com `BulkActionResult`.

## Critérios de aceite

- [x] `POST /api/v1/users/notifications/activate` com uma lista de ids
      válidos ativa `notificationsEnabled` em todos eles.
- [x] Resposta inclui `activatedCount` correto.
- [x] Coberto por `UserBulkNotificationServiceTest`.
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.
