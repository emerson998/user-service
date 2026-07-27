# Task 49 — API Documentation

Status: [ ] Pendente

Depende de: Task 18–43 (cobre todos os endpoints novos deste lote).

## Objetivo

Garantir que a documentação interativa (OpenAPI/Swagger, já presente no
baseline via `springdoc-openapi-starter-webmvc-ui`) cobre todos os
endpoints introduzidos pelas tasks 18–43, não só o CRUD original.

## Arquivos

- alterar: todos os controllers criados neste lote (`UserController`
  — upsert, `UserCsvController`, `UserBulkNotificationController`,
  `AuthController`)

## Especificação

- Auditar cada método `@RequestMapping`/`@GetMapping`/etc. novo e
  garantir `@Operation(summary = "...")` (mesmo padrão já usado em
  `UserController`, seção 2.3 do `PLANO_IMPLEMENTACAO.md`).
- Agrupar por `@Tag`: `Users` (já existe), `Users CSV`, `Users Bulk
  Actions`, `Auth`.
- Endpoints que exigem token (`logout`, e qualquer endpoint com
  `@RequireRole`, task 39) devem documentar o header
  `Authorization: Bearer <token>` via `@Parameter` ou um esquema de
  segurança OpenAPI (`@SecurityScheme`/`@SecurityRequirement`) — não é
  autenticação real do Swagger UI, só documentação do contrato.
- Conferir manualmente `http://localhost:8080/swagger-ui/index.html`
  depois de subir a aplicação com todas as tasks 18–43 implementadas.

## Critérios de aceite

- [ ] Todo endpoint novo aparece no Swagger UI com resumo (`summary`) e
      agrupado na tag correta.
- [ ] `GET /v3/api-docs` responde `200` (sem `NoSuchMethodError` — ver
      `AGENTS.md` § Particularidades sobre versão do springdoc).
