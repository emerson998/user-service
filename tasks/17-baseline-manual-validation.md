# Task 17 — Validação manual do baseline saudável

Status: [ ] Não iniciada

Depende de: Task 16.

## Objetivo

Corresponde ao passo 0 de `PLANO_IMPLEMENTACAO.md` § 3.6 e aos passos 1–2
de § 6: provar, manualmente, que o CRUD funciona **antes** de qualquer
chaos existir. É o gate que libera o próximo lote de tasks (chaos,
seção 3 do plano).

## Passos

1. Subir a aplicação: `./mvnw spring-boot:run`.
2. `POST /api/v1/users` com um payload válido → esperar `201`.
3. `GET /api/v1/users` → esperar `200` com o usuário criado na lista.
4. `GET /api/v1/users/{id}` do usuário criado → `200`.
5. `PUT /api/v1/users/{id}` alterando nome/telefone/bio → `200` com os
   dados atualizados.
6. `DELETE /api/v1/users/{id}` → `204`; `GET` do mesmo id depois → `404`.
7. Conferir Swagger UI (`/swagger-ui/index.html`) lista os 5 endpoints
   corretamente.

## Critérios de aceite

- [ ] Todos os 6 passos acima executados com sucesso, sem nenhum registro
      corrompido e sem nenhum mecanismo de chaos armado.
- [ ] Resultado registrado aqui (data + observações), já que é a
      evidência de que a seção 2 do plano está de fato concluída.
