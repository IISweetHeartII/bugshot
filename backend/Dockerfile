# ============================================
# Bugshot Backend - Multi-stage Dockerfile
# ============================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# Gradle 실행 권한 부여
RUN chmod +x gradlew

# 빌드 실행 (테스트 스킵)
RUN ./gradlew build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# curl 설치 (health check용) + 로그 디렉토리 생성
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/logs

# 환경변수 기본값
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 포트 노출
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# 실행 (JAVA_OPTS 적용)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
