# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Bugshot** is a real-time error monitoring and session replay service. It captures JavaScript errors from client applications via SDKs, records user sessions, and provides a dashboard with notifications through Discord, Slack, Email, and Kakao.

## Development Commands

### Backend (Spring Boot)

```bash
cd backend
./gradlew bootRun              # Run application
./gradlew build -x test        # Build (skip tests - currently disabled)
./gradlew clean build -x test  # Clean rebuild
```

### Frontend (Next.js)

```bash
cd frontend
pnpm install     # Install dependencies
pnpm run dev     # Development server (Turbopack)
pnpm run build   # Production build
pnpm run lint    # Lint code
```

### SDK Packages

```bash
# Browser SDK
cd packages/sdk
npm install && npm run build   # Build SDK
npm run dev                    # Watch mode

# React SDK
cd packages/react
npm install && npm run build
```

### Docker Environment

```bash
docker-compose up mysql redis -d   # Start MySQL + Redis (recommended)
docker-compose up -d               # Start all services
docker-compose logs -f             # View logs
```

### Local Development Workflow

1. Start dependencies: `docker-compose up mysql redis -d`
2. Start backend: `cd backend && ./gradlew bootRun`
3. Start frontend: `cd frontend && pnpm run dev`
4. Access: Frontend http://localhost:3000, Backend http://localhost:8081, Swagger http://localhost:8081/swagger-ui.html

## Architecture

### Monorepo Structure

```
bugshot/
├── backend/          # Spring Boot 3.5 (Java 21) - Port 8081
├── frontend/         # Next.js 15 (TypeScript/React 19) - Port 3000
├── packages/
│   ├── sdk/          # @bugshot/browser-sdk (vanilla JS)
│   ├── react/        # @bugshot/react (React wrapper)
│   └── java-sdk/     # Java SDK (JitPack)
└── docs/             # Deployment guides
```

### Backend Domain Structure

**Location**: `backend/src/main/java/com/bugshot/`

```
domain/
├── auth/           # User entity with OAuth (GitHub, Google), plan types (FREE/PRO/TEAM)
├── project/        # Projects with API keys, environment settings, session replay config
├── error/          # Error deduplication, priority scoring, occurrences tracking
├── notification/   # Discord (JDA), Slack, Email (Spring Mail), Kakao integrations
├── replay/         # Session replay storage (Cloudflare R2 with local fallback)
├── dashboard/      # Statistics with Redis caching (10-min TTL)
├── webhook/        # Custom webhook endpoints
└── common/         # BaseEntity with audit fields
global/
├── config/         # SecurityConfig, RedisConfig
└── security/       # UserIdAuthenticationFilter (X-User-Id header auth)
```

**Key Algorithms**:
- **Error Deduplication**: SHA-256 hash of (errorType + filePath + lineNumber)
- **Priority Scoring**: Factors in page importance (checkout 10x, login 8x, homepage 5x), frequency, affected users
- **Rate Limiting**: Bucket4j - 100 req/min per API key, 20 req/min per IP

### Frontend Architecture (BFF Pattern)

**Location**: `frontend/src/`

The frontend uses Backend-For-Frontend pattern - all API requests flow through Next.js API routes.

```
src/
├── app/
│   ├── (dashboard)/        # Protected route group (requires auth)
│   │   ├── dashboard/      # Stats and trends
│   │   ├── projects/       # Project CRUD + [id] detail
│   │   ├── errors/         # Error list + [id] detail + [id]/replay
│   │   └── settings/       # User settings + webhooks
│   ├── api/                # BFF proxy routes to backend
│   └── login/              # OAuth login page
├── lib/
│   ├── api.ts              # Client-side axios (calls /api/* routes)
│   └── server-api.ts       # Server-side fetch (direct backend calls with X-User-Id)
└── auth.ts                 # NextAuth config (GitHub/Google OAuth)
```

**Request Flow**: Browser → Next.js API Route (`/api/*`) → Spring Boot Backend (`:8081`)

**Authentication**: NextAuth handles OAuth → Backend syncs user → `X-User-Id` header on all requests

### SDK Architecture

**Browser SDK** (`packages/sdk/src/`):
- `client.ts` - Main BugShot class, initialization
- `error-capture.ts` - Global error handlers (window.onerror, unhandledrejection)
- `session-replay.ts` - DOM mutation tracking, event recording
- `transport.ts` - HTTP submission with retry and Beacon API fallback

**React SDK** (`packages/react/src/`):
- `BugShotProvider.tsx` - Context provider for SDK initialization
- `ErrorBoundary.tsx` - React Error Boundary integration
- `hooks.ts` - `useBugShot()`, `useCaptureError()` hooks

## Key Configuration

### Environment Variables

**Backend** (see `.env.example`):
- `DB_URL`, `DB_USER`, `DB_PW` - MySQL connection
- `REDIS_HOST`, `REDIS_PORT` - Redis connection
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` - SMTP
- `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET` - Cloudflare R2

**Frontend** (`.env.local`):
- `BACKEND_URL` - Backend URL (default: `http://localhost:8081`)
- `NEXTAUTH_SECRET`, `NEXTAUTH_URL` - NextAuth
- `GITHUB_CLIENT_ID/SECRET`, `GOOGLE_CLIENT_ID/SECRET` - OAuth

### Ports

| Service | Port |
|---------|------|
| Frontend | 3000 |
| Backend | 8081 |
| MySQL | 3306 (Docker: 3307) |
| Redis | 6379 (Docker: 6380) |

## Testing

**Backend**: Tests currently disabled (MySQL dependency). Planned: H2 in-memory DB for integration tests.

**Frontend**: No test framework configured yet.

**SDK**: Manual testing via `packages/examples/vanilla-js.html`

## Deployment

- **Frontend**: Vercel (automatic from main branch)
- **Backend**: Mac Mini via Docker + Cloudflare Tunnel
- **Storage**: Cloudflare R2 for session replays
- See `docs/DEPLOYMENT.md` for full guide
