# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

```bash
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

## Architecture

### BFF (Backend-For-Frontend) Pattern

The frontend uses a BFF pattern where all API requests flow through Next.js API routes before reaching the Spring Boot backend. This provides:

- **Session-based authentication**: NextAuth handles OAuth (GitHub/Google) and manages JWT sessions
- **Server-to-server authentication**: API routes add `X-User-Id` and `X-Internal-Secret` headers to backend requests
- **Client isolation**: The `BACKEND_URL` is never exposed to the browser

**Request flow**: Browser → Next.js API Route (`/api/*`) → Spring Boot Backend (`localhost:8081`)

### Key Files

- `src/auth.ts` - NextAuth configuration with GitHub/Google providers and backend sync
- `src/lib/server-api.ts` - Server-side utilities for proxying requests to backend with auth headers
- `src/lib/api.ts` - Client-side API client (axios) that calls Next.js API routes (not backend directly)
- `src/types/api.ts` - TypeScript types matching backend API response formats

### Directory Structure

```
src/
├── app/
│   ├── (dashboard)/       # Dashboard route group with shared layout
│   │   ├── layout.tsx     # Sidebar navigation, auth check
│   │   ├── dashboard/     # Main dashboard with stats
│   │   ├── projects/      # Project list and detail pages
│   │   ├── errors/        # Error list, detail, and replay pages
│   │   └── settings/      # Settings and webhook configuration
│   ├── api/               # Next.js API routes (BFF layer)
│   │   ├── auth/          # NextAuth handlers
│   │   ├── projects/      # Proxy to /api/projects
│   │   ├── errors/        # Proxy to /api/errors
│   │   ├── dashboard/     # Proxy to /api/dashboard
│   │   ├── replays/       # Proxy to /api/replays
│   │   └── webhooks/      # Proxy to /api/webhooks
│   └── login/             # Login page with OAuth buttons
├── components/
│   ├── ui/                # Reusable UI components (button, input, dialog, etc.)
│   └── providers/         # Context providers (toaster)
├── lib/                   # Utilities and API client
└── types/                 # TypeScript type definitions
```

### Styling

Uses Tailwind CSS with custom theme colors defined in `tailwind.config.ts`:
- `bg-primary/secondary/tertiary` - Background colors (Discord-inspired dark theme)
- `text-primary/secondary/muted` - Text colors
- `severity-critical/high/medium/low` - Error severity colors
- `primary/primary-dark/primary-light` - Brand accent colors

### Route Groups

The `(dashboard)` directory uses Next.js route groups for shared layout. Pages under this group:
- Require authentication (redirects to `/login` if unauthenticated)
- Share sidebar navigation and top bar
- Are protected by client-side session check

### API Route Pattern

Most API routes follow a simple proxy pattern using `proxyToBackend()`:

```typescript
import { proxyToBackend } from "@/lib/server-api";

export async function GET(request: NextRequest) {
  return proxyToBackend(request, "/api/projects");
}
```

For routes needing custom logic, use `backendFetch()` with `getAuthenticatedUserId()`.

## Environment Variables

Required in `.env.local`:
- `BACKEND_URL` - Backend URL (default: `http://localhost:8081`)
- `INTERNAL_API_SECRET` - Shared secret with backend for server-to-server auth
- `NEXTAUTH_URL` - NextAuth base URL (default: `http://localhost:3000`)
- `NEXTAUTH_SECRET` - NextAuth JWT encryption secret
- `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` - GitHub OAuth
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` - Google OAuth

## Path Aliases

TypeScript path alias `@/*` maps to `./src/*`. Use `@/lib/api` instead of relative imports.
