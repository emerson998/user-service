# Task 12 — infrastructure.persistence.adapter.UserRepositoryAdapter

Status: [ ] Não iniciada

Depende de: Task 02, 09, 10, 11.

## Objetivo

Implementação do port `domain.repository.UserRepository` usando JPA —
fecha a inversão de dependência (domínio define o contrato, infra
satisfaz).

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/infrastructure/persistence/adapter/UserRepositoryAdapter.java`

## Especificação

- `@Component implements UserRepository`.
- Construtor injeta `UserJpaRepository`.
- Cada método delega para o JPA repository e converte via
  `UserEntityMapper`:
  - `save`: `toEntity` → `jpaRepository.save` → `toDomain`.
  - `findById`/`findByEmail`: `Optional<UserJpaEntity>.map(UserEntityMapper::toDomain)`.
  - `findAll`: `.stream().map(toDomain).toList()`.
  - `deleteById`, `existsById`: delega direto.

## Critérios de aceite

- [ ] Nenhum vazamento de `UserJpaEntity` para fora deste arquivo.
- [ ] `UserService` (task 08) só conhece `domain.repository.UserRepository`.
