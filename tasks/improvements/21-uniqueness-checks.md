# Task 21 — Uniqueness Checks (email + CPF)

Status: [x] Concluída

Depende de: Task 19, 20.

## Objetivo

Validar unicidade de e-mail **e CPF** antes de executar o upsert. Decisão
registrada (ver `README.md` § Decisões em aberto): adicionar campo `cpf` ao
`domain.model.User`, validado com a anotação `@CPF` do Hibernate Validator
(`org.hibernate.validator.constraints.br.CPF`, já disponível via
`spring-boot-starter-validation` → `hibernate-validator`, sem nova
dependência no `pom.xml`).

## Arquivos

- alterar: `src/main/java/com/solutis/dev/domain/model/User.java` (novo
  campo `cpf`)
- alterar: `src/main/java/com/solutis/dev/domain/repository/UserRepository.java`
  (novo método `findByCpf`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/entity/UserJpaEntity.java`
  (novo campo `cpf`, `@Column(unique = true)`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/repository/UserJpaRepository.java`
  (novo método `findByCpf`)
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/mapper/UserEntityMapper.java`
- alterar: `src/main/java/com/solutis/dev/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- alterar: `src/main/java/com/solutis/dev/application/dto/user/UserRequest.java`,
  `UserUpdateRequest.java`, `UserUpsertRequest.java` (novo campo `cpf`
  onde fizer sentido — `UserRequest`/`UserUpsertRequest` sim,
  `UserUpdateRequest` não, já que CPF não deveria mudar após criado)
- alterar: `src/main/java/com/solutis/dev/application/dto/user/UserResponse.java`
- alterar: `src/main/java/com/solutis/dev/application/mapper/UserMapper.java`
- alterar: `src/main/java/com/solutis/dev/application/service/UserService.java`

## Especificação

- `User`: novo campo final `cpf` (String), incluído no construtor
  completo e em `createNew(name, email, cpf, passwordHash)` — atualizar a
  assinatura da factory (impacto em `UserMapper.toDomain`).
- `UserJpaEntity`: coluna `cpf`, `@Column(nullable = false, unique = true)`.
- DTOs: `@NotBlank @CPF String cpf` em `UserRequest`/`UserUpsertRequest`;
  `UserResponse` passa a expor `cpf`.
- `UserService.create`/`upsert`: antes de salvar, checar
  `userRepository.findByCpf(request.cpf())` presente → mesmo tratamento
  do e-mail duplicado (`DuplicateResourceException`).
- **Migração de dado existente**: como `spring.jpa.hibernate.ddl-auto=update`
  (H2), usuários já persistidos sem `cpf` quebrariam a constraint
  `nullable = false` — como o dataset é H2 em memória (recriado a cada
  subida), isso não afeta dev/test; sinalizar explicitamente se um banco
  persistente (task 42, Postgres) já tiver dado antes desta task.

## Critérios de aceite

- [x] `POST`/`PUT upsert` com CPF já cadastrado devolve `409`.
- [x] `POST`/`PUT upsert` com CPF em formato inválido devolve `400`
      (mensagem do `@CPF`).
- [x] `UserResponse` expõe `cpf`.
- [x] Coberto por `UserServiceTest` (CPF duplicado) e `UserTest`.
      **Confirmado pelo usuário**: `mvn clean compile` e `mvn test`
      rodados manualmente no IntelliJ.
