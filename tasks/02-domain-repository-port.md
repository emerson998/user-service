# Task 02 — domain.repository.UserRepository (port)

Status: [ ] Não iniciada

Depende de: Task 01.

## Objetivo

Port de persistência do agregado `User`. Interface pura, sem anotação de
Spring Data — vive no domínio, é implementada pela infraestrutura
(`UserRepositoryAdapter`, task 12).

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/domain/repository/UserRepository.java`

## Assinatura

```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
```

## Critérios de aceite

- [ ] Nenhuma dependência de `org.springframework.data.*` neste arquivo.
- [ ] Usado por `UserService` (task 08) e implementado por
      `UserRepositoryAdapter` (task 12).
