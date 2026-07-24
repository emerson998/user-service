# AGENTS.md — Guia de arquitetura para microsserviços Java/Spring Boot

Guia de convenções e boas práticas para qualquer pessoa (ou agente) que for
criar, manter ou evoluir um microsserviço nesta stack. Leia isto antes de
abrir um PR. Este documento é genérico — não descreve um domínio de negócio
específico, apenas a arquitetura e os padrões de código a seguir.

## Visão geral

Stack: Java 21, Spring Boot 4.1.0 (Spring Framework 7), Spring Data JPA, H2
(dev/test) ou outro banco relacional em produção, Bean Validation,
springdoc-openapi (Swagger UI).

O projeto segue **Clean Architecture** (arquitetura hexagonal / ports &
adapters) com princípios **SOLID**: regras de negócio no domínio,
independentes de framework; infraestrutura (JPA, web, segurança, mensageria
etc.) depende do domínio, nunca o contrário.

## Arquitetura e estrutura de pacotes

```
com.business-operations.<nome-do-servico>
├── domain                  # núcleo — sem dependência de Spring/JPA
│   ├── model                # entidades de domínio ricas (regras de negócio)
│   ├── repository           # ports (interfaces) implementados pela infra
│   └── exception             # exceções de negócio
├── application              # casos de uso — orquestra o domínio
│   ├── dto                  # records de entrada/saída (Request/Response)
│   ├── mapper                # domínio <-> DTO
│   ├── port.in                # interfaces de use case (XxxUseCase)
│   ├── port.out               # interfaces para dependências externas
│   │                           # (ex.: envio de e-mail, geração de token,
│   │                           # hashing de senha) implementadas na infra
│   └── service                # implementação dos use cases (@Service)
├── infrastructure
│   ├── persistence
│   │   ├── entity            # entidades JPA (@Entity), isoladas do domínio
│   │   ├── repository         # Spring Data JpaRepository
│   │   ├── mapper             # entidade JPA <-> domínio
│   │   └── adapter             # implementa domain.repository usando JPA
│   ├── config                 # OpenApiConfig, beans gerais etc.
│   └── security                # (se houver) config de autenticação/autorização,
│                                # implementações de application.port.out
└── web
    ├── controller             # @RestController, depende de application.port.in
    └── exception               # @RestControllerAdvice + ErrorResponse
```

Regra de dependência (de fora para dentro): `web -> application -> domain`
e `infrastructure -> domain`/`application.port.out`. O domínio nunca importa
`infrastructure` nem `web`. Isso é o que permite trocar JPA por outra
tecnologia de persistência, ou trocar a implementação de um port externo,
sem tocar nas regras de negócio.

### Por que essa separação existe

- **Modelo de domínio (`domain.model`) ≠ entidade JPA (`infrastructure.persistence.entity`)**:
  são classes deliberadamente duplicadas. O modelo de domínio carrega as
  regras de negócio; a entidade JPA só existe para mapear tabelas. Não
  colapse as duas só para "economizar código" — isso vaza detalhes de
  persistência para o domínio.
- **Use cases como interface (`application.port.in`)**: os controllers
  dependem da interface, não da implementação (`XxxService` etc.),
  permitindo testar o controller com um mock simples e trocar a
  implementação sem tocar no web layer.
- **Repositórios e dependências externas como *port* (`domain.repository`,
  `application.port.out`)**: a interface mora no domínio/application; o
  adapter que a implementa mora na infraestrutura. Isso é Dependency
  Inversion — a camada interna define o contrato, a infraestrutura o
  satisfaz.

## Convenções ao adicionar uma nova feature/aggregate

Ao criar um novo agregado (ex.: "Pedido", "Usuário", "Notificação"), siga
esta ordem:

1. `domain.model` — classe rica com regras de negócio e factory estático
   (`createNew(...)`), sem anotações de framework.
2. `domain.repository` — interface do port de persistência.
3. `domain.exception` — exceções específicas, se necessário.
4. `application.dto` — `XxxRequest`/`XxxResponse` como `record`, com
   `jakarta.validation` nos campos do Request.
5. `application.mapper` — classe final utilitária (métodos estáticos).
6. `application.port.in.XxxUseCase` — uma interface por agregado, um método
   por operação (create/update/getById/listAll/delete...). Evite criar uma
   interface por operação — isso é over-engineering para CRUD simples.
7. `application.port.out` — se a feature depender de algo externo ao
   domínio (hashing, tokens, envio de mensagens, chamada a outro serviço),
   declare o contrato aqui antes de implementá-lo na infraestrutura.
8. `application.service.XxxService` — implementa o use case, injeta os
   repositórios/ports via construtor (sem `@Autowired` em campo).
9. `infrastructure.persistence.entity` — `@Entity` com construtor protegido
   sem args (exigência do JPA) + construtor completo.
10. `infrastructure.persistence.repository` — interface `JpaRepository`.
11. `infrastructure.persistence.mapper` — entidade <-> domínio.
12. `infrastructure.persistence.adapter` — implementa o port do domínio.
13. `web.controller` — REST controller com `@Valid`, anotações springdoc
    (`@Tag`, `@Operation`), retorno `ResponseEntity<T>`.
14. Testes (veja seção abaixo).
15. Se a feature introduzir uma nova exceção de negócio, mapeie-a em
    `web.exception.GlobalExceptionHandler`.

## Validação

- Toda validação de formato/obrigatoriedade fica nos DTOs de `application.dto`
  via `jakarta.validation.constraints` (`@NotBlank`, `@Email`, `@Min`, etc.).
- Toda validação de **regra de negócio** fica no **modelo de domínio**,
  nunca no controller nem no service. O service apenas orquestra chamadas.
- Regras que dependem de estado externo (ex.: "e-mail já cadastrado", "CPF
  já existe") ficam no `application.service`, pois exigem consulta ao
  repositório.

## Tratamento de erros

Toda exceção de negócio deve estender uma das classes em
`domain.exception` e ser mapeada em `web.exception.GlobalExceptionHandler`
para um status HTTP apropriado:

| Exceção                        | HTTP Status |
|---------------------------------|-------------|
| `ResourceNotFoundException`      | 404         |
| `DuplicateResourceException`     | 409         |
| `BusinessRuleException` (e subclasses específicas do domínio) | 422 |
| `MethodArgumentNotValidException` (bean validation) | 400 |

Nunca deixe uma exceção de infraestrutura (`DataIntegrityViolationException`,
`SQLException`, etc.) vazar para o cliente. Se uma nova categoria de erro
surgir, adicione um `@ExceptionHandler` específico em vez de deixar cair no
handler genérico (`Exception.class`), que apenas loga e devolve 500.

## Testes

- **Domínio** (`domain.model.*Test`): testes unitários puros (JUnit 5,
  AssertJ), sem mocks — o objetivo é validar as regras de negócio
  isoladamente.
- **Application** (`application.service.*Test`): JUnit 5 + Mockito,
  mockando os ports (`domain.repository.*`, `application.port.out.*`).
  Cobrem os fluxos de sucesso e as regras que orquestram múltiplas
  dependências.
- **Web** (`web.controller.*Test`): `@WebMvcTest` + `MockMvc`, mockando o
  use case com `@MockitoBean` (não `@MockBean`, removido nesta versão do
  Spring Boot). Cobrem serialização, validação (400) e mapeamento de
  exceção (404/422).
- **Integração end-to-end** (`*IntegrationTest`): `@SpringBootTest` +
  `@AutoConfigureMockMvc`, sobe o contexto real com H2 em memória e
  exercita o fluxo completo do agregado principal. Use esse tipo de teste
  com moderação — é o mais lento; prefira cobrir casos de borda nos testes
  de service/domínio.
- Rode `./mvnw test` antes de qualquer commit. Não marque uma tarefa como
  concluída com testes falhando.

## Particularidades desta stack (Spring Boot 4.1 / Spring Framework 7)

Esta stack usa uma versão muito recente do Spring Boot, que reorganizou
vários módulos. Ao adicionar dependências de teste, atenção:

- **`@MockBean` foi removido** — use `@MockitoBean`, importado de
  `org.springframework.test.context.bean.override.mockito.MockitoBean`
  (vem do módulo `spring-test`, já incluído em `spring-boot-starter-test`).
- **`@WebMvcTest` e `@AutoConfigureMockMvc` não estão mais em
  `spring-boot-test-autoconfigure`** — foram movidos para o artefato
  separado `org.springframework.boot:spring-boot-webmvc-test`, com pacote
  `org.springframework.boot.webmvc.test.autoconfigure`. Se o `pom.xml` não
  tiver essa dependência, adicione-a antes de criar um teste de slice web;
  não assuma o caminho "clássico" do Spring Boot 3.
- **springdoc-openapi**: use `2.8.6` ou mais recente. Versões antigas (ex.
  `2.6.0`) quebram em runtime (`NoSuchMethodError` em `ControllerAdviceBean`)
  por incompatibilidade com o Spring Framework 7. Se o Swagger UI passar a
  retornar 500 em `/v3/api-docs` após um bump de versão, essa
  incompatibilidade é o primeiro lugar a checar.
- `HttpStatus.UNPROCESSABLE_ENTITY` está deprecated nesta versão — use
  `HttpStatus.valueOf(422)`.

## Executando o projeto

```bash
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Console H2 (se habilitado): http://localhost:8080/h2-console

```bash
./mvnw test          # roda toda a suíte de testes
./mvnw clean test     # em caso de estado inconsistente em target/
```

## Regras gerais de estilo

- Construtores para injeção de dependência — nunca `@Autowired` em campo.
- Sem Lombok nos modelos de domínio ou entidades JPA (mantém explícito o
  que é regra de negócio vs. boilerplate); Lombok está disponível no
  `pom.xml` mas seu uso é opcional. Se decidir usar, prefira
  `@Getter`/`@RequiredArgsConstructor` em vez de `@Data` (evita
  `equals`/`hashCode`/`toString` gerados incorretamente em entidades JPA
  associadas).
- DTOs são sempre `record` (imutáveis, sem boilerplate).
- Mappers são classes `final` com construtor privado e métodos estáticos —
  não são componentes Spring, pois não têm estado nem dependências.
- Um `@Transactional` de classe em cada `*Service`, com
  `@Transactional(readOnly = true)` nos métodos de leitura.
- Não exponha entidades JPA nem o modelo de domínio diretamente pela API —
  a fronteira HTTP sempre fala DTOs de `application.dto`.
- Ao criar um novo microsserviço a partir deste template, substitua
  `com.business-operations.<nome-do-servico>` pelo pacote real e ajuste
  `spring.application.name`/nome do banco H2 em `application.properties`.
