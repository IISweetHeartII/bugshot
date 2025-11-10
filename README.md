# Error Monitoring Service

> 다국어 지원 에러 모니터링 서비스 (모노레포)

## 🏗️ 프로젝트 구조

```
error-monitor/
├── frontend/          # Next.js (TypeScript)
│   ├── app/          # App Router
│   ├── components/   # React 컴포넌트
│   └── public/       # 정적 파일
│
├── backend/           # Spring Boot (Java 21)
│   ├── src/main/
│   │   ├── java/
│   │   └── resources/
│   └── build.gradle
│
├── docker-compose.yml # 통합 개발 환경
└── README.md
```

## 🚀 빠른 시작

### 전체 요구사항

- Java 21
- Node.js 18+
- MySQL 8.0 (또는 Docker)

### 방법 1: Docker Compose (권장)

```bash
# MySQL만 실행
docker-compose up mysql -d

# 백엔드 실행 (로컬)
cd backend
./gradlew bootRun

# 프론트엔드 실행 (로컬)
cd frontend
pnpm install
pnpm run dev
```

### 방법 2: 로컬 실행

#### 백엔드

```bash
cd backend

# MySQL 연결 정보 설정 (환경변수)
export DB_URL=jdbc:mysql://localhost:3306/error_monitor
export DB_USER=root
export DB_PW=your_password

# 실행
./gradlew bootRun
```

#### 프론트엔드

```bash
cd frontend

# 의존성 설치
pnpm install

# 개발 서버 실행
pnpm run dev
```

## 📝 접속 URL

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **Actuator**: http://localhost:8080/actuator

## 🛠️ 기술 스택

### Frontend

- **Framework**: Next.js 15
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Features**: App Router, Turbopack

### Backend

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Gradle

## 📂 개발 가이드

### 백엔드 구조

```
backend/src/main/java/com/error/monitor/
├── ErrorMonitorApplication.java
├── domain/
│   ├── project/
│   ├── error/
│   └── notification/
└── global/
    ├── config/
    ├── exception/
    └── response/
```

### 프론트엔드 구조

```
frontend/
├── app/
│   ├── layout.tsx
│   ├── page.tsx
│   └── api/           # API Routes (필요 시)
├── components/
└── lib/
```

## 🔧 환경 변수

### Backend (.env 또는 환경변수)

```env
DB_URL=jdbc:mysql://localhost:3306/error_monitor
DB_USER=root
DB_PW=password
SPRING_PROFILES_ACTIVE=local
```

### Frontend (.env.local)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 📖 문서

- [PRD 문서](./PRD_ErrorMonitoring_Service.md)
- [아키텍처 진화 가이드](./ARCHITECTURE_EVOLUTION.md)
- [Next.js 풀스택 가이드](./NEXTJS_FULLSTACK_STARTER.md)

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📜 라이센스

MIT License
