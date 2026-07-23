# Task 03 — Exceções de domínio

Status: [ ] Não iniciada

## Objetivo

Exceções genéricas e reutilizáveis (não só para `User`) que o
`GlobalExceptionHandler` (task 15) mapeia para status HTTP, conforme a
tabela de `AGENTS.md` § "Tratamento de erros".

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/domain/exception/ResourceNotFoundException.java`
- criar: `src/main/java/com/emerson/dev/usuarios/domain/exception/DuplicateResourceException.java`

## Especificação

Ambas `extends RuntimeException`, com pelo menos um construtor
`(String message)`. Considerar um construtor de conveniência
`(String resource, Object id)` que monta a mensagem
(`"User com id 42 não encontrado"`), já que provavelmente serão
reaproveitadas por outros agregados no futuro — mas não criar hierarquia
além disso (sem `BusinessRuleException` ainda, não é necessária para o
CRUD de `User`; a tabela de `AGENTS.md` menciona subclasses de
`BusinessRuleException` como algo genérico do template, não um requisito
desta feature).

## Critérios de aceite

- [ ] `ResourceNotFoundException` → mapeada para 404 na task 15.
- [ ] `DuplicateResourceException` → mapeada para 409 na task 15.
- [ ] Usadas por `UserService` (task 08): not-found em `getById`/`update`/`delete`,
      duplicate em `create`/`update` (e-mail já existe).
