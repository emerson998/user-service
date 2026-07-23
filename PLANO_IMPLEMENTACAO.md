# Plano de Implementação — CRUD de Usuários + Chaos por Dado Corrompido (NullPointerException)

## 1. Objetivo

1. Implementar o CRUD completo da entidade `User` no `usuarios-service`,
   seguindo a arquitetura hexagonal descrita em [AGENTS.md](AGENTS.md).
2. Implementar um **bug controlado de dado corrompido**: um bot de chaos
   insere, direto no banco, um usuário com um campo obrigatório faltando
   (`name = null`), o que derruba com `NullPointerException` todos os
   endpoints `GET` que leem a tabela de usuários — inclusive os consumidos
   por outras partes do sistema.
3. Corrigir isso é **código, não infraestrutura**: um agente (ex.: Claude
   Code) recebe o stack trace, edita a classe responsável e aplica uma
   correção de nulidade.
4. Depois de corrigido, precisa existir uma forma simples de **rollback do
   fix** (voltar o código para o estado com bug) para repetir a
   demonstração em outras apresentações, de forma rápida e sem
   redesenhar o cenário do zero.

> ⚠️ **Importante**: o mecanismo de corrupção de dado só deve existir atrás
> de um profile de demonstração (`demo`), nunca em produção. Ver seção 5.

## 2. CRUD de Usuários

Segue exatamente a convenção descrita em `AGENTS.md` (seção "Convenções ao
adicionar uma nova feature/aggregate").

### 2.1 Modelo de domínio — `domain.model.User`

Campos: `id`, `name`, `email`, `passwordHash`, `phone` (opcional), `bio`
(opcional), `enabled`. Sem senha em texto plano — hashing fica atrás de um
`application.port.out.PasswordEncoderPort` (implementado na infra com
`BCryptPasswordEncoder`), para não acoplar o domínio ao Spring Security.

Métodos de negócio: `createNew(name, email, passwordHash)`,
`updateProfile(name, phone, bio)`, `changePassword(newHash)`,
`activate()`/`deactivate()`.

### 2.2 Camadas a implementar

| Camada | Classe | Observação |
|---|---|---|
| `domain.repository` | `UserRepository` | port: save/findById/findByEmail/findAll/deleteById/existsById |
| `domain.exception` | `ResourceNotFoundException`, `DuplicateResourceException` | já seguem o padrão do handler global |
| `application.dto.user` | `UserRequest` (record, com `@NotBlank`/`@Email`/`@Size`), `UserResponse` | request nunca expõe senha em claro no response |
| `application.mapper` | `UserMapper` | estático, sem estado — **é aqui que o bug de nulidade vive** (seção 3) |
| `application.port.in` | `UserUseCase` | create/update/getById/listAll/delete |
| `application.port.out` | `PasswordEncoderPort` | encode/matches |
| `application.service` | `UserService` | `@Transactional`; valida e-mail duplicado via `findByEmail` antes de criar/atualizar |
| `infrastructure.persistence.entity` | `UserJpaEntity` | `@Entity`, `email` com `@UniqueConstraint` |
| `infrastructure.persistence.repository` | `UserJpaRepository` | `findByEmail` |
| `infrastructure.persistence.mapper` | `UserEntityMapper` | entidade <-> domínio |
| `infrastructure.persistence.adapter` | `UserRepositoryAdapter` | implementa `UserRepository` |
| `infrastructure.security` | `BCryptPasswordEncoderAdapter` | implementa `PasswordEncoderPort` |
| `web.controller` | `UserController` | `/api/v1/users` |
| `web.exception` | reaproveita `GlobalExceptionHandler` existente | mapear `DuplicateResourceException` → 409, `ResourceNotFoundException` → 404 |

### 2.3 Endpoints do CRUD

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/users` | Cria usuário (nome, e-mail, senha) |
| `GET` | `/api/v1/users` | Lista todos — **endpoint que quebra com o chaos** |
| `GET` | `/api/v1/users/{id}` | Busca por id — **também quebra se o id corrompido for consultado** |
| `PUT` | `/api/v1/users/{id}` | Atualiza dados/perfil |
| `DELETE` | `/api/v1/users/{id}` | Remove |

### 2.4 Testes do CRUD

- `domain.model.UserTest` — regras de negócio isoladas.
- `application.service.UserServiceTest` — mocka `UserRepository` e
  `PasswordEncoderPort`, cobre criação, duplicidade de e-mail, not found.
- `web.controller.UserControllerTest` — `@WebMvcTest` + `MockMvc`,
  `@MockitoBean` no `UserUseCase`.

## 3. O bug: dado corrompido no banco quebra os `GET`

### 3.1 Ideia central

Diferente de um "bug de infraestrutura" (timeout, thread travada), aqui o
bug é **de dado + código**, o que é muito mais próximo de um incidente real
de produção e mais interessante para um agente de correção de código atuar
em cima:

1. Existe uma validação de `@NotBlank` no `UserRequest` (seção 2) — então,
   pelo fluxo normal (`POST /api/v1/users`), é **impossível** criar um
   usuário com `name = null`.
2. O bot de chaos usa um caminho **interno**, que existe só para a demo,
   para inserir um `User` direto no repositório, **sem passar pela
   validação do DTO** — simulando exatamente o tipo de falha real que
   acontece quando um dado chega corrompido por outra via (migração,
   import em lote, outro serviço escrevendo direto no banco, etc.).
3. O código de leitura (`UserMapper.toResponse`) **hoje assume que `name`
   nunca é nulo**, porque, no fluxo normal, essa garantia sempre veio da
   validação do `POST`. Ele não tem defesa nenhuma contra nulo — essa
   ausência de defesa é o "bug" que o agente vai precisar corrigir.

```
com.emerson.usuarios
├── application.mapper.UserMapper           # <- os dois blocos (bug/fix) vivem aqui, um sempre comentado
└── infrastructure.chaos
    ├── ChaosUserCorruptionService.java      # insere o User corrompido, ignora validação
    └── web.controller.ChaosController        # trigger / status
scripts/chaos-toggle.sh                      # comenta/descomenta via git + reinicia a app
```

### 3.2 Endpoint que o bot de chaos chama

`POST /internal/chaos/users-flow/trigger`

```json
// Response 200
{
  "corruptedUserId": 42,
  "message": "Usuário 42 inserido sem 'name'. Qualquer GET em /api/v1/users vai quebrar."
}
```

Implementação (`ChaosUserCorruptionService`):

```java
@Service
public class ChaosUserCorruptionService {

    private final UserRepository userRepository; // mesmo port do CRUD

    public Long insertCorruptedUser() {
        // Construção direta do domínio, sem passar por UserRequest/@NotBlank —
        // é exatamente esse desvio da validação normal que simula o dado corrompido.
        User corrupted = new User(null, null, "chaos+" + System.nanoTime() + "@demo.io",
                "N/A", null, null, true);
        return userRepository.save(corrupted).getId();
    }
}
```

> Esse serviço só existe para a demo — vive isolado em
> `infrastructure.chaos`, nunca é chamado pelo `UserService` "de verdade".

### 3.3 `UserMapper.java` com os dois blocos lado a lado

Em vez de um flag em memória, o "estado" do bug mora **no próprio
arquivo-fonte**: duas implementações do mesmo método, cada uma dentro de um
par de marcadores; só uma fica descomentada (ativa) por vez. É esse
arquivo que o bot edita via git.

```java
public static UserResponse toResponse(User user) {
    // CHAOS:BUG >>>
    return new UserResponse(
            user.getId(),
            user.getName().trim(),   // NPE quando name == null
            user.getEmail(), user.getPhone(), user.getBio(), user.isEnabled());
    // <<< CHAOS:BUG

    // CHAOS:FIX >>>
    // String safeName = user.getName() != null ? user.getName().trim() : "(sem nome)";
    // return new UserResponse(
    //         user.getId(), safeName,
    //         user.getEmail(), user.getPhone(), user.getBio(), user.isEnabled());
    // <<< CHAOS:FIX
}
```

No estado acima (bloco `CHAOS:BUG` descomentado, `CHAOS:FIX` comentado), a
aplicação tem o bug ativo. Os marcadores `CHAOS:<NOME> >>> … <<< CHAOS:<NOME>`
existem só para o script do bot achar o início/fim de cada bloco com
segurança — sem eles, comentar/descomentar por regex seria frágil.

> Como é Java, os dois blocos **nunca podem estar descomentados ao mesmo
> tempo** (o método teria dois `return` e não compilaria) — é papel do
> script garantir essa exclusão mútua a cada troca.

### 3.4 `application.properties` como fonte da verdade do estado atual

Para não precisar abrir o `.java` e ver qual bloco está comentado, o
properties guarda um valor plano — só para leitura/rastreio, não é lido
pela lógica de negócio, é o próprio script que escreve nele a cada toggle:

```properties
# Rastreia qual bloco do UserMapper está ativo. Escrito pelo
# scripts/chaos-toggle.sh — não influencia o comportamento da aplicação,
# é só para status/observabilidade.
chaos.mapper.active-block=BUG
```

`GET /internal/chaos/users-flow/status` lê essa propriedade
(`@Value("${chaos.mapper.active-block}")`) e devolve, por exemplo:

```json
{ "activeBlock": "BUG", "corruptedUserId": 42 }
```

### 3.5 Script do bot — comenta/descomenta via git

`scripts/chaos-toggle.sh` recebe o bloco alvo (`FIX` para corrigir, `BUG`
para voltar o bug) e faz tudo em sequência: comenta o bloco atual,
descomenta o alvo, atualiza o properties, comita e atualiza a aplicação.

```bash
#!/usr/bin/env bash
# Uso:
#   scripts/chaos-toggle.sh FIX   -> aplica a correção
#   scripts/chaos-toggle.sh BUG   -> restaura o bug (para repetir a demo)
set -euo pipefail

TARGET="$1"                                   # FIX | BUG
OTHER=$([ "$TARGET" = "FIX" ] && echo BUG || echo FIX)
MAPPER="src/main/java/com/emerson/usuarios/application/mapper/UserMapper.java"
PROPS="src/main/resources/application.properties"

# comenta todo o bloco $OTHER (prefixa "// " em cada linha entre os marcadores, exceto os próprios marcadores)
sed -i "/CHAOS:$OTHER >>>/,/<<< CHAOS:$OTHER/{/CHAOS:$OTHER/!s#^\(\s*\)\([^[:space:]/]\)#\1// \2#}" "$MAPPER"

# descomenta todo o bloco $TARGET (remove um "// " logo após a indentação)
sed -i "/CHAOS:$TARGET >>>/,/<<< CHAOS:$TARGET/{/CHAOS:$TARGET/!s#^\(\s*\)// #\1#}" "$MAPPER"

# atualiza o properties com o bloco ativo (fonte da verdade para o /status)
sed -i "s/^chaos.mapper.active-block=.*/chaos.mapper.active-block=$TARGET/" "$PROPS"

git add "$MAPPER" "$PROPS"
git commit -m "chaos: alterna UserMapper para bloco $TARGET"

# --- atualiza a aplicação: escolha a linha que fizer sentido no seu ambiente ---
# (a) local, sem CI/CD:
pkill -f "usuarios-service" 2>/dev/null || true
nohup ./mvnw -q spring-boot:run > /tmp/usuarios-service.log 2>&1 &
# (b) serviço gerenciado por systemd:
# systemctl restart usuarios-service
# (c) pipeline de deploy (o push já dispara o build/deploy):
# git push
```

> ⚠️ O padrão de `sed` acima depende da indentação real do arquivo — vale
> ajustar/testar assim que `UserMapper.java` existir de verdade (ele ainda
> não existe: o CRUD é o passo 1 da seção 6). Uma alternativa mais robusta
> que dispensa regex é manter os dois blocos como **arquivos-fonte
> completos separados** (`UserMapper.bug.java` / `UserMapper.fix.java`, fora
> do build) e o script só copiar o alvo por cima de `UserMapper.java`
> (`cp UserMapper.$TARGET.java UserMapper.java`) — sem parsing de
> comentário, sem chance de deixar o arquivo num estado que não compila.

### 3.6 Ciclo completo (bug → fix → bug de novo)

0. **Baseline saudável**: com o CRUD implementado e a aplicação no ar,
   antes de qualquer chaos, `POST /api/v1/users` e `GET /api/v1/users`
   respondem `201`/`200` normalmente — nenhum registro corrompido, nenhum
   chaos armado. Isso prova que o bug só existe depois do passo 2, não
   porque o CRUD já nasce quebrado.
1. Estado inicial: `chaos.mapper.active-block=BUG` (bloco `CHAOS:BUG`
   ativo em `UserMapper.java`).
2. Bot de chaos chama `POST /internal/chaos/users-flow/trigger` → insere o
   usuário sem `name`.
3. `GET /api/v1/users` → `500` (`NullPointerException`) — esse mesmo 500
   já dispara a notificação REST ao agente (seção 3.8), então ninguém
   precisa ficar olhando log para perceber o incidente.
4. Bot/agente, avisado pela notificação, roda `scripts/chaos-toggle.sh FIX`
   → comenta `CHAOS:BUG`, descomenta `CHAOS:FIX`, atualiza o properties,
   comita, reinicia a aplicação.
5. `GET /api/v1/users` → `200` (inclusive o registro corrompido, agora com
   `"(sem nome)"`).
6. Antes da próxima apresentação: `scripts/chaos-toggle.sh BUG` — caminho
   inverso: comenta `CHAOS:FIX`, descomenta `CHAOS:BUG`, comita, reinicia.
7. Volta ao passo 2/3 — ver seção 3.7 sobre o que garante que o próximo
   `GET` já quebra de novo sem precisar chamar `/trigger` outra vez.

### 3.7 "Se eu voltar o bug, quebra de novo na hora?"

Sim — com uma ressalva importante: diferente de um flag em memória, este
mecanismo **reinicia o processo** a cada toggle (é um `git commit` +
restart de verdade). O H2 em memória
(`jdbc:h2:mem:usuariosdb;DB_CLOSE_DELAY=-1`) volta vazio a cada restart, e
o usuário corrompido some junto. Para garantir "quebra na hora" mesmo
assim:

- **Recomendado para o profile `demo`**: trocar o datasource para H2 **em
  arquivo** (`jdbc:h2:file:./data/usuariosdb`), assim o dado sobrevive ao
  restart e o passo 7 do ciclo funciona sem repetir o `/trigger`.
- **Alternativa mais simples**: aceitar que, depois de todo
  `scripts/chaos-toggle.sh BUG`, é preciso chamar
  `POST /internal/chaos/users-flow/trigger` de novo antes de seguir com a
  demo — mais um passo no roteiro, mas sem mexer no datasource.
- Se `spring-boot-devtools` estiver no classpath, alterar
  `UserMapper.java` em disco dispara um **restart automático do contexto
  Spring dentro do mesmo processo** (bem mais rápido que matar e subir o
  processo de novo) — vale considerar para deixar os passos 4/6 quase
  instantâneos.

### 3.8 Notificação do agente quando bugar (REST agora, RabbitMQ depois)

Em vez do agente descobrir o incidente olhando log/stack trace, a própria
aplicação avisa assim que o erro acontece. Transporte escolhido:
**REST (webhook HTTP)**, não RabbitMQ — `spring-boot-starter-web` já é
dependência do projeto (dá `RestClient` de graça), e não exige subir/
manter um broker só para a apresentação. Fica atrás de uma interface de
port, então trocar para RabbitMQ mais tarde é só um novo adapter, sem
mexer em quem chama.

```
com.emerson.usuarios
├── application.port.out.ErrorNotifierPort   # notify(ErrorNotification)
└── infrastructure.notification
    └── RestErrorNotifier.java                # POST via RestClient
```

```java
public interface ErrorNotifierPort {
    void notify(ErrorNotification event);
}

public record ErrorNotification(
        String service, String path, String exceptionType, String message, Instant timestamp) {
}
```

```java
@Component
public class RestErrorNotifier implements ErrorNotifierPort {

    private final RestClient restClient;
    private final String webhookUrl; // app.chaos.agent-webhook-url

    @Override
    public void notify(ErrorNotification event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return; // sem agente configurado, não faz nada
        }
        try {
            restClient.post().uri(webhookUrl).body(event).retrieve().toBodilessEntity();
        } catch (Exception e) {
            // notificação nunca pode virar um segundo erro — só loga
            log.warn("Falha ao notificar o agente em {}", webhookUrl, e);
        }
    }
}
```

Ligação no handler genérico já existente
(`web.exception.GlobalExceptionHandler.handleGeneric`), que já captura
qualquer `Exception` não mapeada (inclusive a `NullPointerException` do
chaos):

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
    errorNotifier.notify(new ErrorNotification(
            "usuarios-service", path(request), ex.getClass().getName(), ex.getMessage(), Instant.now()));
    log.error("Unexpected error handling request {}", path(request), ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request);
}
```

Como o hook fica no handler genérico (não em código específico de chaos),
**qualquer** erro inesperado real notifica o agente — não só o da demo.

Configuração (`application.properties`/`application-demo.properties`):

```properties
# vazio = notificação desligada (nenhuma chamada é feita)
app.chaos.agent-webhook-url=
```

Contrato do webhook (lado do agente, fora deste serviço):

```
POST <app.chaos.agent-webhook-url>
Content-Type: application/json

{
  "service": "usuarios-service",
  "path": "/api/v1/users",
  "exceptionType": "java.lang.NullPointerException",
  "message": "Cannot invoke \"String.trim()\" because ... is null",
  "timestamp": "2026-07-23T12:34:56Z"
}
```

O notificador ignora o corpo da resposta do agente; só loga se o `POST`
falhar (timeout curto, ex. 2s, para não segurar a resposta do 500 ao
cliente original).

> **Evolução futura**: quando fizer sentido desacoplar de verdade (mais de
> um consumidor, garantia de entrega, replay), trocar por
> `RabbitErrorNotifier implements ErrorNotifierPort` (`spring-boot-starter-amqp`,
> publica num exchange de tópicos) — zero mudança no
> `GlobalExceptionHandler` ou no restante do fluxo, graças ao port.

## 4. Guardas de segurança

- `ChaosController` e `ChaosUserCorruptionService` só são registrados com
  `@Profile("demo")` (ou `@ConditionalOnProperty(name = "chaos.enabled",
  havingValue = "true")`); o profile padrão não os expõe.
- Rodar a apresentação com `SPRING_PROFILES_ACTIVE=demo`.
- Opcional: exigir header `X-Chaos-Token` para chamar `/internal/chaos/**`,
  para que só o bot autorizado consiga inserir o dado corrompido.
- `scripts/chaos-toggle.sh` faz `git commit` (e possivelmente `git push`)
  automaticamente — mantenha esse script rodando contra um branch/repo
  dedicado à demo, nunca direto contra `main`/produção, para não misturar
  esses commits de "liga/desliga bug" com o histórico real do projeto.
- O bloco `CHAOS:BUG` só deve existir enquanto durar o ciclo de
  demonstrações — depois da última apresentação, rode
  `scripts/chaos-toggle.sh FIX` uma última vez e remova o bloco `CHAOS:BUG`
  e os marcadores do arquivo, deixando só o código corrigido definitivo.

## 5. Testes

- `application.mapper.UserMapperTest` — cobre o bloco `CHAOS:FIX`: com
  `name = null`, o mapper **não lança exceção** e devolve `"(sem nome)"`.
  Cobre também, num teste separado/documentado como "estado de demo", que
  o bloco `CHAOS:BUG` lança `NullPointerException` — útil para não deixar
  passar despercebido caso o bloco errado fique ativo em um merge.
- `ChaosUserCorruptionServiceTest` — confirma que o `User` persistido tem
  `name == null` (prova de que o bypass da validação funcionou).
- `ChaosControllerTest` — `@WebMvcTest`, valida `/trigger` e `/status`
  (retorna o `chaos.mapper.active-block` atual), e que os beans só sobem
  com o profile `demo`.
- Teste de integração (`ChaosFlowIntegrationTest`, profile `demo`):
  dispara `/trigger`, confirma `GET /api/v1/users` → 500 com o bloco
  `CHAOS:BUG` ativo (estado do repositório no momento do teste). Não dá
  para testar o toggle do script dentro do mesmo teste JVM (ele reinicia o
  processo) — validar o script separadamente, via shell, num ambiente de
  CI dedicado à demo.
- `RestErrorNotifierTest` — com `MockRestServiceServer` (Spring) ou
  `MockWebServer`, confirma que `notify(...)` faz o `POST` com o payload
  esperado, e que uma falha do webhook (timeout/5xx) **não** propaga
  exceção para quem chamou.
- Ampliar `GlobalExceptionHandlerTest` (ou criar um): com
  `ErrorNotifierPort` mockado, confirma que `handleGeneric` chama
  `notify(...)` antes de devolver o 500.

## 6. Ordem de execução sugerida

1. Implementar CRUD de `User` completo (seção 2) e validar com
   `./mvnw test`.
2. **Validar o baseline saudável** (passo 0 da seção 3.6): `POST`/`GET` em
   `/api/v1/users` respondendo `201`/`200` normalmente, sem chaos armado —
   confirma que o CRUD funciona antes de introduzir qualquer bug.
3. Implementar `ChaosUserCorruptionService` + `ChaosController`
   (`trigger`/`status`) atrás do profile `demo`.
4. Confirmar manualmente que `GET /api/v1/users` quebra com 500 depois do
   `trigger`, com o bloco `CHAOS:BUG` ativo.
5. Escrever `UserMapper.java` já com os dois blocos marcados
   (`CHAOS:BUG`/`CHAOS:FIX`) e o properties `chaos.mapper.active-block`.
6. Escrever e testar `scripts/chaos-toggle.sh` num branch de demo dedicado
   — validar manualmente o ciclo completo: `FIX` (500 → 200) e depois
   `BUG` (200 → 500 de novo).
7. Trocar o datasource do profile `demo` para H2 em arquivo (seção 3.7),
   para o "quebra de novo" não depender de rechamar `/trigger`.
8. Implementar `ErrorNotifierPort` + `RestErrorNotifier` (seção 3.8),
   ligar no `GlobalExceptionHandler`, e validar manualmente: `trigger` →
   `GET` → 500 → confirmar que `app.chaos.agent-webhook-url` recebeu o
   `POST` (ex.: apontar para um `nc -l` local ou um endpoint de teste tipo
   `webhook.site`/`RequestBin` durante o ensaio).
9. Ensaiar a sequência da seção 3.6 pelo menos uma vez de ponta a ponta
   antes da apresentação real, incluindo os dois sentidos do toggle e a
   notificação disparando no passo do 500.
