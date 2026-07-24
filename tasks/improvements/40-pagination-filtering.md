# Task 40 — Pagination & Filtering

Status: [ ] Pendente

Depende de: Task 23.

## Objetivo

Adicionar paginação e filtros de consulta na listagem de usuários
(`GET /api/v1/users`), sem vazar tipos do Spring Data (`Pageable`/`Page`)
para o domínio — ver `AGENTS.md` § "Por que essa separação existe"
(domínio não conhece framework).

## Arquivos

- criar: `src/main/java/com/solutis/dev/domain/repository/PageQuery.java`
- criar: `src/main/java/com/solutis/dev/domain/repository/PageResult.java`
- criar: `src/main/java/com/solutis/dev/domain/repository/UserFilter.java`
- alterar: `src/main/java/com/solutis/dev/domain/repository/UserRepository.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/repository/UserJpaRepository.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- alterar: `src/main/java/com/solutis/dev/application/port/in/UserUseCase.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserService.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/UserController.java`

## Especificação

- `PageQuery(int page, int size)` — records simples, sem depender de
  `org.springframework.data.domain.Pageable`.
- `PageResult<T>(List<T> content, long totalElements, int totalPages)`.
- `UserFilter(String name, String email, Boolean enabled)` — todos os
  campos opcionais (`null` = sem filtro naquele campo); `name`/`email`
  filtram por `LIKE` (contains, case-insensitive).
- `UserRepository`: novo método
  `PageResult<User> findAll(PageQuery pageQuery, UserFilter filter)` —
  **mantém** o `findAll()` sem paginação existente (usado internamente
  pelo export de CSV, task 24, que precisa da lista completa).
- `UserJpaRepository`: usar `Specification<UserJpaEntity>` (Spring Data
  JPA) ou métodos derivados combinados, respeitando o filtro de
  `deletedAt IS NULL` (task 23) sempre.
- `UserRepositoryAdapter`: converte `PageQuery`/`UserFilter` para
  `Pageable`/`Specification` do Spring Data **dentro da infraestrutura**
  — só ali o tipo do Spring Data é usado, nunca atravessa para
  `application`/`domain`.
- `UserController.listAll`: novos `@RequestParam` opcionais `page`
  (default `0`), `size` (default `20`), `name`, `email`, `enabled`;
  monta `PageQuery`/`UserFilter` e delega para
  `userUseCase.listAll(pageQuery, filter)` (sobrecarga do método
  existente, que continua sem argumentos para outros usos internos, se
  necessário, ou substituído — avaliar ao implementar).

## Critérios de aceite

- [ ] `GET /api/v1/users?page=0&size=10` devolve no máximo 10 itens e os
      metadados de paginação (`totalElements`, `totalPages`).
- [ ] `GET /api/v1/users?name=jo` filtra por nome contendo "jo"
      (case-insensitive).
- [ ] Usuário soft-deleted (task 23) nunca aparece, com ou sem filtro.
- [ ] Coberto por `UserServiceTest`/`UserControllerTest`.
