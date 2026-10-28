FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/backend
COPY backend/ .
COPY parceiro-90-dias/dashboard/ /workspace/parceiro-90-dias/dashboard/
RUN chmod +x ./mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/backend/target/parceiro-90-dias-1.0.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
