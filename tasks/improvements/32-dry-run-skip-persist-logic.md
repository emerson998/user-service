# Task 32 — Dry-Run / Skip Persist Logic

Status: [x] Concluída

Depende de: Task 29, 30, 31.

## Objetivo

Se a flag de dry-run vier ativa no payload, processar toda a validação
(array vazio, duplicados, existência) **sem** persistir a ativação da
flag de notificação em nenhum usuário.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/application/dto/user/BulkActionResult.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserBulkNotificationService.java`

## Especificação

- `BulkActionResult`: novo campo `boolean dryRun`, para deixar explícito
  na resposta que nada foi persistido.
- `UserBulkNotificationService.activate(...)`: as validações das tasks
  30/31 rodam sempre, independente de `dryRun`. Só o passo final
  (`user.enableNotifications()` + `save`) é pulado quando
  `request.dryRun() == true` — o método ainda retorna
  `BulkActionResult` com `activatedCount` refletindo quantos **seriam**
  ativados (para o chamador conseguir conferir o resultado esperado sem
  efeito colateral).

## Critérios de aceite

- [x] `dryRun: true` com payload válido devolve `200` com
      `activatedCount` correto e `dryRun: true`, mas nenhum usuário tem
      `notificationsEnabled` alterado no banco.
- [x] `dryRun: true` com payload inválido (vazio/duplicado/inexistente)
      continua devolvendo o erro correspondente (tasks 30/31) — dry-run
      não amacia validação.
- [x] `dryRun: false` (ou ausente) mantém o comportamento das tasks 29–31.
- [x] Coberto por `UserBulkNotificationServiceTest` (dry-run não persiste).
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.
