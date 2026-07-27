# Task 46 — Audit Logging

Status: [ ] Pendente

Depende de: Task 44.

## Objetivo

Log de auditoria capturando ator, timestamp, tipo de ação e dado
modificado, para toda operação que muda estado (create/update/upsert/
delete/bulk activation/login/logout).

## Arquivos

- criar: `src/main/java/com/solutis/dev/application/dto/audit/AuditEvent.java`
- criar: `src/main/java/com/solutis/dev/application/port/out/AuditLogPort.java`
- criar: `src/main/java/com/solutis/dev/infrastructure/audit/Slf4jAuditLogAdapter.java`
- criar: `src/main/java/com/solutis/dev/infrastructure/audit/AuditAspect.java`
- criar: `src/main/java/com/solutis/dev/infrastructure/audit/Audited.java` (anotação)
- alterar: os métodos mutáveis de `UserService`, `UserBulkNotificationService`,
  `AuthService` (anotar com `@Audited`)

## Especificação

- `AuditEvent(String actor, Instant timestamp, String actionType, String entityData)`.
- `AuditLogPort.record(AuditEvent event): void` — porta, seguindo o
  mesmo padrão de `application.port.out` já usado por
  `PasswordEncoderPort`.
- `Slf4jAuditLogAdapter implements AuditLogPort`: por enquanto só loga
  estruturado (`log.info("AUDIT actor={} action={} entity={} at={}", ...)`)
  — trocar por um sink dedicado (tabela própria, ELK, etc.) é evolução
  futura, sem mudar quem chama (mesma ideia de port/adapter já usada
  para `ErrorNotifierPort` no `PLANO_IMPLEMENTACAO.md` § 3.8).
- `@Audited(actionType = "USER_CREATE")`: anotação de método;
  `AuditAspect` (`@Aspect @Component`, `@AfterReturning`) monta o
  `AuditEvent` a partir do retorno do método e do `actionType` da
  anotação, e chama `auditLogPort.record(...)`.
- **Ator**: nesta task, sem RBAC/sessão ainda propagada ao contexto de
  request de forma centralizada — usar o `userId` do token (se o
  endpoint for autenticado, task 39) ou `"unknown"` como fallback
  explícito; revisar quando houver um `SecurityContext` real.

## Critérios de aceite

- [ ] Toda chamada a `create`/`update`/`upsert`/`delete` em `UserService`
      gera uma linha de log de auditoria com ator, timestamp, ação e o
      dado modificado.
- [ ] `activate` (bulk, task 29) e `login`/`logout` (tasks 33/37) também
      geram log de auditoria.
- [ ] Coberto por `AuditAspectTest` (usando um `AuditLogPort` mockado).
