# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
# respecte quarkus.package.type=uber-jar défini dans le POM
RUN mvn -q -B -DskipTests package

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# le runner généré s’appelle *-runner.jar avec uber-jar
COPY --from=build /workspace/target/*-runner.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/app.jar"]
