# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
# build simple (pas de packaging forcé)
RUN mvn -q -DskipTests clean package

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# on copie TOUT le target du build pour avoir n'importe quel format
COPY --from=build /workspace/target/ /app/target/

ENV PORT=8080
EXPOSE 8080

# Choix auto : fast-jar -> runner.jar -> ton jar "SNAPSHOT"
CMD ["sh","-lc","\
if [ -f /app/target/quarkus-app/quarkus-run.jar ]; then \
  exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar /app/target/quarkus-app/quarkus-run.jar; \
elif ls /app/target/*-runner.jar >/dev/null 2>&1; then \
  J=$(ls /app/target/*-runner.jar | head -n1); \
  exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar \"$J\"; \
elif ls /app/target/bets-backend-*.jar >/dev/null 2>&1; then \
  J=$(ls /app/target/bets-backend-*.jar | head -n1); \
  exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar \"$J\"; \
else \
  echo 'Aucun JAR exécutable trouvé dans /app/target :' && ls -R /app/target && exit 1; \
fi"]
