# Task 45 — Optimistic Lock Conflict Handling

Status: [ ] Pendente

Depende de: Task 22, 44.

## Objetivo

Capturar explicitamente a exceção de lock otimista (task 22) e responder
`409 Conflict`, em vez de deixá-la cair no handler genérico (`500`).

## Arquivos

- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`

## Especificação

- Novo `@ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)`
  → `build(HttpStatus.CONFLICT, "Registro foi modificado por outra
  requisição, tente novamente", request)` — mesma assinatura dos demais
  handlers já existentes (`build(...)`).
- Este handler fica **antes** do handler genérico
  (`@ExceptionHandler(Exception.class)`) na ordem de declaração — Spring
  resolve pelo tipo mais específico independente da ordem no arquivo,
  mas manter a convenção de organização já usada na classe (específicos
  primeiro, genérico por último).

## Critérios de aceite

- [ ] Update concorrente do mesmo usuário (dois `save` a partir do mesmo
      estado carregado, task 22) → `409`, não `500`.
- [ ] Corpo da resposta segue o mesmo formato `ErrorResponse` dos demais
      erros mapeados.
- [ ] Coberto por `GlobalExceptionHandlerTest`.
