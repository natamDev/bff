# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copie tout le projet (pas besoin de step séparés pour pom.xml/src si tu veux no-cache)
COPY . .

RUN mvn clean package -DskipTests quarkus:build

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copie uniquement le runner jar généré
COPY --from=build /workspace/target/*-runner.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/app.jar"]
