# 🎯 Bugshot

> 실시간 에러 모니터링 및 알림 서비스

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 소개

Bugshot은 웹 애플리케이션의 에러를 실시간으로 수집하고, Discord/Slack/카카오톡으로 즉시 알림을 보내주는 서비스입니다.

## 주요 기능

- 🔴 **실시간 에러 수집** - JavaScript SDK로 에러 자동 캡처
- 🎬 **세션 리플레이** - 에러 발생 당시 사용자 행동 녹화
- 📊 **대시보드** - 에러 통계 및 트렌드 분석
- 🔔 **알림** - Discord, Slack, Email 알림
- 🔑 **API Key 관리** - 프로젝트별 API 키 발급

## 기술 스택

### Backend
- Java 21 + Spring Boot 3.5
- MySQL 8.0 + Redis
- Cloudflare R2 (세션 리플레이 저장)

### Frontend
- Next.js 15 + TypeScript
- Tailwind CSS
- React 19

### SDK
- JavaScript SDK (Browser)
- Java SDK (JitPack)

## 빠른 시작

### 1. 환경 설정
```bash
cp .env.example .env
# .env 파일 수정
```

### 2. Docker로 실행
```bash
docker compose up -d
```

### 3. 접속
- Frontend: http://localhost:3000
- Backend API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html

## SDK 사용법

### JavaScript
```html
<script src="https://cdn.bugshot.log8.kr/sdk.js"></script>
<script>
  Bugshot.init({ apiKey: 'your-api-key' });
</script>
```

### Java
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.YOUR_USERNAME:bugshot-java-sdk:1.0.0'
}
```

## 라이선스

MIT License
