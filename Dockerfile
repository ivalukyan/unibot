# Используем образ Eclipse Temurin (OpenJDK)
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/unibot-1.0-SNAPSHOT.jar app.jar

COPY src/main/resources/cred.json app/resources/cred.json

ENTRYPOINT ["java", "-jar", "app.jar"]