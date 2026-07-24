# Task 25 — CSV Import Endpoint

Status: [ ] Pendente

Depende de: Task 19, 20, 21, 24.

## Objetivo

Endpoint para importar usuários a partir de um CSV enviado (upload),
reaproveitando a lógica de upsert (task 19) e as validações de
obrigatoriedade/unicidade (tasks 20, 21) — cada linha do CSV vira uma
chamada a `UserUseCase.upsert(...)`.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/application/port/in/UserCsvUseCase.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserCsvService.java`
- alterar: `src/main/java/com/solutis/dev/web/controller/UserCsvController.java`
- criar: `src/main/java/com/solutis/dev/application/dto/csv/CsvImportResult.java`

## Especificação

- Formato de entrada esperado: mesmo cabeçalho do export (task 24):
  `name,email,cpf,phone,bio,password` (`id`/`enabled` ignorados no
  import — upsert decide se cria ou atualiza pela combinação
  e-mail/CPF).
- `CsvImportResult` (record): `int successCount`, `int errorCount`,
  `List<CsvRowError> errors` (detalhamento de `CsvRowError` fica a cargo
  da task 27, que trata erro por linha — nesta task, a lista pode ficar
  vazia/placeholder se nenhuma linha falhar).
- `UserCsvUseCase.importFromCsv(MultipartFile file): CsvImportResult`
  novo método na porta.
- `UserCsvService.importFromCsv(...)`:
  1. Lê o CSV com `CSVReader` (opencsv, mesma dependência da task 24).
  2. Para cada linha, monta um `UserUpsertRequest` e chama
     `userUseCase.upsert(request)`.
  3. Acumula sucesso/erro por linha (base para a task 27).
- `UserCsvController`: `POST ApiRoutes.V1 + "/users/csv/import"`,
  `consumes = MULTIPART_FORM_DATA_VALUE`, parâmetro
  `@RequestParam("file") MultipartFile file`. Resposta `200` com
  `CsvImportResult` no corpo (mesmo se houver erros parciais — só a task
  26 rejeita o arquivo inteiro antes de processar).

## Critérios de aceite

- [ ] `POST /api/v1/users/csv/import` com um CSV válido cria/atualiza os
      usuários correspondentes (mesma semântica do upsert).
- [ ] Resposta inclui `successCount` condizente com o número de linhas
      processadas com sucesso.
- [ ] Coberto por teste em `UserCsvServiceTest` (linhas de create e de
      update misturadas no mesmo arquivo).
