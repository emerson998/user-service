# Tasks — Melhorias da API de Usuários (Project Requirements & Technical Checklist)

Checklist de execução, um arquivo por passo, no mesmo formato usado em
[`tasks/CRUD/README.md`](../CRUD/README.md). Numeração **contínua** a partir
da task 17 (última do lote CRUD) — task 18 é a primeira deste lote. Marque o
checkbox abaixo **e** o status dentro do arquivo da task conforme for
implementando, seguindo a convenção de camadas de `AGENTS.md` ("Convenções
ao adicionar uma nova feature/aggregate").

Escopo: os 7 épicos abaixo, derivados do checklist original de requisitos da
API. Pré-requisito: lote `tasks/CRUD` (tasks 00–17) concluído — todo o
domínio `User` já existe.

> ⚠️ Vários itens abaixo dependem de uma decisão de design **não coberta**
> pelo código atual (ex.: campo `CPF` não existe no `domain.model.User`,
> modelo de roles não existe, tecnologia de cache/DB secundário não
> escolhida). Ver [Decisões em aberto](#decisões-em-aberto) — **não decida
> silenciosamente**, resolva com o usuário antes de criar o arquivo da task.

## Checklist

### 1. User Management & Upsert

- [x] [18 — API Versioning](18-api-versioning.md) — expor as rotas com prefixo de versão (ex.: `/v1/users`).
- [x] [19 — Upsert Endpoint](19-upsert-endpoint.md) — endpoint que atualiza o usuário se existir, ou cria um novo se não existir.
- [x] [20 — Data Validation](20-data-validation.md) — validações obrigatórias de campo (ex.: `name`, `email`).
- [x] [21 — Uniqueness Checks](21-uniqueness-checks.md) — validar unicidade de e-mail e CPF antes de executar o upsert.
- [x] [22 — Optimistic Locking](22-optimistic-locking.md) — `@Version` na entidade de usuário.
- [x] [23 — Soft Delete](23-soft-delete.md) — remoção lógica via campo de status (ex.: `active`/`deletedAt`).

### 2. CSV Import & Export

- [x] [24 — CSV Export Endpoint](24-csv-export-endpoint.md) — endpoint para exportar usuários para CSV.
- [x] [25 — CSV Import Endpoint](25-csv-import-endpoint.md) — endpoint para importar usuários a partir de um CSV enviado.
- [x] [26 — File Validation](26-csv-file-validation.md) — validar formato e tamanho do CSV no import, rejeitando arquivos inválidos antes de processar.
- [x] [27 — Row-Level Error Handling](27-csv-row-level-error-handling.md) — tratar e reportar erros por linha no import, sem interromper o restante do processamento.

### 3. Notification Flag & Bulk Actions

- [x] [28 — Notification Flag](28-notification-flag.md) — flag booleana na entidade de usuário para habilitar/desabilitar notificações.
- [x] [29 — Bulk Flag Activation Endpoint](29-bulk-flag-activation-endpoint.md) — endpoint para ativar a flag em lote, recebendo um array de ids no corpo.
- [x] [30 — Validation — Empty Array](30-bulk-validation-empty-array.md) — validar que o array de ids não vem vazio.
- [x] [31 — Validation — Duplicates & Existence](31-bulk-validation-duplicates-existence.md) — validar ausência de ids duplicados e existência de todos os usuários informados.
- [x] [32 — Dry-Run / Skip Persist Logic](32-dry-run-skip-persist-logic.md) — se a flag de dry-run vier ativa no payload, validar sem persistir.

### 4. Authentication, Session & Access Control

- [x] [33 — Basic Login Endpoint](33-basic-login-endpoint.md) — login recebendo um id de usuário e devolvendo um token UUID válido.
- [ ] [34 — UUID Lookup Endpoint](34-uuid-lookup-endpoint.md) — endpoint para consultar status/detalhes de um token UUID ativo.
- [ ] [35 — Token Expiration](35-token-expiration.md) — TTL configurável para o token, `401` após expirar.
- [ ] [36 — Automated Token Invalidation](36-automated-token-invalidation.md) — job agendado (cron/scheduler) para expurgar tokens expirados.
- [ ] [37 — Logout Endpoint](37-logout-endpoint.md) — revoga/invalida o token ativo manualmente.
- [ ] [38 — Rate Limiting](38-rate-limiting.md) — rate limit no endpoint de login, para mitigar força bruta.
- [ ] [39 — Role-Based Access Control (RBAC)](39-rbac.md) — papéis e permissões básicas (ex.: `ADMIN` vs `USER`).

### 5. Data Retrieval, Caching & Resilience

- [ ] [40 — Pagination & Filtering](40-pagination-filtering.md) — paginação e filtros na listagem de usuários.
- [ ] [41 — Database Caching](41-database-caching.md) — camada de cache (ex.: Redis ou cache L2) para as consultas.
- [ ] [42 — Database Fallback](42-database-fallback.md) — conexão secundária de fallback caso a instância principal (PostgreSQL) fique indisponível.
- [ ] [43 — Service Layer Retry Logic](43-service-layer-retry-logic.md) — retry automático no service layer para erros transitórios de conexão com o banco.

### 6. Security, Compliance & Observability

- [ ] [44 — Standardized HTTP Status Codes](44-standardized-http-status-codes.md) — padronizar os status de resposta em todos os endpoints (`200`, `201`, `400`, `401`, `404`, `409`, `422`).
- [ ] [45 — Optimistic Lock Conflict Handling](45-optimistic-lock-conflict-handling.md) — capturar exceção de lock otimista e responder `409 Conflict`.
- [ ] [46 — Audit Logging](46-audit-logging.md) — log de auditoria capturando ator, timestamp, tipo de ação e dado modificado.
- [ ] [47 — LGPD / Data Privacy Compliance](47-lgpd-data-privacy-compliance.md) — mascarar/criptografar dado sensível (CPF, senha, dados pessoais) em logs e exports de CSV.
- [ ] [48 — Health Check Endpoint](48-health-check-endpoint.md) — endpoint para monitorar status da API e da conexão com o banco.
- [ ] [49 — API Documentation](49-api-documentation.md) — documentação interativa (OpenAPI/Swagger) cobrindo os endpoints novos.

### 7. Automated Testing

- [ ] [50 — Unit Tests](50-unit-tests.md) — cobrindo upsert de usuário, import/export de CSV e ativação da flag de notificação.
- [ ] [51 — Integration Tests](51-integration-tests.md) — cobrindo login, emissão de token UUID, expiração e endpoints protegidos.

Status geral: 16/34 concluídas.

## Ordem e dependências

| # | Task | Épico | Depende de |
|---|------|-------|------------|
| 18 | API Versioning | 1 | baseline CRUD (00–17) |
| 19 | Upsert Endpoint | 1 | 18 |
| 20 | Data Validation | 1 | 19 |
| 21 | Uniqueness Checks (email + CPF) | 1 | 19, 20 |
| 22 | Optimistic Locking (`@Version`) | 1 | baseline CRUD |
| 23 | Soft Delete | 1 | baseline CRUD |
| 24 | CSV Export Endpoint | 2 | 23 |
| 25 | CSV Import Endpoint | 2 | 19, 20, 21 |
| 26 | CSV File Validation | 2 | 25 |
| 27 | CSV Row-Level Error Handling | 2 | 25, 26 |
| 28 | Notification Flag | 3 | baseline CRUD |
| 29 | Bulk Flag Activation Endpoint | 3 | 18, 28 |
| 30 | Validation — Empty Array | 3 | 29 |
| 31 | Validation — Duplicates & Existence | 3 | 29, 21 |
| 32 | Dry-Run / Skip Persist Logic | 3 | 29, 30, 31 |
| 33 | Basic Login Endpoint | 4 | 18, baseline CRUD |
| 34 | UUID Lookup Endpoint | 4 | 33 |
| 35 | Token Expiration | 4 | 33 |
| 36 | Automated Token Invalidation | 4 | 35 |
| 37 | Logout Endpoint | 4 | 33, 35 |
| 38 | Rate Limiting | 4 | 33 |
| 39 | RBAC | 4 | 33, 37 |
| 40 | Pagination & Filtering | 5 | 23 |
| 41 | Database Caching | 5 | baseline CRUD |
| 42 | Database Fallback | 5 | baseline CRUD |
| 43 | Service Layer Retry Logic | 5 | 42 |
| 44 | Standardized HTTP Status Codes | 6 | baseline CRUD (`GlobalExceptionHandler`) |
| 45 | Optimistic Lock Conflict Handling | 6 | 22, 44 |
| 46 | Audit Logging | 6 | 44 |
| 47 | LGPD / Data Privacy Compliance | 6 | 24, 46 |
| 48 | Health Check Endpoint | 6 | - |
| 49 | API Documentation | 6 | 18–43 |
| 50 | Unit Tests | 7 | 19, 25, 29 |
| 51 | Integration Tests | 7 | 33, 34, 35, 36, 37, 38, 39 |

## Decisões em aberto

Resolver com o usuário **antes** de criar o arquivo da task correspondente
(mesma regra de `AGENTS.md`/`tasks/CRUD/README.md` — não decidir
silenciosamente):

- **21/47 — Campo CPF**: `domain.model.User` (seção 2.1 do
  `PLANO_IMPLEMENTACAO.md`) não tem campo `CPF` hoje. Precisa ser adicionado
  (formato, validação, unicidade) antes da task 21. -> Pode adicionar o campo de cpf para o usuário com @CPF para validar
- **39 — Modelo de roles**: não existe autenticação/autorização no projeto
  atual. Definir onde o papel (`ADMIN`/`USER`) fica armazenado (no próprio
  `User`? entidade separada?) antes da task 39. -> definir a role no próprio user com ENUM 
- **41 — Tecnologia de cache**: Redis vs. cache L2 (ex. Caffeine/Ehcache via
  Spring Cache) — escolher antes da task 41. -> utilizar Spring cache inicialmente
- **42 — Banco de fallback**: qual banco secundário (outra instância
  PostgreSQL? H2 local?) e a estratégia de failover — escolher antes da
  task 42. -> O banco principal vai ser o postgres e o secundário vai ser o H2 em memória, para testes de fallback
- **33/35 — Persistência do token**: onde o token UUID e seu TTL ficam
  armazenados (tabela própria? cache/Redis, já unificando com a task 41?) —
  decidir antes da task 33. -> o token pode ficar no cache do spring, com TTL configurável

## Convenção de uso

- Ao quebrar este checklist em arquivos de task, siga o template usado em
  `tasks/CRUD/*.md` (Status / Depende de / Objetivo / Arquivos /
  Especificação / Critérios de aceite) e ligue o link do item acima ao
  arquivo criado.
- Antes de começar uma task, marque-a como `[~] Em andamento` no cabeçalho
  do arquivo dela.
- Ao terminar, marque `[x] Concluída` e deixe avisado para o usuário que deve revisar a compilação e 
- rodar os testes antes de seguir para a próxima task.
- Se uma task revelar uma decisão de design não coberta pela seçãox
  [Decisões em aberto](#decisões-em-aberto) ou pelo `PLANO_IMPLEMENTACAO.md`,
  pare e resolva com o usuário antes de codificar.
