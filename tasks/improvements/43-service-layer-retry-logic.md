# Task 43 — Service Layer Retry Logic

Status: [ ] Pendente

Depende de: Task 42.

## Objetivo

Retry automático no service layer para erros transitórios de conexão com
o banco (ex.: timeout momentâneo antes de cair para o fallback da task
42).

## Arquivos

- alterar: `pom.xml` (`org.springframework.retry:spring-retry`,
  `org.springframework:spring-aspects` — `@Retryable` depende de AOP)
- alterar: `src/main/java/com/solutis/dev/UsuariosServiceApplication.java`
  (`@EnableRetry`)
- alterar: `src/main/java/com/solutis/dev/application/service/UserService.java`

## Especificação

- `@Retryable(retryFor = { TransientDataAccessException.class },
  maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))` nos
  métodos de `UserService` que tocam o repositório
  (`create`/`update`/`upsert`/`getById`/`listAll`/`delete`) — só captura
  exceções transitórias do Spring (`TransientDataAccessException` e
  subclasses), nunca erros de regra de negócio
  (`DuplicateResourceException`/`ResourceNotFoundException`, que não
  devem ser re-tentados).
- Método `@Recover` correspondente, para logar e relançar a exceção
  depois de esgotadas as tentativas (evita mascarar a falha real do
  chamador).

## Critérios de aceite

- [ ] Uma falha transitória simulada (ex.: mock do `UserRepository`
      lançando `TransientDataAccessException` nas duas primeiras
      chamadas e sucesso na terceira) é absorvida pelo retry — o
      chamador não vê exceção.
- [ ] Esgotadas as tentativas, a exceção original é relançada (não
      silenciada).
- [ ] `DuplicateResourceException`/`ResourceNotFoundException` nunca são
      re-tentadas.
- [ ] Coberto por `UserServiceTest` com `UserRepository` mockado.
