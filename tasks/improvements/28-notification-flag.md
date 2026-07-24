# Task 28 — Notification Flag

Status: [ ] Pendente

Depende de: baseline CRUD (`User`, `UserJpaEntity`).

## Objetivo

Adicionar uma flag booleana na entidade de usuário para habilitar/desabilitar
o recebimento de notificações — base para a ativação em lote das tasks
29–32.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/domain/model/User.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar: `src/main/java/com/solutis/dev/application/dto/user/UserResponse.java`
- alterar: `src/main/java/com/solutis/dev/application/mapper/UserMapper.java`

## Especificação

- `User`: campo `boolean notificationsEnabled`, default `false` na
  criação (`createNew`) — precisa de ativação explícita (task 29), não
  vem ligado por padrão. Métodos `enableNotifications()`/
  `disableNotifications()` (paralelo a `activate()`/`deactivate()` já
  existentes).
- `UserJpaEntity`: coluna `notifications_enabled` (`boolean`, `not null`,
  default `false`).
- `UserResponse`: novo campo `notificationsEnabled` (somente leitura pela
  API pública — a ativação acontece só pelo endpoint em lote da task 29,
  não por `POST`/`PUT`/`upsert`).

## Critérios de aceite

- [ ] Novo usuário criado (`POST`, `upsert`) nasce com
      `notificationsEnabled = false`.
- [ ] `UserResponse` expõe `notificationsEnabled`.
- [ ] Coberto por `UserTest` (`enableNotifications`/`disableNotifications`).
