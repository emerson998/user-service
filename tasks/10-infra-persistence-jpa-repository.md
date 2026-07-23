# Task 10 — infrastructure.persistence.repository.UserJpaRepository

Status: [ ] Não iniciada

Depende de: Task 09.

## Objetivo

Repositório Spring Data — detalhe de infraestrutura, nunca referenciado
fora da camada `infrastructure`.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/infrastructure/persistence/repository/UserJpaRepository.java`

## Especificação

```java
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);
}
```

## Critérios de aceite

- [ ] Usado só por `UserRepositoryAdapter` (task 12).
