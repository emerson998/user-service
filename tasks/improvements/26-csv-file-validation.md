# Task 26 — File Validation

Status: [x] Concluída

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

## Nota de implementação

`UserCsvService` ganhou um terceiro parâmetro de construtor
(`maxFileSizeBytes`, via `@Value`). Como o Mockito `@InjectMocks` não
sabe resolver um `long` vindo de `@Value` (usaria `0` por padrão, o que
quebraria todo teste de arquivo válido), `UserCsvServiceTest` trocou
`@InjectMocks` por construção explícita em `@BeforeEach`.

## Critérios de aceite

- [x] Arquivo vazio, extensão inválida, acima do tamanho máximo, ou com
      cabeçalho incorreto → `422`, nenhuma linha é processada.
- [x] Arquivo válido segue o fluxo normal da task 25.
- [x] Coberto por `UserCsvServiceTest` (um caso por validação) e
      `UserCsvControllerTest` para o mapeamento HTTP (não há
      `GlobalExceptionHandlerTest` dedicado ainda — mapeamento verificado
      via o controller). **Confirmado pelo usuário**: `mvn clean compile`
      e `mvn test` rodados manualmente no IntelliJ.
