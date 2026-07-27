# Task 20 — Data Validation

Status: [x] Concluída

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

## Nota de implementação

`UserUpsertRequest` já nasceu (task 19) com `@NotBlank name` e
`@NotBlank @Email email`, e `UserController.upsert` já usava `@Valid`.
`GlobalExceptionHandler.handleValidation` já cobria o caso sem alteração.
Esta task foi, na prática, a auditoria + os testes de controller que
confirmam isso.

## Critérios de aceite

- [x] `PUT /api/v1/users/upsert` com `name`/`email` ausentes ou `email`
      malformado devolve `400` com mensagem de campo (mesmo formato dos
      demais endpoints).
- [x] Nenhum DTO de usuário aceita `name`/`email` em branco.
- [x] Coberto por teste em `UserControllerTest` (caso de validação do
      upsert). **Confirmado pelo usuário**: `mvn clean compile` e
      `mvn test` rodados manualmente no IntelliJ.
