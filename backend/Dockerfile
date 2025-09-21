# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# pas de cache: on copie tout et on build
COPY pom.xml .
COPY src ./src

# FORCE le packaging Quarkus en uber-jar (produit *-runner.jar)
RUN mvn -q -B -DskipTests -Dquarkus.package.jar.type=uber-jar quarkus:build

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# copie uniquement le runner jar
COPY --from=build /workspace/target/*-runner.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/app.jar"]
