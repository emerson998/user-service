# Dev-mode: codigo entra por bind mount (docker-compose.yml na raiz), a imagem
# so traz JDK+Maven. "docker restart" reexecuta este CMD do zero, recompilando
# via Maven -- e o que faz o ciclo do chaos-fix-agent (bug real -> fix real ->
# restart) funcionar igual ao modo nativo (ver COMO_RODAR.md).
FROM maven:3.9-eclipse-temurin-21

WORKDIR /workspace/usuarios-service

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]
