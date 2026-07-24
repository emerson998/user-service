# Task 22 — Optimistic Locking

Status: [ ] Pendente

Depende de: baseline CRUD (`UserJpaEntity`).

## Objetivo

Implementar controle de concorrência otimista (`@Version`) na entidade de
usuário, para detectar updates concorrentes sobre o mesmo registro
(pré-requisito da task 45, que trata o conflito como `409`).

## Arquivos

- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar (avaliar): `src/main/java/com/solutis/dev/domain/model/User.java`

## Especificação

- `UserJpaEntity`: novo campo `@Version private Long version;` — só na
  entidade JPA, **não** no domínio (o domínio não deveria conhecer
  detalhe de persistência; ver `AGENTS.md` § "Por que essa separação
  existe").
- `UserEntityMapper.toEntity`/`toDomain`: a versão não atravessa para
  `User` — ao converter entidade → domínio, a versão simplesmente não é
  copiada; ao converter domínio → entidade para um `save` de update, o
  Hibernate já rastreia a versão pelo id gerenciado (não precisa
  repassar manualmente, desde que o fluxo de update sempre passe por um
  `findById` antes do `save`, como já é o caso em
  `UserService.update`/`upsert`).
- Nenhuma mudança de contrato pública (`UserResponse` não expõe versão).

## Critérios de aceite

- [ ] `UserJpaEntity` tem campo `@Version`.
- [ ] Update concorrente do mesmo registro (dois `save` a partir do mesmo
      estado carregado) lança `OptimisticLockingFailureException` — testar
      manualmente ou via teste de integração dedicado.
- [ ] `./mvnw test` continua passando sem alteração de comportamento nos
      fluxos normais (sem concorrência).
