# ErrorWatch 배포 체크리스트 ✅

이 문서는 배포 시 순서대로 따라가며 체크할 수 있는 간단한 체크리스트입니다.

자세한 설명은 [DEPLOYMENT.md](./DEPLOYMENT.md)를 참고하세요.

---

## 1단계: Cloudflare R2 설정 (10분)

- [x] Cloudflare 계정 생성 또는 로그인
- [x] R2 Bucket 생성: `errorwatch-replays`
- [x] R2 API Token 생성
- [x] 아래 정보 저장 (한 번만 보임!):
  ```
  Access Key ID: ________________________
  Secret Access Key: ________________________
  Account ID: ________________________
  ```

---

## 2단계: 환경 변수 설정 (5분)

- [x] 프로젝트 루트에 `.env` 파일 생성
- [x] `.env` 파일에 아래 내용 작성:

```env
# Database
DB_USER=error_user
DB_PW=________________________  # 16자 이상

# MySQL (Docker)
MYSQL_ROOT_PASSWORD=________________________  # 16자 이상
MYSQL_DATABASE=error_monitor
MYSQL_USER=error_user
MYSQL_PASSWORD=________________________  # DB_PW와 동일

# Cloudflare R2
CLOUDFLARE_R2_ACCOUNT_ID=________________________
CLOUDFLARE_R2_BUCKET=errorwatch-replays
CLOUDFLARE_R2_ACCESS_KEY=________________________
CLOUDFLARE_R2_SECRET_KEY=________________________

# JWT Secret (32자 이상)
JWT_SECRET=________________________
```

- [x] 강력한 비밀번호 생성: `openssl rand -base64 32`
- [x] `.env` 파일 저장
- [x] `.env.backup` 백업 파일 생성

---

## 3단계: Mac Mini 백엔드 배포 (20분)

### 3.1 Mac Mini 준비

- [x] Java 21 설치: `brew install openjdk@21`
- [x] Docker Desktop 설치: `brew install --cask docker`
- [x] Docker Desktop 실행

### 3.2 프로젝트 배포

- [ ] 프로젝트 클론:

  ```bash
  mkdir -p ~/projects
  cd ~/projects
  git clone https://github.com/IISweetHeartII/error-monitor.git
  cd error-monitor
  ```

- [ ] `.env` 파일 복사 (로컬 PC → Mac Mini)

- [ ] Docker Compose 실행:

  ```bash
  docker-compose up --build -d
  ```

- [ ] Health Check:
  ```bash
  curl http://localhost:8081/actuator/health
  # 응답: {"status":"UP"}
  ```

---

## 4단계: Cloudflare Tunnel 설정 (15분)

### 4.1 Tunnel 생성

- [ ] cloudflared 설치: `brew install cloudflare/cloudflare/cloudflared`
- [ ] Cloudflare 로그인: `cloudflared tunnel login`
- [ ] 도메인 선택 및 Authorize
- [ ] Tunnel 생성: `cloudflared tunnel create errorwatch-api`
- [ ] Tunnel ID 저장: `________________________`

### 4.2 설정 파일

- [ ] `~/.cloudflared/config.yml` 생성:

  ```yaml
  tunnel: errorwatch-api
  credentials-file: /Users/YOUR-USERNAME/.cloudflared/[TUNNEL-ID].json

  ingress:
    - hostname: api.errorwatch.com # 실제 도메인으로 변경
      service: http://localhost:8081
    - service: http_status:404
  ```

- [ ] DNS 라우팅: `cloudflared tunnel route dns errorwatch-api api.errorwatch.com`

### 4.3 테스트

- [ ] Tunnel 실행: `cloudflared tunnel run errorwatch-api`
- [ ] 새 터미널에서 테스트:
  ```bash
  curl https://api.errorwatch.com/actuator/health
  ```
- [ ] Ctrl+C로 종료

### 4.4 자동 시작 설정

- [ ] `~/Library/LaunchAgents/com.cloudflare.cloudflared.plist` 생성 (DEPLOYMENT.md 참고)
- [ ] LaunchAgent 로드:
  ```bash
  launchctl load ~/Library/LaunchAgents/com.cloudflare.cloudflared.plist
  ```
- [ ] 상태 확인: `launchctl list | grep cloudflare`

---

## 5단계: Vercel 프론트엔드 배포 (10분)

### 5.1 GitHub 푸시

- [ ] 코드 커밋 & 푸시:
  ```bash
  git add .
  git commit -m "chore: prepare for deployment"
  git push origin main
  ```

### 5.2 Vercel 설정

- [ ] [Vercel Dashboard](https://vercel.com) 로그인
- [ ] **Add New** → **Project**
- [ ] GitHub 저장소 `error-monitor` 선택
- [ ] **Root Directory**: `frontend` 입력
- [ ] **Environment Variables** 추가:
  ```
  NEXT_PUBLIC_API_URL=https://api.errorwatch.com
  ```
- [ ] **Deploy** 클릭!

### 5.3 배포 확인

- [ ] 빌드 완료 대기 (2-3분)
- [ ] Production URL 확인: `https://error-monitor-xxxx.vercel.app`
- [ ] 브라우저에서 접속 테스트

### 5.4 커스텀 도메인 (선택)

- [ ] Vercel → **Settings** → **Domains**
- [ ] 도메인 추가: `errorwatch.com`
- [ ] Cloudflare DNS에 CNAME 레코드 추가
- [ ] 도메인 확인 완료

---

## 6단계: SDK CDN 배포 (선택, 5분)

- [ ] SDK를 public 폴더로 복사:

  ```bash
  mkdir -p frontend/public/sdk
  cp packages/sdk/dist/errorwatch.min.js frontend/public/sdk/
  ```

- [ ] Git 푸시:

  ```bash
  git add frontend/public/sdk/
  git commit -m "feat: add SDK to CDN"
  git push origin main
  ```

- [ ] Vercel 자동 배포 대기
- [ ] CDN URL 확인: `https://errorwatch.com/sdk/errorwatch.min.js`

---

## 7단계: 전체 테스트 (10분)

### 7.1 백엔드 테스트

- [ ] Health Check:

  ```bash
  curl https://api.errorwatch.com/actuator/health
  ```

- [ ] Swagger UI:
  ```
  https://api.errorwatch.com/swagger-ui.html
  ```

### 7.2 프론트엔드 테스트

- [ ] 프론트엔드 접속: `https://errorwatch.com`
- [ ] 회원가입 테스트
- [ ] 로그인 테스트
- [ ] 프로젝트 생성 테스트
- [ ] API 키 복사

### 7.3 SDK 테스트

- [ ] 테스트 HTML 파일 생성 (DEPLOYMENT.md 참고)
- [ ] SDK 초기화 확인
- [ ] 에러 발생 테스트
- [ ] Dashboard에서 에러 확인

---

## 8단계: 보안 점검 (5분)

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는지 확인
- [ ] GitHub에 `.env` 파일이 푸시되지 않았는지 확인
- [ ] 모든 비밀번호가 16자 이상인지 확인
- [ ] MySQL 포트(3306)가 외부에 노출되지 않는지 확인
- [ ] HTTPS만 사용하는지 확인 (HTTP 접근 불가)

---

## 9단계: 모니터링 설정 (선택, 10분)

- [ ] [UptimeRobot](https://uptimerobot.com) 계정 생성
- [ ] Monitor 추가:
  - Name: ErrorWatch API
  - Type: HTTP(s)
  - URL: `https://api.errorwatch.com/actuator/health`
  - Interval: 5분
- [ ] Alert Contact 설정 (이메일)
- [ ] 테스트 알림 확인

---

## 10단계: 백업 설정 (5분)

- [ ] `.env` 백업:

  ```bash
  cp .env .env.backup.$(date +%Y%m%d)
  ```

- [ ] MySQL 백업 스크립트 작성:

  ```bash
  mkdir -p ~/backups
  ```

- [ ] cron 백업 설정 (DEPLOYMENT.md 참고)

---

## 🎉 완료!

모든 체크리스트를 완료했습니다!

### 다음 단계

1. **팀원 초대** - 프로젝트에 팀원 추가
2. **Webhook 설정** - Discord/Slack 알림 설정
3. **사용량 모니터링** - Cloudflare R2 사용량 확인
4. **성능 최적화** - 필요 시 캐싱 추가

### 유지보수 체크리스트 (주간)

- [ ] 로그 확인
- [ ] 디스크 사용량 확인
- [ ] 백업 파일 확인
- [ ] 에러 발생 추이 확인

### 유지보수 체크리스트 (월간)

- [ ] MySQL 백업 보관함 정리
- [ ] Cloudflare R2 사용량 확인
- [ ] Docker 이미지 정리: `docker system prune -a`
- [ ] 의존성 업데이트 확인

---

**문제가 발생하면 [DEPLOYMENT.md](./DEPLOYMENT.md)의 트러블슈팅 섹션을 참고하세요!**
