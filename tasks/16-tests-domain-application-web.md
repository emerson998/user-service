# Task 16 — Testes (domain / application / web)

Status: [x] Concluída e verificada — `./mvnw test` verde

Depende de: Task 01 até 15.

## Objetivo

Cobertura conforme `PLANO_IMPLEMENTACAO.md` § 2.4 e as convenções de
teste de `AGENTS.md` § "Testes".

## Arquivos

- criar: `src/test/java/com/emerson/dev/usuarios/domain/model/UserTest.java`
- criar: `src/test/java/com/emerson/dev/usuarios/application/service/UserServiceTest.java`
- criar: `src/test/java/com/emerson/dev/usuarios/web/controller/UserControllerTest.java`

## `UserTest` (JUnit 5 + AssertJ, sem mocks)

- `createNew` inicia `enabled = true`, `phone`/`bio` nulos.
- `updateProfile` atualiza os três campos.
- `changePassword` atualiza o hash.
- `activate`/`deactivate` alternam `enabled`.

## `UserServiceTest` (JUnit 5 + Mockito, mocka `UserRepository` e `PasswordEncoderPort`)

- `create`: sucesso (chama `encode`, salva, mapeia resposta).
- `create`: e-mail já existente → `DuplicateResourceException`, não
  chama `save`.
- `getById`/`update`/`delete`: id inexistente → `ResourceNotFoundException`.
- `listAll`: mapeia lista vazia e lista com itens.

## `UserControllerTest` (`@WebMvcTest` + `MockMvc`, `@MockitoBean` no `UserUseCase`)

- `POST` válido → 201.
- `POST` com `name` em branco → 400 (valida serialização do erro de
  bean validation).
- `GET /{id}` inexistente → 404 (`UserUseCase` mockado lança
  `ResourceNotFoundException`).
- `POST` com e-mail duplicado → 409.
- `DELETE` → 204.

## Critérios de aceite

- [x] `./mvnw test` verde — confirmado pelo usuário (após corrigir o
      import de `@WebMvcTest` para `org.springframework.boot.webmvc.test.autoconfigure`
      e trocar o `ObjectMapper` autowired por uma instância local no
      `UserControllerTest`).
- [x] Casos de sucesso e de erro cobertos em cada camada listada acima
      (incluindo um teste extra de `update` com sucesso em
      `UserServiceTest`, além dos casos originalmente listados).
