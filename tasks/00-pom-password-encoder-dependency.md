# Task 00 — Dependência do BCrypt no pom.xml

Status: [ ] Não iniciada

## Objetivo

Disponibilizar `BCryptPasswordEncoder` no classpath para a task 13, sem
puxar a auto-configuração de HTTP security (não precisamos de
autenticação/autorização neste CRUD, só de hashing de senha).

## Arquivos

- modificar: `pom.xml`

## O que fazer

Adicionar apenas:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

Não adicionar `spring-boot-starter-security` — versão gerenciada pelo BOM
do `spring-boot-starter-parent`, não precisa declarar `<version>`.

## Critérios de aceite

- [ ] Dependência adicionada em `pom.xml`.
- [ ] `./mvnw compile` continua funcionando.
- [ ] Nenhum bean de segurança HTTP é auto-configurado (não deve aparecer
      filtro de login/senha gerada no log de startup).
