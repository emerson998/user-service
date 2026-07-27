# Task 04 — DTOs UserRequest/UserResponse

Status: [x] Concluída

Depende de: Task 01.

## Objetivo

DTOs de entrada/saída da API, como `record`, com Bean Validation no
request. Response nunca expõe senha.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/application/dto/user/UserRequest.java`
- criar: `src/main/java/com/emerson/dev/usuarios/application/dto/user/UserUpdateRequest.java`
- criar: `src/main/java/com/emerson/dev/usuarios/application/dto/user/UserResponse.java`

## `UserRequest` (usado só no `POST`/create)

```java
public record UserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        String phone,
        @Size(max = 500) String bio) {
}
```

## `UserUpdateRequest` (usado só no `PUT`/update)

```java
public record UserUpdateRequest(
        @NotBlank String name,
        String phone,
        @Size(max = 500) String bio) {
}
```

Sem `email`/`password` — o domínio só expõe `updateProfile(name, phone, bio)`
para essa rota (troca de senha é `changePassword`, sem endpoint na
seção 2.3 do plano).

## `UserResponse`

```java
public record UserResponse(
        Long id, String name, String email, String phone, String bio, boolean enabled) {
}
```

## Decisão registrada — resolvida

`PLANO_IMPLEMENTACAO.md` § 2.2 lista um único `UserRequest` para o
agregado, mas o domínio só permite `create` (nome/e-mail/senha) e
`updateProfile` (nome/telefone/bio) — dois shapes diferentes. Confirmado
com o usuário em 2026-07-23: **opção 2**, `UserUpdateRequest` separado,
por ser mais fiel ao que o domínio realmente permite mudar (o cliente não
é obrigado a mandar e-mail/senha só para atualizar o perfil).

Consequência para as tasks seguintes (atualizar quando forem
implementadas):
- Task 06 (`UserUseCase`): `update(Long id, UserUpdateRequest request)`.
- Task 08 (`UserService`): `update` recebe `UserUpdateRequest`.
- Task 14 (`UserController`): `PUT /api/v1/users/{id}` usa
  `@Valid @RequestBody UserUpdateRequest`.

## Critérios de aceite

- [x] `UserRequest`, `UserUpdateRequest` e `UserResponse` criados
      conforme especificado acima. `UserRequest`/`UserUpdateRequest` só
      serão efetivamente usados em `create`/`update` a partir das tasks
      06/08/14 (pendentes).
- [x] `UserResponse` não tem campo de senha em nenhuma forma (nem hash).
