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

# Expose port
EXPOSE 8080

# Run the application
CMD ["./gradlew", "bootRun"]
