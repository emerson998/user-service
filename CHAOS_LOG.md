# Chaos Log (demo)

- `2026-07-23T16:54:56.210023+00:00` **chaos** — incidente #1 — NoResourceFoundException em /favicon.ico
- `2026-07-23T16:55:03.812632+00:00` **chaos** — incidente #2 — NoResourceFoundException em /
- `2026-07-23T16:55:37.949564+00:00` **fix** — incidente #2 resolvido — diagnostico: A causa raiz provavel é que o servidor não conseguiu encontrar um recurso estático para atender à requisição para o caminho raiz, o que pode ser devido à falta de um arquivo index.html ou uma configur
- `2026-07-23T16:55:59.655966+00:00` **fix** — incidente #1 resolvido — diagnostico: A causa raiz provavel é que o arquivo favicon.ico não foi encontrado no caminho especificado, o que indica um problema de configuração de recursos estáticos no servidor.
- `2026-07-23T17:02:15.180299+00:00` **chaos** — incidente #1 — NullPointerException em /api/v1/users
- `2026-07-23T17:02:46.789517+00:00` **fix** — incidente #1 resolvido — diagnostico: a causa raiz provavel e que o objeto User esta sendo criado ou atualizado com o nome nulo, o que esta causando um NullPointerException quando o metodo trim() e chamado, isso pode estar ocorrendo devid
- `2026-07-23T17:05:57.336351+00:00` **chaos** — incidente #38 — NullPointerException em /api/v1/users
- `2026-07-23T17:06:58.689451+00:00` **chaos** — incidente #78 — NullPointerException em /api/v1/users
- `2026-07-23T17:07:58.713007+00:00` **chaos** — incidente #109 — NullPointerException em /api/v1/users
