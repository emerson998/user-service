# Task 48 — Health Check Endpoint

Status: [ ] Pendente

Depende de: nenhuma.

## Objetivo

Endpoint para monitorar o status da API e da conexão com o banco.

## Arquivos

- alterar: `pom.xml` (`spring-boot-starter-actuator`)
- alterar: `src/main/resources/application.properties`

## Especificação

- Adicionar `spring-boot-starter-actuator` — já traz
  `GET /actuator/health` com indicador de banco (`db`) automático via
  auto-configuração (detecta o `DataSource` configurado, incluindo o
  cenário de fallback da task 42, se já implementada).
- `application.properties`:
  ```properties
  management.endpoints.web.exposure.include=health
  management.endpoint.health.show-details=when-authorized
  ```
- Não expor outros endpoints do Actuator (`env`, `beans`, etc.) além de
  `health` — reduz superfície de exposição.

## Critérios de aceite

- [ ] `GET /actuator/health` devolve `200` com `{"status":"UP"}` quando o
      banco está acessível.
- [ ] Com o banco indisponível, o indicador `db` reporta `DOWN` (validar
      manualmente, derrubando a conexão).
