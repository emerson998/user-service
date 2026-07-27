# Task 27 — Row-Level Error Handling

Status: [x] Concluída

Depende de: Task 25, 26.

## Objetivo

Tratar e reportar erros específicos de linha durante o import de CSV, sem
interromper o processamento das linhas restantes — complementa a task 26
(que rejeita o arquivo inteiro só em casos estruturais).

## Arquivos

- criar: `src/main/java/com/solutis/dev/application/dto/csv/CsvRowError.java`
- alterar: `src/main/java/com/solutis/dev/application/dto/csv/CsvImportResult.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserCsvService.java`

## Especificação

- `CsvRowError` (record): `int rowNumber`, `String rawLine`,
  `String message`.
- `UserCsvService.importFromCsv`: envolver a chamada de
  `userUseCase.upsert(...)` por linha num `try/catch` — capturar
  `DuplicateResourceException`, `ConstraintViolationException`/erro de
  `@Valid` equivalente, e qualquer `RuntimeException` inesperada da
  linha; em caso de falha, adicionar um `CsvRowError` a
  `CsvImportResult.errors()` e **continuar para a próxima linha** (não
  propagar a exceção).
- `CsvImportResult.errorCount()` passa a refletir `errors.size()`.

## Nota de implementação

Todo o código de produção desta task (`CsvRowError`, `CsvImportResult`,
`try/catch` por linha em `UserCsvService.importFromCsv`) já tinha sido
implementado na task 25 (documentado lá na época). Esta task foi só o
teste dedicado ao cenário exato dos critérios de aceite.

## Critérios de aceite

- [x] Um CSV com 10 linhas, onde 2 têm e-mail duplicado, processa as 8
      linhas válidas e devolve `errorCount = 2` com o número da linha e a
      mensagem de cada erro.
- [x] Nenhuma exceção de linha individual interrompe o processamento das
      linhas seguintes.
- [x] Coberto por `UserCsvServiceTest` (arquivo misto válido/inválido).
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.
