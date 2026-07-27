# Task 39 — Role-Based Access Control (RBAC)

Status: [x] Concluída

Depende de: Task 33, 37.

## Objetivo

Papéis e permissões básicas (`ADMIN` vs `USER`). Decisão registrada (ver
`README.md` § Decisões em aberto): o papel fica no próprio `User`, como
`enum`, sem entidade separada de roles/permissões.

## Arquivos

- criar: `src/main/java/com/solutis/dev/domain/model/Role.java` (`enum Role { ADMIN, USER }`)
- alterar: `src/main/java/com/solutis/dev/domain/model/User.java` (campo `role`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
  (`@Enumerated(EnumType.STRING)`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar: `src/main/java/com/solutis/dev/application/dto/user/UserResponse.java`
- alterar: `src/main/java/com/solutis/dev/application/mapper/UserMapper.java`
- criar: `src/main/java/com/solutis/dev/web/security/RequireRole.java` (anotação)
- criar: `src/main/java/com/solutis/dev/web/security/RoleAuthorizationInterceptor.java`
- criar: `src/main/java/com/solutis/dev/infrastructure/config/WebMvcConfig.java`
  (registra o interceptor)
- criar: `src/main/java/com/solutis/dev/domain/exception/AccessDeniedException.java`
- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`

## Especificação

- `User`: campo `Role role`, default `Role.USER` em `createNew`.
- `@RequireRole(Role.ADMIN)`: anotação de método, aplicável a endpoints
  que exigem papel específico (ex.: candidato natural — task 29, ativação
  em lote de notificação, mas a escolha de quais endpoints exigem
  `ADMIN` deve ser confirmada com o usuário antes de aplicar a anotação).
- `RoleAuthorizationInterceptor implements HandlerInterceptor`:
  1. Extrai o token do header `Authorization: Bearer <token>`.
  2. `authService.requireValidToken(token)` (task 35) → `userId`.
  3. Carrega o `User`, compara `user.getRole()` com o valor de
     `@RequireRole` no método do handler; se não bater, lança
     `AccessDeniedException`.
- `GlobalExceptionHandler`: `@ExceptionHandler(AccessDeniedException.class)`
  → `403` (`HttpStatus.FORBIDDEN`).
- `WebMvcConfig`: registra `RoleAuthorizationInterceptor` só para os
  paths que tiverem controllers anotados — ou aplica globalmente e deixa
  o interceptor pular métodos sem `@RequireRole`.

## Nota de implementação — confirmar antes de codificar

Quais endpoints exigem qual papel não estava no checklist original —
levantamento e confirmação com o usuário são pré-requisito antes de
aplicar `@RequireRole` em qualquer controller existente.

**Resolvido com o usuário**: apenas `POST /api/v1/users/notifications/activate`
(task 29) recebeu `@RequireRole(Role.ADMIN)`. Nenhum outro endpoint
existente foi restringido nesta task.

## Nota de implementação — ripple em `@WebMvcTest`

`WebMvcConfig implements WebMvcConfigurer` é automaticamente incluído por
qualquer slice `@WebMvcTest` (o filtro padrão do Spring Boot inclui beans
`WebMvcConfigurer`), então toda classe `@WebMvcTest` do projeto passou a
precisar de `@MockitoBean` para `AuthUseCase` e `UserRepository` (as
dependências do construtor de `WebMvcConfig`/`RoleAuthorizationInterceptor`),
mesmo quando o controller testado não usa `@RequireRole`. Já ajustado em
`UserControllerTest`, `UserCsvControllerTest`,
`UserBulkNotificationControllerTest` (que também ganhou testes com header
`Authorization: Bearer <token>` simulando ADMIN/USER) e `AuthControllerTest`
(só precisou do `UserRepository`, já tinha `AuthUseCase`). **Qualquer nova
classe `@WebMvcTest` criada em tasks futuras precisa dos mesmos dois
`@MockitoBean`.**

## Critérios de aceite

- [x] Endpoint anotado com `@RequireRole(Role.ADMIN)` chamado por um
      token de usuário `USER` → `403`.
- [x] Mesmo endpoint chamado por um token de usuário `ADMIN` → funciona
      normalmente.
- [x] Endpoint sem `@RequireRole` continua acessível independente do
      papel.
- [x] Coberto por `RoleAuthorizationInterceptorTest`/teste de integração
      dedicado (mais os casos 403/401 em `UserBulkNotificationControllerTest`).
