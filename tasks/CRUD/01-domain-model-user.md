# Task 01 — domain.model.User

Status: [x] Concluída (classe implementada; compilação/testes não
verificados localmente — ver nota abaixo)

## Objetivo

Classe rica de domínio, sem nenhuma anotação de framework (nem Lombok, por
convenção de `AGENTS.md`).

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/domain/model/User.java`

## Campos

`id (Long)`, `name (String)`, `email (String)`, `passwordHash (String)`,
`phone (String, opcional)`, `bio (String, opcional)`, `enabled (boolean)`.

## Construtores

- Construtor completo (todos os campos) — usado pelos mappers
  (`application.mapper.UserMapper`, `infrastructure.persistence.mapper.UserEntityMapper`)
  para reconstruir o domínio a partir de dado já existente (id não nulo).
- Factory estático `createNew(String name, String email, String passwordHash)`
  → `id = null`, `enabled = true`, `phone = null`, `bio = null`.

## Métodos de negócio

- `updateProfile(String name, String phone, String bio)`
- `changePassword(String newHash)`
- `activate()`
- `deactivate()`
- Getters para todos os campos. Sem setters soltos — toda mutação passa
  pelos métodos acima.

## Critérios de aceite

- [x] Classe implementada sem depender de `org.springframework.*` ou
      `jakarta.*` (revisão manual do código — compilação não verificada
      neste ambiente, sem acesso admin para rodar Maven/wrapper).
- [x] `createNew` não aceita `phone`/`bio` (ficam null até um
      `updateProfile`).
- [ ] Coberto por `UserTest` (task 16) — pendente, task 16 ainda não
      começou.
