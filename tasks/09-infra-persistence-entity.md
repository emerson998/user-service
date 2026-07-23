# Task 09 — infrastructure.persistence.entity.UserJpaEntity

Status: [x] Concluída

Depende de: Task 01.

## Objetivo

Entidade JPA isolada do domínio — campos espelham `User`, mas é uma classe
deliberadamente separada (ver `AGENTS.md` § "Por que essa separação
existe").

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/infrastructure/persistence/entity/UserJpaEntity.java`

## Especificação

- `@Entity @Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))`.
- Campos: `id (@Id @GeneratedValue(strategy = GenerationType.IDENTITY))`,
  `name`, `email (nullable = false)`, `passwordHash`, `phone`, `bio`,
  `enabled`.
- Construtor protegido sem args (exigência do JPA) + construtor completo.
- Getters (e setters só se necessário para o Hibernate — preferir manter
  o mínimo).
- Sem Lombok (convenção de `AGENTS.md` para entidades JPA).

## Critérios de aceite

- [x] `email` com unique constraint no schema gerado (`ddl-auto=update`
      no H2 em dev/test) — declarada via `@UniqueConstraint`; validar em
      runtime quando possível.
- [x] Usada só por `UserJpaRepository` (task 10), `UserEntityMapper`
      (task 11) e `UserRepositoryAdapter` (task 12) — nunca exposta pelo
      controller.
