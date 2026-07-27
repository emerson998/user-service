# Task 51 — Integration Tests

Status: [ ] Pendente

Depende de: Task 33, 34, 35, 36, 37, 38, 39.

## Objetivo

Testes de integração ponta a ponta cobrindo login, emissão de token UUID,
expiração e endpoints protegidos.

## Arquivos

- criar: `src/test/java/com/solutis/dev/AuthFlowIntegrationTest.java`

## Especificação

Segue o padrão de `AGENTS.md` § Testes ("Integração end-to-end"):
`@SpringBootTest` + `@AutoConfigureMockMvc`, contexto real com H2 em
memória. Usar com moderação (só o fluxo principal, não todo caso de
borda — esses já estão nos testes unitários da task 50).

Cenário único, exercitando a sequência completa:
1. `POST /api/v1/auth/login` com um usuário existente → `200` + token.
2. `GET /api/v1/auth/tokens/{token}` → `valid: true`.
3. Endpoint protegido por `@RequireRole` (task 39) com o token → `200`
   (ou `403`, se o papel do usuário de teste não for o exigido —
   validar os dois casos).
4. Reduzir o TTL via `@TestPropertySource` (`app.auth.token-ttl-seconds=1`),
   aguardar a expiração (`Thread.sleep`/`Awaitility`), repetir o passo 3
   → `401`.
5. Novo login, depois `POST /api/v1/auth/logout` → `200`; repetir
   `GET /api/v1/auth/tokens/{token}` → `valid: false`.

## Critérios de aceite

- [ ] Sequência completa (login → lookup → endpoint protegido →
      expiração → logout) passa em um único teste de integração.
- [ ] `./mvnw test` continua rodando em tempo aceitável — se o passo 4
      (esperar expirar) deixar a suíte lenta, usar
      `Awaitility.await().atMost(...)` em vez de `Thread.sleep` fixo.
