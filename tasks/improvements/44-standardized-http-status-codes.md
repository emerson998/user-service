# Task 44 — Standardized HTTP Status Codes

Status: [ ] Pendente

Depende de: baseline CRUD (`GlobalExceptionHandler`).

## Objetivo

Padronizar os status de resposta em todos os endpoints do serviço
(`200`, `201`, `400`, `401`, `404`, `409`, `422`), auditando o que já
existe e fechando lacunas introduzidas pelas tasks 18–43.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`
- criar: `src/main/java/com/solutis/dev/domain/exception/BusinessRuleException.java`

## Especificação

Tabela de referência (já parcialmente coberta pelo `AGENTS.md` §
"Tratamento de erros" — esta task garante que todo endpoint novo segue a
mesma tabela):

| Situação | Status |
|---|---|
| Criação bem-sucedida (`POST`) | `201` |
| Leitura/atualização/ação bem-sucedida | `200` |
| Bean Validation (`MethodArgumentNotValidException`) | `400` — já existe |
| `IllegalArgumentException` de regra de entrada (ex.: tasks 19, 31) | `400` |
| Token ausente/expirado/inválido (tasks 35, 37) | `401` — já existe (task 35) |
| `ResourceNotFoundException` | `404` — já existe |
| `DuplicateResourceException` | `409` — já existe |
| `OptimisticLockingFailureException` (task 45) | `409` |
| `AccessDeniedException` (task 39) | `403` |
| `RateLimitExceededException` (task 38) | `429` |
| `InvalidFileException` (task 26) | `422` |
| `BusinessRuleException` (nova — regra de negócio não coberta pelas
  anteriores) | `422` |

- `BusinessRuleException extends RuntimeException`: classe base para
  futuras regras de negócio que não sejam nem "não encontrado" nem
  "duplicado" — mapeada para `422`.
- Adicionar `@ExceptionHandler(IllegalArgumentException.class)` → `400`
  no `GlobalExceptionHandler`, já que várias tasks anteriores (19, 31)
  assumiram esse mapeamento.
- Auditar `UserController`: `create` já devolve `201` (baseline); demais
  ações (`update`, `getById`, `listAll`, `upsert`) devolvem `200`.

## Critérios de aceite

- [ ] Todo endpoint criado pelas tasks 18–43 tem seu status de sucesso e
      de erro conferido contra a tabela acima.
- [ ] `IllegalArgumentException` não mapeado antes desta task agora
      devolve `400` em vez de cair no handler genérico (`500`).
- [ ] Coberto por `GlobalExceptionHandlerTest` (um caso por exceção
      mapeada).
