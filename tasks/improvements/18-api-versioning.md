# Task 18 — API Versioning

Status: [x] Concluída

Depende de: baseline CRUD (tasks 00–17).

## Objetivo

`UserController` já expõe `/api/v1/users` (seção 2.3 do
`PLANO_IMPLEMENTACAO.md`), mas o prefixo `/api/v1` está hardcoded como
literal dentro do `@RequestMapping`. Esta task extrai esse prefixo para uma
constante compartilhada, para que todo controller novo criado neste lote
(CSV, bulk actions, auth, health) use exatamente o mesmo valor, sem repetir
a string literal em cada classe.

## Arquivos

- criar: `src/main/java/com/solutis/dev/web/ApiRoutes.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/UserController.java`

## Especificação

- `ApiRoutes`: classe `public final class` com construtor privado e
  `public static final String V1 = "/api/v1";`.
- `UserController` passa a usar
  `@RequestMapping(ApiRoutes.V1 + "/users")` em vez do literal.
- Convenção para as próximas tasks que criam controller novo (25, 29, 33,
  48): usar sempre `ApiRoutes.V1 + "/<recurso>"` — nenhuma rota nova deve
  hardcodar `/api/v1` de novo.

## Critérios de aceite

- [x] `ApiRoutes.V1` existe e vale `"/api/v1"`.
- [x] `UserController` usa a constante — nenhum literal `/api/v1` restante
      fora de `ApiRoutes`.
- [x] `./mvnw test` continua passando (nenhuma rota mudou de valor
      observável, só a origem do valor). **Confirmado pelo usuário**:
      `mvn clean compile` e `mvn test` rodados manualmente no IntelliJ.
