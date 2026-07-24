# Task 20 — Data Validation

Status: [ ] Pendente

Depende de: Task 19.

## Objetivo

Garantir validação obrigatória de campo (`name`, `email`) de forma
consistente em **todos** os DTOs de entrada relacionados a usuário
existentes até aqui (`UserRequest`, `UserUpdateRequest`, `UserUpsertRequest`),
fechando qualquer lacuna deixada pela task 19.

## Arquivos

- alterar (se necessário): `src/main/java/com/solutis/dev/application/dto/user/UserUpsertRequest.java`
- alterar (se necessário): `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`

## Especificação

- Auditar `UserRequest`, `UserUpdateRequest`, `UserUpsertRequest`: `name` e
  `email` (quando presentes no DTO) devem ter `@NotBlank`; `email` deve ter
  `@Email`. Isso já vale para `UserRequest`/`UserUpdateRequest` (CRUD
  baseline) — confirmar que `UserUpsertRequest` (task 19) segue o mesmo
  padrão.
- Garantir que `UserController` anota `@Valid` em todos os métodos que
  recebem esses DTOs (já vale para `create`/`update`; adicionar em
  `upsert`, task 19).
- Confirmar que `GlobalExceptionHandler.handleValidation` (já existente,
  mapeia `MethodArgumentNotValidException` → `400`) cobre o novo endpoint
  sem alteração — não deve ser necessário criar handler novo.

## Critérios de aceite

- [ ] `PUT /api/v1/users/upsert` com `name`/`email` ausentes ou `email`
      malformado devolve `400` com mensagem de campo (mesmo formato dos
      demais endpoints).
- [ ] Nenhum DTO de usuário aceita `name`/`email` em branco.
- [ ] Coberto por teste em `UserControllerTest` (caso de validação do
      upsert).
