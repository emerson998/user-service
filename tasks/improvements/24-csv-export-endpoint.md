# Task 24 — CSV Export Endpoint

Status: [ ] Pendente

Depende de: Task 18, 23.

## Objetivo

Endpoint para exportar os usuários (ativos, respeitando o soft delete da
task 23) para um arquivo CSV.

## Arquivos

- alterar: `pom.xml` (nova dependência `com.opencsv:opencsv`)
- criar: `src/main/java/com/solutis/dev/application/port/in/UserCsvUseCase.java`
- criar: `src/main/java/com/solutis/dev/application/service/UserCsvService.java`
- criar: `src/main/java/com/solutis/dev/web/controller/UserCsvController.java`

## Especificação

- Dependência: `opencsv` (versão gerenciada manualmente — não faz parte do
  BOM do `spring-boot-starter-parent`, declarar `<version>` explícita,
  ex. `5.9`) evita reimplementar parsing/escaping de CSV na mão (relevante
  já para a task 25/26, que lidam com quoting/linhas malformadas).
- `UserCsvUseCase`: `byte[] exportToCsv();`
- `UserCsvService.exportToCsv()`:
  1. `userRepository.findAll()` (já filtra soft-deleted, task 23).
  2. Serializa com `CSVWriter` (opencsv): cabeçalho
     `id,name,email,cpf,phone,bio,enabled` (não inclui `passwordHash` —
     mesma regra do `UserResponse`, nunca expor senha).
  3. Retorna os bytes (`UTF-8`).
- `UserCsvController`:
  - `GET ApiRoutes.V1 + "/users/csv/export"`.
  - Resposta `200`, `Content-Type: text/csv`,
    `Content-Disposition: attachment; filename="users.csv"`.

## Critérios de aceite

- [ ] `GET /api/v1/users/csv/export` devolve um CSV com cabeçalho e uma
      linha por usuário ativo.
- [ ] Usuário soft-deleted (task 23) não aparece no export.
- [ ] Nenhuma coluna de senha/hash no CSV.
