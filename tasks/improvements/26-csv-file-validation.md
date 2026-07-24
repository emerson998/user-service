# Task 26 — File Validation

Status: [ ] Pendente

Depende de: Task 25.

## Objetivo

Validar formato e tamanho do arquivo CSV **antes** de processar qualquer
linha — rejeitar o arquivo inteiro se ele não for um CSV válido ou
exceder o tamanho máximo permitido, sem chegar a chamar `upsert` em
nenhuma linha.

## Arquivos

- criar: `src/main/java/com/solutis/dev/domain/exception/InvalidFileException.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserCsvService.java`
- alterar: `src/main/java/com/solutis/dev/web/exception/GlobalExceptionHandler.java`
- alterar: `src/main/resources/application.properties`

## Especificação

- `InvalidFileException extends RuntimeException` (segue o padrão de
  `domain.exception` já usado por `ResourceNotFoundException`/
  `DuplicateResourceException`).
- Validações, na entrada de `UserCsvService.importFromCsv`, antes de abrir
  o `CSVReader`:
  - arquivo vazio (`file.isEmpty()`) → `InvalidFileException`.
  - extensão/`content-type` diferente de `text/csv` ou `.csv` →
    `InvalidFileException`.
  - tamanho acima do limite configurável
    (`app.csv.max-file-size-bytes`, `application.properties`) →
    `InvalidFileException`.
  - cabeçalho não bate com o esperado (`name,email,cpf,phone,bio,password`,
    task 25) → `InvalidFileException`.
- `GlobalExceptionHandler`: novo `@ExceptionHandler(InvalidFileException.class)`
  → `422` (`HttpStatus.valueOf(422)`, já que `UNPROCESSABLE_ENTITY` está
  deprecated nesta versão do Spring — ver `AGENTS.md` § Particularidades).
- `application.properties`: `app.csv.max-file-size-bytes=5242880` (5MB,
  valor default sugerido — ajustar se o usuário definir outro limite).

## Critérios de aceite

- [ ] Arquivo vazio, extensão inválida, acima do tamanho máximo, ou com
      cabeçalho incorreto → `422`, nenhuma linha é processada.
- [ ] Arquivo válido segue o fluxo normal da task 25.
- [ ] Coberto por `UserCsvServiceTest` (um caso por validação) e
      `GlobalExceptionHandlerTest`/`UserCsvControllerTest` para o mapeamento
      HTTP.
