# Task 05 — application.mapper.UserMapper

Status: [ ] Não iniciada

Depende de: Task 01, 04.

## Objetivo

Mapper estático entre domínio e DTOs. **Importante**: nesta task, escrever
a versão **simples e correta** (com defesa contra `name` nulo, já que é a
prática certa) — os dois blocos comentados `CHAOS:BUG`/`CHAOS:FIX` do
`PLANO_IMPLEMENTACAO.md` § 3.3 só entram quando o lote de tasks do chaos
começar (depois da task 17). Não implementar o bug de propósito agora.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/application/mapper/UserMapper.java`

## Especificação

Classe `final`, construtor privado, métodos estáticos:

```java
public static User toDomain(UserRequest request, String passwordHash) {
    return User.createNew(request.name(), request.email(), passwordHash);
}

public static UserResponse toResponse(User user) {
    return new UserResponse(
            user.getId(), user.getName(), user.getEmail(),
            user.getPhone(), user.getBio(), user.isEnabled());
}
```

## Critérios de aceite

- [ ] Classe `final` com construtor privado (não é um `@Component`).
- [ ] Sem dependência de Spring.
