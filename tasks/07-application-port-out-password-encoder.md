# Task 07 — application.port.out.PasswordEncoderPort

Status: [ ] Não iniciada

## Objetivo

Port para hashing/verificação de senha — mantém o domínio/application
desacoplados de Spring Security.

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/application/port/out/PasswordEncoderPort.java`

## Assinatura

```java
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
```

## Critérios de aceite

- [ ] Sem import de `org.springframework.security.*` nesta interface.
- [ ] Implementado por `BCryptPasswordEncoderAdapter` (task 13).
- [ ] Injetado (via construtor) em `UserService` (task 08).
