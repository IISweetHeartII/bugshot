# Development Dockerfile for Spring Boot
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Gradle wrapper and dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Source code
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Expose port (8081 - 8080은 다른 프로젝트에서 사용 중)
EXPOSE 8081

# Run the application
CMD ["./gradlew", "bootRun"]
