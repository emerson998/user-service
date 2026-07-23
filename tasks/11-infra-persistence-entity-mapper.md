# Task 11 — infrastructure.persistence.mapper.UserEntityMapper

Status: [ ] Não iniciada

Depende de: Task 01, 09.

## Objetivo

Conversão entidade JPA ↔ domínio, estático, sem estado.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/infrastructure/persistence/mapper/UserEntityMapper.java`

## Especificação

Classe `final`, construtor privado:

```java
public static UserJpaEntity toEntity(User user) { ... }
public static User toDomain(UserJpaEntity entity) { ... }
```

`toEntity` deve preservar o `id` quando presente (update), não gerar um
novo.

## Critérios de aceite

- [ ] Round-trip `toDomain(toEntity(user))` preserva todos os campos
      (útil como caso de teste, mesmo que informal, dentro de
      `UserRepositoryAdapter` se houver teste de integração de
      persistência).
