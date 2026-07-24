# Task 11 — infrastructure.persistence.mapper.UserEntityMapper

Status: [x] Concluída

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

- [x] `toEntity` preserva o `id` recebido (não gera um novo).
- [ ] Round-trip `toDomain(toEntity(user))` preserva todos os campos —
      pendente de teste formal (task 16 não cobre este mapper
      explicitamente; considerar adicionar se houver tempo).
