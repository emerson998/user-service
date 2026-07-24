# Task 41 — Database Caching

Status: [ ] Pendente

Depende de: baseline CRUD (`UserRepositoryAdapter`).

## Objetivo

Camada de cache para as consultas do repositório de usuário. Decisão
registrada (ver `README.md` § Decisões em aberto): usar Spring Cache
inicialmente (não Redis) — mesma infraestrutura (Caffeine) já introduzida
pela task 33 para os tokens de login.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/infrastructure/config/CacheConfig.java`
  (novo cache nomeado, além de `authTokens`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/adapter/UserRepositoryAdapter.java`

## Especificação

- `CacheConfig`: adicionar o cache `users` ao `CaffeineCacheManager` (TTL
  configurável, ex. `app.cache.users-ttl-seconds`, separado do TTL de
  token da task 35).
- `UserRepositoryAdapter`:
  - `@Cacheable(cacheNames = "users", key = "#id")` em `findById`.
  - `@CachePut(cacheNames = "users", key = "#result.id")` em `save` —
    mantém o cache coerente após create/update.
  - `@CacheEvict(cacheNames = "users", key = "#id")` em `deleteById` e no
    caminho de soft delete (task 23, que passa por `save`, já coberto
    pelo `@CachePut` acima — como o soft delete não remove a linha,
    `@CacheEvict` explícito só é necessário se um delete físico
    permanecer em algum fluxo).
  - `findAll`/`findAll(PageQuery, UserFilter)` (task 40) **não** são
    cacheados — resultado depende de parâmetros compostos e muda com
    frequência; cachear só a consulta por id, que é o caso de uso mais
    repetitivo (`GET /{id}`).

## Critérios de aceite

- [ ] Duas chamadas seguidas de `GET /api/v1/users/{id}` para o mesmo id
      só geram uma consulta ao banco (verificável via
      `spring.jpa.show-sql=true` num teste manual, ou mockando o
      `UserJpaRepository` num teste de cache dedicado).
- [ ] `PUT`/`upsert`/`DELETE` sobre um id atualizam o cache
      correspondente — uma leitura seguinte não devolve dado desatualizado.
- [ ] Coberto por um teste de cache dedicado (`@SpringBootTest` com
      `CacheManager` injetado, confirmando hit/miss).
