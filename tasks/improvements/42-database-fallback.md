# Task 42 — Database Fallback

Status: [ ] Pendente

Depende de: baseline CRUD (`UserRepositoryAdapter`, `UserJpaRepository`).

## Objetivo

Mecanismo de conexão secundária de fallback caso a instância principal
fique indisponível. Decisão registrada (ver `README.md` § Decisões em
aberto): banco principal PostgreSQL, banco secundário H2 em memória
(usado só para os testes de fallback, não como banco de produção real).

## Arquivos

- alterar: `pom.xml` (`org.postgresql:postgresql`, `runtime`)
- criar: `src/main/resources/application-postgres.properties` (perfil com
  o datasource principal)
- criar: `src/main/java/com/solutis/dev/infrastructure/config/DataSourceConfig.java`
- alterar: `src/main/resources/application.properties`

## Especificação

- `DataSourceConfig`: dois beans `DataSource` qualificados
  (`@Qualifier("primaryDataSource")` → Postgres,
  `@Qualifier("fallbackDataSource")` → H2 em memória, mesma configuração
  já usada em `application.properties`, seção "H2 in-memory database").
- Estratégia de failover: `DataSource` combinado que tenta abrir conexão
  no `primaryDataSource`; se falhar (`SQLException` de conexão), usa o
  `fallbackDataSource` — implementar como um `DataSource` decorator
  simples (delegando `getConnection()` com `try/catch` e fallback), em
  vez de `AbstractRoutingDataSource` (que roteia por contexto, não por
  falha de conexão).
- `application-postgres.properties`: `spring.datasource.url`,
  `username`, `password` do Postgres (via variável de ambiente, nunca
  hardcoded — ex. `${POSTGRES_URL}`, `${POSTGRES_USER}`,
  `${POSTGRES_PASSWORD}`).
- Perfil padrão (`application.properties`) continua no H2 em memória para
  dev/test, sem tocar no baseline do CRUD — o datasource combinado só é
  ativado no perfil `postgres` (ou `prod`).

## Nota de implementação

Diferente do resto do lote, esta task depende de infraestrutura externa
real (uma instância Postgres acessível) para ser testada de ponta a
ponta — o teste automatizado deve simular a falha de conexão (ex.:
apontar `primaryDataSource` para uma URL inválida) em vez de depender de
derrubar um Postgres de verdade.

## Critérios de aceite

- [ ] Com o Postgres indisponível (URL inválida/timeout), uma operação
      de leitura/escrita cai para o H2 sem lançar exceção para o
      cliente.
- [ ] Com o Postgres disponível, todas as operações usam o
      `primaryDataSource` normalmente (H2 nunca é tocado).
- [ ] Coberto por um teste dedicado (`DataSourceConfigTest` ou
      equivalente) simulando a falha do primário.
