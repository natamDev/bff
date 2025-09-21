# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Pas de cache: on copie tout puis on package
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# On copie tout le dossier target pour couvrir uber-jar ET fast-jar
COPY --from=build /workspace/target /app/target

ENV PORT=8080
EXPOSE 8080

# Lance uber-jar si présent, sinon fast-jar; sinon log clair et exit 1
ENTRYPOINT ["/bin/sh","-lc", "\
  if ls /app/target/*-runner.jar >/dev/null 2>&1; then \
    exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar /app/target/*-runner.jar; \
  elif [ -f /app/target/quarkus-app/quarkus-run.jar ]; then \
    exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar /app/target/quarkus-app/quarkus-run.jar; \
  else \
    echo 'Jar exécutable introuvable (ni *-runner.jar ni quarkus-app/quarkus-run.jar)'; \
    echo 'Contenu de /app/target:'; ls -R /app/target || true; \
    exit 1; \
  fi"]
