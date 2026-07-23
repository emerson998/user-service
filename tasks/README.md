# Tasks — CRUD de Usuários (PLANO_IMPLEMENTACAO.md, seção 2)

Checklist de execução, um arquivo por passo, seguindo a ordem de camadas de
`AGENTS.md` ("Convenções ao adicionar uma nova feature/aggregate") aplicada
ao agregado `User`. Marque o checkbox abaixo **e** o status dentro do
arquivo da task conforme for implementando — isso é o que permite retomar
o trabalho em qualquer sessão futura sem precisar reconstruir o contexto
do zero.

Escopo: só a seção 2 do `PLANO_IMPLEMENTACAO.md` (CRUD). O chaos
(seção 3+) vira um novo lote de tasks depois que a 17 for concluída.

## Checklist

- [x] [00 — Dependência BCrypt no pom.xml](00-pom-password-encoder-dependency.md)
- [x] [01 — domain.model.User](01-domain-model-user.md)
- [ ] [02 — domain.repository.UserRepository (port)](02-domain-repository-port.md)
- [ ] [03 — Exceções de domínio](03-domain-exceptions.md)
- [ ] [04 — DTOs UserRequest/UserResponse](04-application-dto.md)
- [ ] [05 — UserMapper](05-application-mapper.md)
- [ ] [06 — UserUseCase (port.in)](06-application-port-in-usecase.md)
- [ ] [07 — PasswordEncoderPort (port.out)](07-application-port-out-password-encoder.md)
- [ ] [08 — UserService](08-application-service.md)
- [ ] [09 — UserJpaEntity](09-infra-persistence-entity.md)
- [ ] [10 — UserJpaRepository](10-infra-persistence-jpa-repository.md)
- [ ] [11 — UserEntityMapper](11-infra-persistence-entity-mapper.md)
- [ ] [12 — UserRepositoryAdapter](12-infra-persistence-adapter.md)
- [ ] [13 — BCryptPasswordEncoderAdapter](13-infra-security-bcrypt-adapter.md)
- [ ] [14 — UserController](14-web-controller.md)
- [ ] [15 — GlobalExceptionHandler](15-web-exception-handler.md)
- [ ] [16 — Testes (domain/service/controller)](16-tests-domain-application-web.md)
- [ ] [17 — Validação manual do baseline](17-baseline-manual-validation.md)

Status geral: 2/18 concluídas.

> ⚠️ Nenhuma compilação/teste foi verificada neste ambiente (sem acesso
> admin para rodar `./mvnw`/Maven). O código das tasks concluídas foi
> revisado manualmente, mas `./mvnw compile`/`./mvnw test` ainda precisam
> ser rodados por fora assim que houver acesso, antes de considerar essas
> tasks realmente fechadas.
> 
> 00 e 01 verificadas rodando compile manualmente

## Ordem e dependências

| # | Task | Camada | Depende de |
|---|------|--------|------------|
| 00 | Dependência BCrypt | build | - |
| 01 | User (domínio) | domain | - |
| 02 | UserRepository (port) | domain | 01 |
| 03 | Exceções de domínio | domain | - |
| 04 | UserRequest/UserResponse | application | 01 |
| 05 | UserMapper | application | 01, 04 |
| 06 | UserUseCase | application | 04 |
| 07 | PasswordEncoderPort | application | - |
| 08 | UserService | application | 01, 02, 03, 04, 05, 06, 07 |
| 09 | UserJpaEntity | infra | 01 |
| 10 | UserJpaRepository | infra | 09 |
| 11 | UserEntityMapper | infra | 01, 09 |
| 12 | UserRepositoryAdapter | infra | 02, 09, 10, 11 |
| 13 | BCryptPasswordEncoderAdapter | infra | 00, 07 |
| 14 | UserController | web | 04, 06, 08 |
| 15 | GlobalExceptionHandler | web | 03 |
| 16 | Testes | test | 01–15 |
| 17 | Validação manual do baseline | validação | 16 |

## Convenção de uso

- Antes de começar uma task, marque-a como `[~] Em andamento` no cabeçalho
  do arquivo dela.
- Ao terminar, marque `[x] Concluída` no arquivo da task **e** no
  checklist acima, e rode `./mvnw test` antes de seguir para a próxima
  quando a task tocar código compilável.
- Se uma task revelar uma decisão de design não coberta pelo
  `PLANO_IMPLEMENTACAO.md` (ex.: task 04 tem uma em aberto), pare e
  resolva com o usuário antes de codificar — não decida silenciosamente.
