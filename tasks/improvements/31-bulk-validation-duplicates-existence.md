# Task 31 — Validation — Duplicates & Existence

Status: [x] Concluída

Depende de: Task 29, 21 (padrão de checagem de existência reaproveitado).

## Objetivo

Validar que o array de ids não contém duplicados e que todos os usuários
informados existem no banco, antes de ativar a flag de notificação de
qualquer um deles.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/application/service/UserBulkNotificationService.java`

## Especificação

- `UserBulkNotificationService.activate(...)`, antes de processar:
  1. Duplicados: `new HashSet<>(userIds).size() != userIds.size()` →
     `IllegalArgumentException("userIds contém ids duplicados")` (mapear
     como `400`, ver task 44).
  2. Existência: para cada id, `userRepository.existsById(id)`; se
     algum não existir, agregar todos os ids ausentes numa
     `ResourceNotFoundException` só (mensagem lista os ids não
     encontrados) — **nenhum** usuário é ativado se houver qualquer id
     inexistente (tudo ou nada, não é o mesmo comportamento
     "continua sem parar" do import de CSV, task 27 — aqui a lista de
     ids é pequena e vem inteira num payload só, faz sentido ser
     atômico).
- Essas duas checagens rodam **antes** do loop de ativação da task 29.

## Critérios de aceite

- [x] `userIds` com duplicados → `400`, nenhuma ativação ocorre.
- [x] `userIds` com algum id inexistente → `404` (lista os ids não
      encontrados na mensagem), nenhuma ativação ocorre — nem para os ids
      válidos do mesmo payload.
- [x] `userIds` válido e sem duplicados segue o fluxo normal (task 29).
- [x] Coberto por `UserBulkNotificationServiceTest` (um caso por
      validação).
