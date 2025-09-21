# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
# build standard -> target/quarkus-app/ (fast-jar)
RUN mvn -q -B -DskipTests package

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# on copie tout le dossier fast-jar
COPY --from=build /workspace/target/quarkus-app/ /app/
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/quarkus-run.jar"]
