# Task 50 — Unit Tests

Status: [ ] Pendente

Depende de: Task 19, 25, 29.

## Objetivo

Testes unitários cobrindo upsert de usuário, import/export de CSV e
ativação da flag de notificação — fecha a cobertura que cada task
individual já pedia pontualmente, garantindo que nada ficou para trás.

## Arquivos

- criar/completar: `src/test/java/com/solutis/dev/application/service/UserServiceTest.java`
  (casos de `upsert`, task 19)
- criar: `src/test/java/com/solutis/dev/application/service/UserCsvServiceTest.java`
  (tasks 24–27)
- criar: `src/test/java/com/solutis/dev/application/service/UserBulkNotificationServiceTest.java`
  (tasks 28–32)

## Especificação

Segue o padrão de `AGENTS.md` § Testes ("Application"): JUnit 5 + Mockito,
mockando `UserRepository`/`PasswordEncoderPort`/`TokenStorePort` conforme
o caso, sem subir contexto Spring.

- `UserServiceTest.upsert`: branch de criação (e-mail não existe, senha
  obrigatória), branch de atualização (e-mail existe, com e sem troca de
  senha), branch de erro (criação sem senha).
- `UserCsvServiceTest`: export gera CSV com o cabeçalho esperado;
  import cria e atualiza na mesma chamada (mistura de linhas
  create/update); arquivo com linha inválida não interrompe as demais
  (task 27); arquivo estruturalmente inválido é rejeitado inteiro (task
  26).
- `UserBulkNotificationServiceTest`: ativação simples; array vazio
  (`400`); duplicados (`400`); id inexistente (`404`, nada é persistido);
  `dryRun = true` não persiste.

## Critérios de aceite

- [ ] `./mvnw test` passa com os três arquivos de teste acima
      implementados.
- [ ] Nenhum teste desta task sobe contexto Spring (`@SpringBootTest`) —
      são todos testes de unidade puros com Mockito.
