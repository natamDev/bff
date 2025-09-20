# --- build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
# force UBER-JAR => *-runner.jar
RUN mvn -q -B -DskipTests -Dquarkus.package.jar.type=uber-jar package
# debug: on vérifie que le jar existe
RUN ls -lah /workspace/target

# --- run ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# copie le *-runner.jar sous le nom app.jar
COPY --from=build /workspace/target/*-runner.jar /app/app.jar
RUN ls -lah /app
ENV PORT=8080
EXPOSE 8080
# utilise l'ENTRYPOINT de l'image (pas de Start command Koyeb)
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/app.jar"]
