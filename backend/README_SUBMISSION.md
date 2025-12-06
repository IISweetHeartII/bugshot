# Bugshot - Error Monitoring System

## 프로젝트 개요
실시간 JavaScript 에러 모니터링 및 세션 리플레이 시스템

## 시스템 요구사항

### 필수 요구사항
- **Java**: JDK 21 이상
- **Gradle**: 8.x (프로젝트에 포함된 wrapper 사용)
- **MySQL**: 8.0 이상
- **Redis**: 6.0 이상 (선택사항, 캐싱용)

### 권장 환경
- OS: Windows 10+, macOS, Linux
- 메모리: 최소 2GB RAM
- 디스크: 최소 500MB 여유 공간

## 컴파일 방법

### 방법 1: Gradle Wrapper 사용 (권장)
```bash
# Windows
gradlew.bat build -x test

# Linux/Mac
./gradlew build -x test
```

### 방법 2: 직접 Gradle 사용
```bash
gradle build -x test
```

**참고**: `-x test` 옵션은 테스트를 건너뜁니다 (MySQL 연결 필요).

## 실행 방법

### 1. 데이터베이스 설정
MySQL 데이터베이스를 생성하고 `src/main/resources/application.yml` 파일을 수정하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bugshot
    username: your_username
    password: your_password
```

### 2. JAR 파일 실행
```bash
java -jar build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

또는 Spring Boot 프로필 지정:
```bash
java -jar -Dspring.profiles.active=local build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

### 3. Gradle로 직접 실행
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

## 실행 확인

애플리케이션이 정상적으로 실행되면:
- **기본 포트**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health

## 주요 기능

1. **에러 수집**: JavaScript 에러를 실시간으로 수집 및 저장
2. **에러 중복 제거**: SHA-256 해시 기반 에러 그룹핑
3. **우선순위 계산**: 페이지 중요도, 발생 빈도, 영향받은 사용자 수 기반 자동 계산
4. **다중 채널 알림**: Discord, Slack, Email, Kakao Work, Webhook 지원
5. **세션 리플레이**: 사용자 세션 기록 및 재생
6. **대시보드**: 에러 통계 및 트렌드 분석

## API 사용 예시

### 1. 에러 수집 (Ingest)
```bash
curl -X POST http://localhost:8081/api/v1/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your_api_key" \
  -d '{
    "error": {
      "type": "TypeError",
      "message": "Cannot read property of undefined",
      "file": "/app/index.js",
      "line": 42
    },
    "context": {
      "url": "/checkout",
      "userAgent": "Mozilla/5.0..."
    }
  }'
```

### 2. 에러 목록 조회
```bash
curl -X GET "http://localhost:8081/api/v1/errors?projectId=your_project_id" \
  -H "X-User-Id: your_user_id"
```

## 문제 해결

### 포트 충돌
포트 8081이 이미 사용 중인 경우:
```bash
java -jar -Dserver.port=8082 build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

### 데이터베이스 연결 실패
- MySQL 서비스가 실행 중인지 확인
- `application.yml`의 데이터베이스 설정 확인
- 데이터베이스가 생성되어 있는지 확인

### 메모리 부족
JVM 힙 메모리 증가:
```bash
java -Xmx1024m -jar build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

## 프로젝트 구조

```
backend/
├── src/main/java/com/bugshot/
│   ├── domain/              # 도메인 모델
│   │   ├── auth/           # 사용자 인증
│   │   ├── project/        # 프로젝트 관리
│   │   ├── error/          # 에러 처리
│   │   ├── notification/  # 알림 서비스
│   │   └── replay/         # 세션 리플레이
│   └── global/             # 전역 설정
└── src/main/resources/     # 설정 파일
```

## 개발자 정보
- 팀명: Bugshot
- 팀원:
  - 김덕환 (20200477)
  - 정은재 (20226495)
- 과목: Object Oriented Programming (Prof. Bong-Soo Sohn)
- 개발 기간: 2025년

