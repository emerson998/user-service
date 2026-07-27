# Task 42 — Database Fallback

Status: [x] Concluída

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

## Nota de implementação

- `FailoverDataSource` foi implementado como classe estática aninhada em
  `DataSourceConfig` (em vez de um arquivo próprio) — estende
  `org.springframework.jdbc.datasource.DelegatingDataSource` (já
  transitivo via `spring-boot-starter-data-jpa` → `spring-jdbc`), que
  implementa todos os métodos administrativos de `DataSource` delegando
  ao primário; só `getConnection()`/`getConnection(user, pass)` são
  sobrescritos com o `try/catch` de fallback.
- `fallbackDataSource` lê as mesmas chaves `spring.datasource.*` já
  usadas pelo H2 do perfil padrão em `application.properties` (via
  `@Value`), em vez de duplicar a URL/usuário/senha — exatamente a
  "mesma configuração já usada" pedida na especificação.
- `DataSourceConfig` é `@Profile({"postgres", "prod"})` — no perfil
  padrão (dev/test) o autoconfigure normal do Spring Boot continua
  criando o `DataSource` H2 de sempre, sem nenhuma mudança de
  comportamento no baseline do CRUD.

## Critérios de aceite

- [x] Com o Postgres indisponível (URL inválida/timeout), uma operação
      de leitura/escrita cai para o H2 sem lançar exceção para o
      cliente.
- [x] Com o Postgres disponível, todas as operações usam o
      `primaryDataSource` normalmente (H2 nunca é tocado).
- [x] Coberto por um teste dedicado (`DataSourceConfigTest` ou
      equivalente) simulando a falha do primário — `DataSourceConfigTest`
      mocka `DataSource`/`Connection` e testa os 4 caminhos (primário ok/
      falha, com e sem credenciais), sem depender de um Postgres real.
