# Task 19 — Upsert Endpoint

Status: [x] Concluída

Depende de: Task 18, baseline CRUD (`UserService`, `UserRepository`,
`UserController`).

## Objetivo

Endpoint que atualiza o usuário se ele já existir (chave natural: e-mail),
ou cria um novo caso não exista — distinto do `POST` (sempre cria, task 00
do CRUD) e do `PUT /{id}` (sempre atualiza por id, também do CRUD
baseline).

## Arquivos

- criar: `src/main/java/com/solutis/dev/application/dto/user/UserUpsertRequest.java`
- alterar: `src/main/java/com/solutis/dev/application/port/in/UserUseCase.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserService.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/UserController.java`

## Especificação

- `UserUpsertRequest` (record): `@NotBlank name`, `@NotBlank @Email email`,
  `password` (sem `@NotBlank` — só é obrigatório no caminho de criação,
  validado em código, não em anotação), `phone`, `@Size(max = 500) bio`.
- `UserUseCase.upsert(UserUpsertRequest request): UserResponse` — novo
  método na porta.
- `UserService.upsert(...)`:
  1. `userRepository.findByEmail(request.email())`.
  2. Se presente: `user.updateProfile(name, phone, bio)`; se
     `request.password()` não for nulo/branco, também
     `user.changePassword(passwordEncoderPort.encode(...))`; `save`.
  3. Se ausente: exige `request.password()` não nulo/branco (senão
     `IllegalArgumentException` — mapear em `GlobalExceptionHandler` como
     `400`, ver task 44); `User.createNew(name, email, hash)`; `save`.
  4. Mapear para `UserResponse` (adicionar `UserMapper.toDomain`/overload
     equivalente para `UserUpsertRequest`, análogo ao que já existe para
     `UserRequest`).
- Rota: `PUT ApiRoutes.V1 + "/users/upsert"` em `UserController` (rota
  própria, sem id no path, para não colidir com `PUT /{id}`).

## Nota de implementação — decisão confirmada

"Senha obrigatória só na criação, opcional na atualização" — confirmado
com o usuário em 2026-07-24.

## Critérios de aceite

- [x] `PUT /api/v1/users/upsert` cria usuário novo quando o e-mail não
      existe (senha obrigatória).
- [x] Mesma rota atualiza nome/telefone/bio (e senha, se enviada) quando o
      e-mail já existe.
- [x] Coberto por teste em `UserServiceTest` (branch create e branch
      update). **Confirmado pelo usuário**: `mvn clean compile` e
      `mvn test` rodados manualmente no IntelliJ.
