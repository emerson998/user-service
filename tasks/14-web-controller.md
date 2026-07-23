# Task 14 — web.controller.UserController

Status: [ ] Não iniciada

Depende de: Task 04, 06, 08.

## Objetivo

Endpoints REST do CRUD, conforme `PLANO_IMPLEMENTACAO.md` § 2.3.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/web/controller/UserController.java`

## Endpoints

| Método | Rota | Retorno |
|---|---|---|
| `POST` | `/api/v1/users` | `201 Created` (considerar header `Location`), body `UserResponse` |
| `GET` | `/api/v1/users` | `200 OK`, `List<UserResponse>` |
| `GET` | `/api/v1/users/{id}` | `200 OK`, `UserResponse` |
| `PUT` | `/api/v1/users/{id}` | `200 OK`, `UserResponse` |
| `DELETE` | `/api/v1/users/{id}` | `204 No Content` |

## Especificação

- `@RestController @RequestMapping("/api/v1/users") @Tag(name = "Users")`.
- Construtor injeta `UserUseCase` (não `UserService` diretamente).
- `@Valid @RequestBody UserRequest` no `create` (`POST`);
  `@Valid @RequestBody UserUpdateRequest` no `update` (`PUT`).
- Anotações springdoc (`@Operation`) por método.
- Não expor `User` nem `UserJpaEntity` — só os DTOs de
  `application.dto.user`.

## Critérios de aceite

- [ ] Todas as rotas da tabela implementadas.
- [ ] Coberto por `UserControllerTest` (task 16).
- [ ] Base para a validação manual da task 17.
