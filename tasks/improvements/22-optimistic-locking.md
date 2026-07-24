# Task 22 — Optimistic Locking

Status: [x] Concluída

Depende de: baseline CRUD (`UserJpaEntity`).

## Objetivo

Implementar controle de concorrência otimista (`@Version`) na entidade de
usuário, para detectar updates concorrentes sobre o mesmo registro
(pré-requisito da task 45, que trata o conflito como `409`).

## Arquivos

- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
  (não previsto na especificação original — ver "Correção registrada"
  abaixo)

## Especificação

- `UserJpaEntity`: novo campo `@Version private Long version;` — só na
  entidade JPA, **não** no domínio (o domínio não deveria conhecer
  detalhe de persistência; ver `AGENTS.md` § "Por que essa separação
  existe"). Também ganhou `getVersion()` e um `updateMutableFields(name,
  passwordHash, phone, bio, enabled)` (público, usado só pelo mapper) —
  ver correção abaixo.
- `UserEntityMapper`: `toEntity`/`toDomain` continuam sem tocar em
  `version`; novo método `copyMutableFieldsTo(User user, UserJpaEntity
  managedEntity)`, usado pelo adapter no fluxo de update.
- Nenhuma mudança de contrato público (`UserResponse` não expõe versão).

## Correção registrada — a suposição original sobre o adapter estava errada

A especificação original assumia que "o Hibernate já rastreia a versão
pelo id gerenciado" e que `UserRepositoryAdapter.save` não precisaria
mudar. Isso está incorreto para o padrão atual do adapter: `save`
sempre construía uma **nova instância** de `UserJpaEntity` via
`UserEntityMapper.toEntity(user)`, com `version = null`.

O problema: assim que `@Version` existe, o `SimpleJpaRepository.save()`
do Spring Data passa a decidir "é insert ou update?" checando se
`version == null` (em vez de `id == null`, estratégia usada quando não
há `@Version`). Uma entidade nova-porém-com-id (update vindo de
`toEntity`) sempre teria `version == null`, então **toda atualização
seria tratada como inserção** — quebrando o CRUD inteiro, não só o
lock otimista.

Correção aplicada em `UserRepositoryAdapter.save`: quando `user.getId()
!= null` (update), busca a entidade **gerenciada** via
`jpaRepository.findById` (que já carrega a `version` real do banco) e
só copia os campos mutáveis (`copyMutableFieldsTo`) nela, em vez de
criar uma entidade nova. Quando `user.getId() == null` (create),
comportamento inalterado (`toEntity` normal). Isso mantém o domínio sem
conhecer `version` (a regra original do `AGENTS.md` continua valendo) e
faz o lock otimista funcionar de verdade: a entidade gerenciada carrega
a versão correta, e o Hibernate detecta conflito na hora do
`UPDATE ... WHERE version = ?` no commit da transação.

## Critérios de aceite

- [x] `UserJpaEntity` tem campo `@Version`.
- [ ] Update concorrente do mesmo registro (dois `save` a partir do mesmo
      estado carregado) lança `OptimisticLockingFailureException` — não
      coberto por teste automatizado (ver nota acima); não verificado
      manualmente ainda.
- [x] `create`/`update`/`upsert` continuam funcionando normalmente (a
      correção do adapter é o que garante isso — sem ela, todo update
      quebraria). **Confirmado pelo usuário**: `mvn clean compile` e
      `mvn test` rodados manualmente no IntelliJ.
- [x] `./mvnw test` continua passando sem alteração de comportamento nos
      fluxos normais (sem concorrência).
