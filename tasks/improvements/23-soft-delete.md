# Task 23 — Soft Delete

Status: [x] Concluída

Depende de: baseline CRUD (`User`, `UserJpaEntity`, `UserRepository`,
`UserService.delete`).

## Objetivo

Substituir a remoção física por remoção lógica: `DELETE /api/v1/users/{id}`
passa a marcar o usuário como excluído (`deletedAt`) em vez de apagar a
linha, e as consultas de leitura (`findById`, `findAll`, `findByEmail`)
passam a ignorar registros marcados como excluídos por padrão.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/domain/model/User.java` (novo
  campo `deletedAt` + método `delete()`)
- alterar: `src/main/java/com/solutis/dev/domain/repository/UserRepository.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/repository/UserJpaRepository.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserService.java`

## Especificação

- `User`: campo `Instant deletedAt` (nulo = ativo); método
  `delete()` seta `deletedAt = Instant.now()`; getter `isDeleted()`.
- `UserJpaEntity`: coluna `deleted_at` (nullable).
- `UserJpaRepository`: trocar `findById`/`findAll` por versões que
  filtram `deletedAt IS NULL` (`findByIdAndDeletedAtIsNull`,
  `findAllByDeletedAtIsNull`) — usar `@Query` ou nome derivado do Spring
  Data.
- `UserRepositoryAdapter`: usar os métodos filtrados acima na
  implementação do port.
- `UserService.delete(id)`: em vez de `deleteById`, faz
  `findById` → `user.delete()` → `save`.
- `existsById`/`findByEmail`/`findByCpf` (task 21) também devem respeitar
  o filtro de `deletedAt IS NULL`, para permitir recriar um usuário com o
  mesmo e-mail/CPF de um registro soft-deleted (decisão razoável, mas
  confirmar com o usuário antes de codificar — impacta a regra de
  duplicidade da task 19/21).

## Decisão confirmada

Reuso de e-mail/CPF de usuário soft-deleted **permitido** — confirmado
com o usuário em 2026-07-24. `findByEmail`/`findByCpf`/`existsById` (e
`findById`/`findAll`) passam a usar variantes `*AndDeletedAtIsNull` no
`UserJpaRepository`.

## Mudança adicional não prevista — remoção de `deleteById`

`UserRepository.deleteById` (port) e sua implementação em
`UserRepositoryAdapter` foram **removidos**, não só alterados. Depois
desta task, `UserService.delete` nunca mais chama um delete físico —
manter `deleteById` no contrato do port seria um método morto que,
pior, contradiz a regra de negócio recém-criada (nunca apagar a linha
fisicamente por essa via). A busca "gerenciada" usada internamente por
`UserRepositoryAdapter.save` (task 22, `loadManagedEntityWithCurrentVersion`)
continua usando o `jpaRepository.findById` **bruto** (sem filtro), já
que é um detalhe técnico de rastreio de versão do Hibernate, não uma
regra de negócio de visibilidade.

## Critérios de aceite

- [x] `DELETE /api/v1/users/{id}` não remove a linha do banco — só seta
      `deleted_at`.
- [ ] `GET /api/v1/users` e `GET /api/v1/users/{id}` não retornam usuário
      soft-deleted (`404` no segundo caso) — comportamento implementado
      via `*AndDeletedAtIsNull`, mas não verificado por teste de
      repositório/integração dedicado (só coberto indiretamente, via
      mocks, nos testes de `UserServiceTest`).
- [x] Coberto por `UserServiceTest` (delete agora persiste um `User` com
      `isDeleted() == true`, não chama mais `deleteById` — método
      removido do port). **Confirmado pelo usuário**: `mvn clean compile`
      e `mvn test` rodados manualmente no IntelliJ.
