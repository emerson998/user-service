# Task 15 — web.exception.GlobalExceptionHandler

Status: [ ] Não iniciada

Depende de: Task 03.

## Objetivo

Ainda não existe nenhum handler global no projeto — esta task cria o
handler do zero (o texto do `PLANO_IMPLEMENTACAO.md` § 2.2 fala em
"reaproveitar", mas não há nada para reaproveitar ainda). A notificação
via `ErrorNotifierPort` (seção 3.8 do plano) **não** entra aqui — é um
lote de tasks futuro, do chaos.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/web/exception/ErrorResponse.java`
- criar: `src/main/java/com/emerson/dev/usuarios/web/exception/GlobalExceptionHandler.java`

## `ErrorResponse` (record)

Sugestão mínima: `status (int)`, `message (String)`, `path (String)`,
`timestamp (Instant)`.

## `GlobalExceptionHandler`

`@RestControllerAdvice`, um `@ExceptionHandler` por tipo:

| Exceção | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` | 409 |
| `MethodArgumentNotValidException` (bean validation) | 400 — agregar mensagens dos campos inválidos |
| `Exception` (genérico) | 500 — loga o erro, nunca deixa vazar `DataIntegrityViolationException`/`SQLException` cru |

`HttpStatus.UNPROCESSABLE_ENTITY` está deprecated nesta stack — se algum
handler precisar de 422 no futuro, usar `HttpStatus.valueOf(422)`
(nenhuma exceção do CRUD de `User` precisa de 422 hoje).

## Critérios de aceite

- [ ] Os 4 handlers acima implementados.
- [ ] Nenhuma exceção de infraestrutura vaza como stack trace cru para o
      cliente.
- [ ] Coberto por teste (`GlobalExceptionHandlerTest` ou via
      `UserControllerTest`, task 16).
