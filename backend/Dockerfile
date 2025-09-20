# --- build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
# 🔑 on appelle le goal Quarkus (sinon tu n'as qu'un petit .jar "fin")
RUN mvn -q -B -DskipTests \
    -Dquarkus.package.jar.type=uber-jar \
    -Dquarkus.package.output-name=app \
    quarkus:build
# debug: vérifier que le runner est bien là
RUN ls -lah /workspace/target

# --- run ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# on sait maintenant exactement comment s'appelle le jar
COPY --from=build /workspace/target/app-runner.jar /app/app.jar
RUN ls -lah /app
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-Dquarkus.http.port=${PORT}","-jar","/app/app.jar"]
