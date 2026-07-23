# Task 13 — infrastructure.security.BCryptPasswordEncoderAdapter

Status: [ ] Não iniciada

Depende de: Task 00, 07.

## Objetivo

Implementação concreta do `PasswordEncoderPort` usando
`BCryptPasswordEncoder`.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/infrastructure/security/BCryptPasswordEncoderAdapter.java`

## Especificação

```java
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) { return delegate.encode(rawPassword); }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
```

## Critérios de aceite

- [ ] Único ponto do código que importa
      `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`.
- [ ] `./mvnw compile` funciona após a task 00 adicionar a dependência.
