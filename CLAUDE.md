# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a monorepo for an **error monitoring service** with multilingual support (다국어 지원 에러 모니터링 서비스). The project consists of a Spring Boot backend and Next.js frontend designed to work together.

**Architecture**: Monorepo with separate backend and frontend services that communicate via REST API.

## Development Commands

### Backend (Spring Boot)

```bash
cd backend

# Run the application
./gradlew bootRun

# Run tests (currently disabled)
./gradlew test

# Build the project (skip tests)
./gradlew build -x test

# Clean and rebuild
./gradlew clean build -x test
```

**Environment variables for backend**:
- `DB_URL` - Database connection URL (default: `jdbc:mysql://localhost:3306/error_monitor`)
- `DB_USER` - Database username (default: `root`)
- `DB_PW` - Database password (default: `password`)
- `SPRING_PROFILES_ACTIVE` - Active profile (default: `local`)
- `REDIS_HOST` - Redis host (default: `localhost`)
- `REDIS_PORT` - Redis port (default: `6379`)
- `MAIL_HOST` - SMTP host (default: `smtp.gmail.com`)
- `MAIL_USERNAME` - Email username
- `MAIL_PASSWORD` - Email password
- `FRONTEND_BASE_URL` - Frontend URL (default: `http://localhost:3000`)

### Frontend (Next.js)

```bash
cd frontend

# Install dependencies
pnpm install

# Run development server (with Turbopack)
pnpm run dev

# Build for production
pnpm run build

# Start production server
pnpm start

# Lint code
pnpm run lint
```

### Docker Environment

```bash
# Start MySQL only (recommended for local dev)
docker-compose up mysql -d

# Start all services (backend + MySQL)
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f
```

## Architecture

### Monorepo Structure

- **`backend/`** - Spring Boot 3.5.6 application (Java 21)
  - Uses Gradle for build management
  - MySQL database with JPA/Hibernate
  - Redis for caching (performance optimization)
  - Actuator endpoints at `/actuator` (health, info, metrics)

- **`frontend/`** - Next.js 15 application (TypeScript)
  - Uses App Router (not Pages Router)
  - Turbopack for fast development
  - Tailwind CSS for styling
  - React 19 RC

### Backend Architecture

**Location**: `backend/src/main/java/com/error/monitor/`

The backend follows a domain-driven structure (as planned):
- `domain/` - Domain models and business logic (project, error, notification)
- `global/` - Cross-cutting concerns (config, exception handling, response formatting)
- `service/` - Business logic and service layer
- `api/` - REST API controllers

**Configuration**:
- `application.yml` - Main configuration with environment variable defaults
- `application-local.yml` - Local development overrides
- JPA DDL mode: `update` (automatically creates/updates schema)
- SQL logging: Enabled in local profile

### Frontend Architecture

**Location**: `frontend/app/`

Uses Next.js App Router:
- `layout.tsx` - Root layout component
- `page.tsx` - Homepage component
- `api/` - API routes (if needed for BFF pattern)
- `components/` - Reusable React components
- `lib/` - Utility functions and shared code

### Service Communication

- **Frontend → Backend**: REST API calls to `http://localhost:8081`
- **Backend Port**: 8081
- **Frontend Port**: 3000
- **MySQL Port**: 3306
- **Redis Port**: 6379

## Key Technical Details

### Backend Stack
- Java 21 with Spring Boot 3.5.6
- Spring Data JPA with Hibernate
- MySQL 8.0 database
- Redis for caching (Spring Data Redis + Lettuce)
- Spring Mail for email notifications
- Bucket4j for rate limiting (100 req/min per API key, 20 req/min per IP)
- Lombok for boilerplate reduction
- Spring Actuator for monitoring
- Validation with Bean Validation
- Cloudflare R2 (S3-compatible) for session replay storage

### Frontend Stack
- Next.js 15 with App Router
- TypeScript 5
- React 19 RC
- Tailwind CSS 3.4
- ESLint with Next.js config

### Database Configuration

MySQL connection details (Docker default):
- Database: `error_monitor`
- Root password: `root`
- App user: `error_user`
- App password: `error_password`

## Development Workflow

1. **Start Database**: Run `docker-compose up mysql -d`
2. **Start Redis** (optional): Run `docker-compose up redis -d` or install locally
3. **Start Backend**: Run `./gradlew bootRun` from `backend/`
4. **Start Frontend**: Run `pnpm run dev` from `frontend/`
5. **Access**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8081
   - Health check: http://localhost:8081/actuator/health

## Environment Setup

### Backend Requirements
- Java 21 (configured via Gradle toolchain)
- MySQL 8.0 (via Docker or local installation)
- Redis (optional for development, required for production)
- Gradle (wrapper included: `gradlew`)

### Frontend Requirements
- Node.js 18+
- pnpm (for package management)

## Testing

### Backend Tests

**Current Status**: Test files have been removed temporarily.

**Reason**: Tests were failing due to MySQL connection dependencies during build process.

**Planned Implementation**:
- Will use **H2 in-memory database** for integration tests
- Tests will not depend on external MySQL/Redis instances
- Faster test execution with embedded database
- Proper test isolation and cleanup between tests

**TODO**: Implement comprehensive test suite with:
- **Unit tests** for services and repositories
- **Integration tests** with H2 in-memory DB (@DataJpaTest, @SpringBootTest)
- **Controller tests** with MockMvc (@WebMvcTest)
- **Test fixtures** and data builders for consistent test data
- **Mockito** for mocking external dependencies (Discord, Slack, Email)
- **TestContainers** (optional) for more realistic integration tests

**Build without tests** (current approach):
```bash
cd backend
./gradlew build -x test
```

### Frontend Tests
Currently no test framework configured. Add as needed (Jest, Vitest, etc.).

## Recent Improvements

### Performance Optimization
- Redis caching for dashboard stats (10x faster)
- Redis caching for project queries (90% DB load reduction)
- Rate limiting to prevent API abuse

### Features Added
- Email notification system with HTML templates
- Configurable frontend URL for notifications
- Session replay URLs in error notifications
- Automated session replay cleanup (daily at 2 AM)
- Enhanced Discord bot with actual replay data

### Security
- Dual-layer rate limiting (API key + IP)
- Proxy-aware IP extraction for accurate rate limiting
