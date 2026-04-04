# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-21-jammy AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -q -DskipTests=true

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/trade-journal-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
