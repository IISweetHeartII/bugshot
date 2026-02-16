# Bugshot - Real-time Error Monitoring System

실시간 JavaScript 에러 모니터링 및 세션 리플레이 시스템

---

## 🚀 Quick Start (Recommended)

**별도 설치 없이 바로 테스트 가능합니다!**

| 서비스                 | URL                                               |
| ---------------------- | ------------------------------------------------- |
| **대시보드**           | https://bugshot.log8.kr                           |
| **API 문서 (Swagger)** | https://bugshot-api.log8.kr/swagger-ui/index.html |
| **GitHub**             | https://github.com/IISweetHeartII/bugshot         |

### 테스트 방법

1. https://bugshot.log8.kr 접속
2. GitHub 또는 Google 계정으로 로그인
3. 새 프로젝트 생성 → API 키 발급
4. Swagger UI에서 API 직접 테스트 가능

---

## 📋 System Requirements

| 항목     | 요구사항                  |
| -------- | ------------------------- |
| Java     | JDK 21 이상               |
| Build    | Gradle 8.x (wrapper 포함) |
| Database | MySQL 8.0+                |
| Cache    | Redis 6.0+ (선택)         |

---

## 🔨 Build

```bash
# Windows
gradlew.bat build -x test

# Mac/Linux
./gradlew build -x test
```

> `-x test` 플래그는 MySQL 연결이 필요한 테스트를 스킵합니다.

---

## ▶️ Run

### Option 1: JAR 실행

```bash
java -jar build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

### Option 2: Gradle 직접 실행

```bash
# Windows
gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

> ⚠️ **참고**: 로컬 실행은 MySQL 8.0, Redis 6.0 설정이 필요합니다.
> 간편한 테스트를 위해 **배포된 사이트 이용을 권장**합니다.

---

## ✅ Verify

애플리케이션이 정상 실행되면:

- **메인**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health

---

## 📁 Project Structure

```
backend/
├── src/main/java/com/bugshot/
│   ├── domain/
│   │   ├── auth/           # 사용자 인증 (OAuth)
│   │   ├── project/        # 프로젝트 관리
│   │   ├── error/          # 에러 처리 핵심
│   │   ├── notification/   # 다중 채널 알림
│   │   ├── replay/         # 세션 리플레이
│   │   └── dashboard/      # 통계
│   └── global/             # 전역 설정
└── src/main/resources/
    └── application.yml
```

---

## 🔧 Key Features

1. **에러 수집**: JavaScript SDK를 통한 실시간 에러 수집
2. **에러 중복 제거**: SHA-256 해시 기반 에러 그룹핑
3. **우선순위 계산**: 페이지 중요도, 발생 빈도, 영향 사용자 수 기반
4. **다중 채널 알림**: Discord, Slack, Email, Kakao Work, Telegram, Webhook
5. **세션 리플레이**: 사용자 세션 기록 및 재생

---

## 👥 Team

| Role        | Name   | Student ID |
| ----------- | ------ | ---------- |
| Team Leader | 김덕환 | 20200477   |
| Team Member | 정은재 | 20226495   |

**Course**: 객체지향프로그래밍 02분반 - 손봉수 교수님
