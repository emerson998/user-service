# Task 36 — Automated Token Invalidation

Status: [x] Concluída

Depende de: Task 35.

## Objetivo

Job agendado para expurgar tokens expirados. O Caffeine (task 33/35) já
expira entradas de forma preguiçosa (só remove de fato no próximo
acesso/escrita àquela entrada) — esta task força uma limpeza periódica
proativa, conforme pedido explicitamente no checklist original.

## Arquivos

- alterar: `src/main/java/com/solutis/dev/UsuariosServiceApplication.java`
  (`@EnableScheduling`) — ou criar `infrastructure.config.SchedulingConfig`
  dedicado, se preferir não anotar a classe principal.
- criar: `src/main/java/com/solutis/dev/infrastructure/security/TokenCleanupJob.java`

## Especificação

- `TokenCleanupJob`: `@Component`, injeta `CacheManager`; método
  `@Scheduled(fixedRateString = "${app.auth.token-cleanup-interval-ms:60000}")`
  que obtém o cache nativo Caffeine
  (`((CaffeineCache) cacheManager.getCache("authTokens")).getNativeCache().cleanUp()`)
  — força a remoção imediata das entradas já expiradas em vez de esperar
  o próximo acesso.
- `application.properties`: `app.auth.token-cleanup-interval-ms=60000`
  (1 min, default sugerido).

## Critérios de aceite

- [x] Job roda periodicamente sem lançar exceção (log de erro, se
      falhar, sem derrubar a aplicação).
- [x] Após o TTL (task 35) + um ciclo do job, o token não aparece mais no
      cache nativo (verificável via teste com `CacheManager` injetado e
      `Thread.sleep`/`Awaitility`, ou invocando o método do job
      diretamente no teste).
      Coberto por `TokenCleanupJobTest` (cache real com TTL de 100ms +
      `cleanupExpiredTokens()` chamado diretamente, cache ausente, e
      `CacheManager` lançando exceção).
