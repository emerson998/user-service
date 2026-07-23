# Task 06 — application.port.in.UserUseCase

Status: [ ] Não iniciada

Depende de: Task 04.

## Objetivo

Interface de caso de uso — o controller depende dela, não da implementação
(`UserService`).

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/application/port/in/UserUseCase.java`

## Assinatura

```java
public interface UserUseCase {
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserUpdateRequest request);
    UserResponse getById(Long id);
    List<UserResponse> listAll();
    void delete(Long id);
}
```

Uma interface por agregado, um método por operação — não criar uma
interface por operação (`AGENTS.md` § convenções, evitar over-engineering
para CRUD simples).

## Critérios de aceite

- [ ] `update` recebe `UserUpdateRequest` (decisão da task 04, opção 2).
- [ ] Implementada por `UserService` (task 08).
