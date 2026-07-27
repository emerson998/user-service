# Task 47 — LGPD / Data Privacy Compliance

Status: [ ] Pendente

Depende de: Task 24, 46.

## Objetivo

Mascarar/criptografar dado sensível (CPF, senha, dados pessoais) em logs
e exports de CSV.

## Arquivos

- criar: `src/main/java/com/solutis/dev/infrastructure/audit/DataMasker.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/audit/Slf4jAuditLogAdapter.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserCsvService.java`
- alterar: `src/main/resources/application.properties`

## Especificação

- `DataMasker`: utilitário estático (`final class`, construtor privado),
  `maskCpf("12345678900") → "123.***.**-00"` (mantém só os 3 primeiros e
  os 2 últimos dígitos), `maskEmail("nome@dominio.com") →
  "n***@dominio.com"`.
- `Slf4jAuditLogAdapter` (task 46): antes de logar o `AuditEvent`, aplica
  `DataMasker` em qualquer campo de `entityData` reconhecido como
  CPF/e-mail/senha — **nunca** logar `passwordHash` em texto pleno,
  mesmo com hash (mascarar como `"***"` fixo).
- `UserCsvService.exportToCsv` (task 24): coluna `cpf` do CSV passa a sair
  mascarada por padrão; expor uma flag
  `app.csv.export-unmasked-cpf=false` (default `false`) para os poucos
  casos em que o CPF completo é necessário — **nunca** default `true`.
- Dado sensível nunca é criptografado em repouso nesta task (isso seria
  uma mudança de schema/coluna, fora do escopo do checklist original,
  que só pede mascarar/criptografar em **logs e exports**) — confirmar
  com o usuário se criptografia em banco também é esperada antes de
  expandir o escopo.

## Critérios de aceite

- [ ] Log de auditoria (task 46) nunca contém CPF completo nem
      `passwordHash` em texto pleno.
- [ ] Export de CSV (task 24) sai com CPF mascarado por padrão.
- [ ] Coberto por `DataMaskerTest` e um caso em `UserCsvServiceTest`.
