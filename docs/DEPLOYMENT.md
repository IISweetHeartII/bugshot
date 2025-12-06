# BugShot 배포 가이드 🚀

이 문서는 BugShot를 실제 프로덕션에 배포하는 완전한 가이드입니다.

## 📋 목차

1. [아키텍처 개요](#1-아키텍처-개요)
2. [사전 준비](#2-사전-준비)
3. [Cloudflare R2 설정](#3-cloudflare-r2-설정)
4. [백엔드 환경 설정](#4-백엔드-환경-설정)
5. [Mac Mini 백엔드 배포](#5-mac-mini-백엔드-배포)
6. [Cloudflare Tunnel 설정](#6-cloudflare-tunnel-설정)
7. [Vercel 프론트엔드 배포](#7-vercel-프론트엔드-배포)
8. [SDK CDN 배포 (선택)](#8-sdk-cdn-배포-선택)
9. [배포 테스트](#9-배포-테스트)
10. [비용 정리](#10-비용-정리)
11. [트러블슈팅](#11-트러블슈팅)

---

## 1. 아키텍처 개요

```
┌─────────────────────────────────────────────────────────┐
│                     사용자                               │
│  https://bugshot.com (Frontend)                      │
│  https://api.bugshot.com (Backend API)               │
└─────────────────────────────────────────────────────────┘
                    ↓                    ↓
┌────────────────────────────┐  ┌───────────────────────────┐
│   Vercel (Frontend)         │  │  Cloudflare Tunnel        │
│   - Next.js 15             │  │  - HTTPS 자동 SSL         │
│   - 자동 배포               │  │  - DDoS 보호              │
│   - Edge Functions         │  │  - Rate Limiting          │
└────────────────────────────┘  └───────────────────────────┘
                                          ↓
                      ┌─────────────────────────────────────┐
                      │   Mac Mini 홈서버 (192.168.x.x)     │
                      │  ┌────────────────────────────────┐ │
                      │  │  Docker Compose                │ │
                      │  │  ├─ Spring Boot (8081)         │ │
                      │  │  └─ MySQL 8.0 (3306)           │ │
                      │  └────────────────────────────────┘ │
                      │  ┌────────────────────────────────┐ │
                      │  │  cloudflared (터널 데몬)        │ │
                      │  └────────────────────────────────┘ │
                      └─────────────────────────────────────┘
                                          ↓
                      ┌─────────────────────────────────────┐
                      │   Cloudflare R2                     │
                      │   (세션 리플레이 저장소)             │
                      │   - 10GB까지 무료                   │
                      │   - S3 호환 API                     │
                      └─────────────────────────────────────┘
```

---

## 2. 사전 준비

### 2.1 필수 계정

✅ **Cloudflare 계정** (무료)

- R2 Storage (세션 리플레이 저장)
- Tunnel (HTTPS 터널링)
- DNS 관리 (도메인 연결)

✅ **Vercel 계정** (무료 Hobby 플랜)

- 프론트엔드 배포
- 자동 빌드 & 배포

✅ **도메인** (선택, 권장)

- Cloudflare에서 구매 또는 기존 도메인 이전
- 예: `bugshot.com`

### 2.2 Mac Mini 사양

최소 사양:

- Mac Mini M1 이상
- RAM 8GB 이상
- Storage 256GB 이상
- macOS Monterey 이상

설치 필요:

```bash
# Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Java 21
brew install openjdk@21

# Docker Desktop
brew install --cask docker

# Git
brew install git
```

---

## 3. Cloudflare R2 설정

### 3.1 R2 Bucket 생성

1. [Cloudflare Dashboard](https://dash.cloudflare.com) 로그인
2. **R2 Object Storage** 클릭
3. **Create bucket** 클릭
4. Bucket 이름: `bugshot-replays`
5. Location: **Automatic** (자동 선택)
6. **Create bucket** 클릭

### 3.2 R2 API Token 생성

1. **R2** → **Manage R2 API Tokens**
2. **Create API Token** 클릭
3. 설정:
   - **Token name**: `bugshot-backend`
   - **Permissions**: Object Read & Write
   - **Specify bucket**: `bugshot-replays`
   - **TTL**: Forever (만료 안 함)
4. **Create API Token** 클릭

**중요: 아래 정보를 안전하게 저장하세요!**

```
Access Key ID: xxxxxxxxxxxxxxxxxxxxx
Secret Access Key: yyyyyyyyyyyyyyyyyyyyyyyyyyyyy
Account ID: zzzzzzzzzzzzzzzzzzzzzzzzzzz
```

이 정보는 한 번만 보이므로 반드시 메모하세요!

### 3.3 R2 Public Access 설정 (선택)

세션 리플레이 다운로드를 위해 Public Access 허용:

1. Bucket `bugshot-replays` 클릭
2. **Settings** → **Public Access**
3. **Allow Public Access** 활성화
4. Custom Domain 추가 (선택):
   - `replays.bugshot.com`

---

## 4. 백엔드 환경 설정

### 4.1 환경 변수 파일 생성

프로젝트 루트에서:

```bash
cd C:/projects/bugshot
cp .env.example .env
```

### 4.2 `.env` 파일 수정

```env
# ======================
# Database Configuration
# ======================
DB_URL=jdbc:mysql://mysql:3306/error_monitor?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=error_user
DB_PW=your_strong_password_here_123!

# MySQL Root Password (Docker Compose용)
MYSQL_ROOT_PASSWORD=your_root_password_here_456!
MYSQL_DATABASE=error_monitor
MYSQL_USER=error_user
MYSQL_PASSWORD=your_strong_password_here_123!

# ======================
# Cloudflare R2
# ======================
CLOUDFLARE_R2_ACCOUNT_ID=zzzzzzzzzzzzzzzzzzzzzzzzzzz
CLOUDFLARE_R2_BUCKET=bugshot-replays
CLOUDFLARE_R2_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxx
CLOUDFLARE_R2_SECRET_KEY=yyyyyyyyyyyyyyyyyyyyyyyyyyyyy
CLOUDFLARE_R2_ENDPOINT=https://${CLOUDFLARE_R2_ACCOUNT_ID}.r2.cloudflarestorage.com
CLOUDFLARE_R2_PUBLIC_URL=https://replays.bugshot.com

# ======================
# Spring Configuration
# ======================
SPRING_PROFILES_ACTIVE=prod

# ======================
# JWT Secret
# ======================
JWT_SECRET=your_very_long_and_random_jwt_secret_key_minimum_256_bits

# ======================
# OAuth2 - GitHub (선택)
# ======================
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# ======================
# OAuth2 - Google (선택)
# ======================
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_google_client_secret

# ======================
# Discord Webhook (선택)
# ======================
DISCORD_BOT_TOKEN=your_discord_bot_token

# ======================
# Internal API Secret (BFF Pattern)
# ======================
# Frontend와 동일한 값 사용
INTERNAL_API_SECRET=your_internal_api_secret_here
```

**보안 팁:**

- 비밀번호는 최소 16자 이상, 특수문자 포함
- JWT_SECRET은 최소 32자 이상의 랜덤 문자열
- `.env` 파일은 절대 Git에 커밋하지 마세요!

### 4.3 강력한 비밀번호 생성

```bash
# macOS에서 랜덤 비밀번호 생성
openssl rand -base64 32
```

---

## 5. Mac Mini 백엔드 배포

### 5.1 프로젝트 클론 (Mac Mini에서)

```bash
# SSH로 Mac Mini 접속 또는 직접 작업
mkdir -p ~/projects
cd ~/projects
git clone https://github.com/YOUR-USERNAME/bugshot.git
cd bugshot
```

### 5.2 환경 변수 복사

위에서 작성한 `.env` 파일을 Mac Mini로 복사:

```bash
# 로컬에서 Mac Mini로 전송 (SCP)
scp .env your-username@mac-mini-ip:~/projects/bugshot/

# 또는 Mac Mini에서 직접 작성
nano .env
# (위 내용 붙여넣기)
```

### 5.3 Docker Compose로 실행

```bash
cd ~/projects/bugshot

# Docker Compose로 빌드 & 실행
docker-compose up --build -d
```

### 5.4 실행 확인

```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f backend

# Health Check
curl http://localhost:8081/actuator/health
```

예상 응답:

```json
{ "status": "UP" }
```

### 5.5 재시작 시 자동 실행 설정

Docker Desktop 설정:

1. Docker Desktop 실행
2. **Settings** → **General**
3. ✅ **Start Docker Desktop when you log in** 체크

또는 LaunchDaemon으로 설정:

`~/Library/LaunchAgents/com.bugshot.docker.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.bugshot.docker</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/local/bin/docker-compose</string>
        <string>-f</string>
        <string>/Users/your-username/projects/bugshot/docker-compose.yml</string>
        <string>up</string>
        <string>-d</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>WorkingDirectory</key>
    <string>/Users/your-username/projects/bugshot</string>
</dict>
</plist>
```

로드:

```bash
launchctl load ~/Library/LaunchAgents/com.bugshot.docker.plist
```

---

## 6. Cloudflare Tunnel 설정

### 6.1 cloudflared 설치 (Mac Mini에서)

```bash
brew install cloudflare/cloudflare/cloudflared
cloudflared --version
```

### 6.2 Cloudflare 로그인

```bash
cloudflared tunnel login
```

브라우저가 열리면:

1. 로그인
2. 도메인 선택 (예: `bugshot.com`)
3. **Authorize** 클릭

인증 파일 저장: `~/.cloudflared/cert.pem`

### 6.3 Tunnel 생성

```bash
cloudflared tunnel create bugshot-api
```

출력 예시:

```
Created tunnel bugshot-api with id c8020eea-444c-41eb-85c8-302e025fe1cd
```

**Tunnel ID를 복사하세요!**

인증 파일 저장: `~/.cloudflared/c8020eea-444c-41eb-85c8-302e025fe1cd.json`

### 6.4 설정 파일 작성

`~/.cloudflared/config.yml` 생성:

```yaml
tunnel: bugshot-api
credentials-file: /Users/your-username/.cloudflared/c8020eea-444c-41eb-85c8-302e025fe1cd.json

ingress:
  # Backend API
  - hostname: api.bugshot.com
    service: http://localhost:8081

  # Catch-all
  - service: http_status:404
```

**주의:**

- `credentials-file`의 경로를 실제 Tunnel ID로 변경하세요
- `hostname`을 실제 도메인으로 변경하세요

### 6.5 DNS 라우팅

```bash
cloudflared tunnel route dns bugshot-api api.bugshot.com
```

Cloudflare DNS에 CNAME 레코드가 자동으로 추가됩니다.

### 6.6 Tunnel 실행 테스트

```bash
cloudflared tunnel run bugshot-api
```

터미널에 로그가 출력되면 성공!

테스트:

```bash
curl https://api.bugshot.com/actuator/health
```

### 6.7 서비스로 등록 (자동 시작)

`~/Library/LaunchAgents/com.cloudflare.cloudflared.plist` 생성:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.cloudflare.cloudflared</string>
    <key>ProgramArguments</key>
    <array>
        <string>/opt/homebrew/bin/cloudflared</string>
        <string>--config</string>
        <string>/Users/your-username/.cloudflared/config.yml</string>
        <string>tunnel</string>
        <string>run</string>
        <string>bugshot-api</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
    <key>StandardOutPath</key>
    <string>/Users/your-username/cloudflared.out.log</string>
    <key>StandardErrorPath</key>
    <string>/Users/your-username/cloudflared.err.log</string>
</dict>
</plist>
```

**주의:** `your-username`을 실제 사용자명으로 변경하세요!

로드:

```bash
launchctl load ~/Library/LaunchAgents/com.cloudflare.cloudflared.plist
launchctl list | grep cloudflare
```

상태 확인:

```bash
# PID가 있고, Status가 0이면 정상
launchctl list | grep cloudflare
# 예: 12345  0  com.cloudflare.cloudflared
```

---

## 7. Vercel 프론트엔드 배포

### 7.1 GitHub에 코드 푸시

```bash
cd C:/projects/bugshot
git add .
git commit -m "chore: prepare for deployment"
git push origin main
```

### 7.2 Vercel 프로젝트 생성

1. [Vercel Dashboard](https://vercel.com) 로그인
2. **Add New** → **Project** 클릭
3. **Import Git Repository** → GitHub 저장소 `bugshot` 선택
4. 프로젝트 설정:
   - **Framework Preset**: Next.js (자동 감지)
   - **Root Directory**: `frontend` 입력
   - **Build Command**: `pnpm run build` (자동 감지됨)
   - **Install Command**: `pnpm install` (자동 감지됨)
   - **Output Directory**: `.next` (기본값)

### 7.3 환경 변수 설정

**Environment Variables** 섹션에서 추가:

```
BACKEND_URL=https://api.bugshot.com
INTERNAL_API_SECRET=your-internal-api-secret-here
NEXTAUTH_URL=https://bugshot.com
NEXTAUTH_SECRET=your-nextauth-secret-here
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

**중요:**
- `INTERNAL_API_SECRET`은 백엔드와 동일한 값을 사용해야 합니다
- `openssl rand -base64 32`로 시크릿 생성
- **모든 환경 (Production, Preview, Development)에 적용하세요!**

### 7.4 배포

**Deploy** 클릭!

빌드 진행 상황을 실시간으로 확인할 수 있습니다.

배포 완료 후:

- Production URL: `https://bugshot-xxxx.vercel.app`

### 7.5 커스텀 도메인 연결 (선택)

1. Vercel 프로젝트 → **Settings** → **Domains**
2. **Add** 클릭
3. 도메인 입력: `bugshot.com`
4. Vercel이 제공하는 CNAME 레코드를 Cloudflare DNS에 추가:

Cloudflare DNS:

```
Type: CNAME
Name: @ (또는 bugshot.com)
Target: cname.vercel-dns.com
Proxy: DNS only (회색 구름)
```

5. Vercel에서 도메인 확인 완료!

---

## 8. SDK CDN 배포 (선택)

SDK를 CDN으로 배포하여 사용자가 쉽게 사용할 수 있도록 합니다.

### 8.1 Vercel에 SDK 배포

Vercel Public 폴더 사용:

```bash
# 프론트엔드 public 폴더에 SDK 복사
mkdir -p frontend/public/sdk
cp packages/sdk/dist/bugshot.min.js frontend/public/sdk/
cp packages/sdk/dist/bugshot.min.js.map frontend/public/sdk/

git add frontend/public/sdk/
git commit -m "feat: add SDK to CDN"
git push origin main
```

CDN URL:

```
https://bugshot.com/sdk/bugshot.min.js
```

### 8.2 NPM 배포 (선택)

```bash
cd packages/sdk

# NPM 로그인
npm login

# 배포
npm publish --access public
```

설치:

```bash
npm install @bugshot/browser-sdk
```

---

## 9. 배포 테스트

### 9.1 백엔드 API 테스트

```bash
# Health Check
curl https://api.bugshot.com/actuator/health

# Swagger UI
open https://api.bugshot.com/swagger-ui.html
```

### 9.2 프론트엔드 테스트

```bash
open https://bugshot.com
```

로그인 페이지가 정상적으로 보이는지 확인!

### 9.3 SDK 테스트

간단한 HTML 파일 생성:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>BugShot SDK Test</title>
  </head>
  <body>
    <h1>BugShot SDK Test</h1>
    <button onclick="testError()">Test Error</button>

    <script src="https://bugshot.com/sdk/bugshot.min.js"></script>
    <script>
      BugShot.init({
        apiKey: "ew_test_YOUR_API_KEY",
        environment: "production",
        debug: true,
      });

      function testError() {
        throw new Error("Test error from SDK!");
      }
    </script>
  </body>
</html>
```

브라우저에서 열고 버튼 클릭 → 에러가 캡처되는지 확인!

### 9.4 전체 플로우 테스트

1. **프론트엔드** (`https://bugshot.com`)

   - 회원가입 → 로그인
   - 프로젝트 생성
   - API 키 복사

2. **SDK 설치**

   - 테스트 웹사이트에 SDK 설치
   - 에러 발생 시뮬레이션

3. **에러 확인**
   - 프론트엔드 Dashboard에서 에러 확인
   - 세션 리플레이 확인
   - Webhook 알림 테스트

---

## 10. 비용 정리

### 월간 비용 (KRW)

| 항목                              | 비용       | 비고            |
| --------------------------------- | ---------- | --------------- |
| Mac Mini 전기세 (12W × 24h × 30d) | ₩1,300     | 전기 요금 기준  |
| Cloudflare Tunnel                 | **₩0**     | 무료            |
| Cloudflare R2 (10GB)              | **₩0**     | 무료 티어       |
| Vercel (Hobby)                    | **₩0**     | 무료 플랜       |
| 도메인 (.com)                     | ₩1,500     | 월 환산         |
| **총 비용**                       | **₩2,800** | 약 **$2.10/월** |

### AWS 비교

AWS 동일 구성 비용:

- EC2 t3.medium: ₩35,000
- RDS MySQL t3.micro: ₩20,000
- S3 (10GB): ₩300
- ALB: ₩25,000
- Route53: ₩600
- **총 비용: ₩80,900/월**

**절감액: ₩78,100/월 (연 약 94만 원!!)** 🎉

---

## 11. 트러블슈팅

### 11.1 Cloudflare Tunnel이 연결 안 됨

**증상:** `Status 1` 또는 PID 없음

```bash
launchctl list | grep cloudflare
# -    1    com.cloudflare.cloudflared  ← 문제!
```

**해결:**

1. 로그 확인

```bash
cat ~/cloudflared.err.log
```

2. plist 파일 확인

```bash
cat ~/Library/LaunchAgents/com.cloudflare.cloudflared.plist
```

3. ProgramArguments 각 인자가 별도 `<string>` 태그에 있는지 확인!

4. 재시작

```bash
launchctl unload ~/Library/LaunchAgents/com.cloudflare.cloudflared.plist
launchctl load ~/Library/LaunchAgents/com.cloudflare.cloudflared.plist
```

### 11.2 Docker 컨테이너가 시작 안 됨

```bash
# 로그 확인
docker-compose logs backend
docker-compose logs mysql

# MySQL이 healthy 상태인지 확인
docker-compose ps

# 완전 재시작
docker-compose down
docker-compose up --build -d
```

### 11.3 Vercel 빌드 실패

**증상:** `Module not found: Can't resolve...`

**해결:**

```bash
cd frontend
pnpm install
pnpm run build  # 로컬에서 빌드 테스트
```

Vercel 환경 변수 확인:

- `NEXT_PUBLIC_API_URL`이 올바른지 확인

### 11.4 CORS 에러

**증상:** 프론트엔드에서 API 호출 시 CORS 에러

**해결:** 백엔드 `WebConfig.java` 확인:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("https://bugshot.com")  // 실제 도메인으로 변경
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true);
}
```

### 11.5 Rate Limiting 테스트

```bash
# 100회 이상 요청 (100 req/min 제한 테스트)
for i in {1..105}; do
  curl -H "X-API-Key: your-api-key" https://api.bugshot.com/api/ingest
done

# 105번째부터 429 에러 발생해야 함
```

---

## 12. 유지보수

### 12.1 로그 확인

```bash
# Cloudflare Tunnel
cat ~/cloudflared.out.log
tail -f ~/cloudflared.err.log

# Docker Backend
docker-compose logs -f backend

# MySQL
docker-compose logs -f mysql
```

### 12.2 백업

**MySQL 백업:**

```bash
cd ~/projects/bugshot
docker-compose exec mysql mysqldump -u root -p error_monitor > backup_$(date +%Y%m%d).sql
```

**환경 변수 백업:**

```bash
cp .env .env.backup.$(date +%Y%m%d)
```

**자동 백업 설정 (cron):**

```bash
crontab -e

# 매일 새벽 3시 백업
0 3 * * * cd ~/projects/bugshot && docker-compose exec mysql mysqldump -u root -pYOUR_PASSWORD error_monitor > ~/backups/error_monitor_$(date +\%Y\%m\%d).sql
```

### 12.3 업데이트

```bash
cd ~/projects/bugshot
git pull origin main
docker-compose up --build -d
```

### 12.4 모니터링

**UptimeRobot** (무료) 설정:

- URL: `https://api.bugshot.com/actuator/health`
- Interval: 5분
- Alert: Email

---

## 13. 보안 체크리스트

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는지 확인
- [ ] 모든 비밀번호가 16자 이상인지 확인
- [ ] JWT_SECRET이 32자 이상 랜덤 문자열인지 확인
- [ ] `INTERNAL_API_SECRET`이 백엔드와 프론트엔드에서 동일한지 확인
- [ ] Cloudflare R2 API Token이 최소 권한만 가지는지 확인
- [ ] MySQL 포트(3306)가 외부에 노출되지 않는지 확인 (Docker 내부만)
- [ ] Cloudflare Tunnel이 Rate Limiting 활성화되어 있는지 확인
- [ ] HTTPS만 사용하고 HTTP는 리다이렉트되는지 확인

---

## 14. 다음 단계

배포가 완료되었습니다! 🎉

이제 다음을 진행할 수 있습니다:

1. **마케팅 준비**

   - 랜딩 페이지 개선
   - 데모 영상 제작
   - 블로그 글 작성

2. **기능 추가**

   - Slack 웹훅 통합
   - Telegram 웹훅 통합
   - 이메일 알림

3. **모니터링 강화**
   - Prometheus + Grafana
   - Sentry 자체 에러 모니터링
   - 로그 분석

---

**Happy Deploying! 🚀**
