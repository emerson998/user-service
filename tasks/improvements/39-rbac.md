# Task 39 — Role-Based Access Control (RBAC)

Status: [ ] Pendente

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

## Critérios de aceite

- [ ] Endpoint anotado com `@RequireRole(Role.ADMIN)` chamado por um
      token de usuário `USER` → `403`.
- [ ] Mesmo endpoint chamado por um token de usuário `ADMIN` → funciona
      normalmente.
- [ ] Endpoint sem `@RequireRole` continua acessível independente do
      papel.
- [ ] Coberto por `RoleAuthorizationInterceptorTest`/teste de integração
      dedicado.
