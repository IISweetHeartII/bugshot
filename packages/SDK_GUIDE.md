# BugShot SDK 개발 가이드

## 📦 SDK 구조

```
packages/
├── sdk/                    # @bugshot/browser-sdk (바닐라 JS)
│   ├── src/
│   │   ├── types.ts       # TypeScript 타입 정의
│   │   ├── utils.ts       # 유틸리티 함수
│   │   ├── transport.ts   # 서버 전송 로직
│   │   ├── error-capture.ts    # 에러 캡처 시스템
│   │   ├── session-replay.ts   # 세션 리플레이
│   │   ├── client.ts      # 메인 클라이언트 클래스
│   │   ├── index.ts       # NPM 엔트리포인트
│   │   └── browser.ts     # CDN/IIFE 엔트리포인트
│   ├── dist/              # 빌드 결과물
│   ├── package.json
│   ├── tsconfig.json
│   └── rollup.config.js
│
├── react/                 # @bugshot/react
│   ├── src/
│   │   ├── ErrorBoundary.tsx      # React Error Boundary
│   │   ├── BugShotProvider.tsx # Provider 컴포넌트
│   │   ├── hooks.ts               # React Hooks
│   │   └── index.tsx              # 엔트리포인트
│   ├── dist/
│   ├── package.json
│   ├── tsconfig.json
│   └── rollup.config.js
│
└── examples/              # 사용 예제
    ├── vanilla-js.html    # 바닐라 JS 예제
    └── react-example.tsx  # React 예제
```

## 🔨 빌드 방법

### 1. 의존성 설치

```bash
# SDK
cd packages/sdk
npm install

# React 플러그인
cd ../react
npm install
```

### 2. 빌드

```bash
# SDK 빌드
cd packages/sdk
npm run build

# 결과물:
# - dist/index.esm.js        (ES Module - NPM용)
# - dist/index.cjs.js        (CommonJS - NPM용)
# - dist/bugshot.min.js   (IIFE - CDN용)
# - dist/index.d.ts          (TypeScript 정의)

# React 플러그인 빌드
cd ../react
npm run build

# 결과물:
# - dist/index.esm.js
# - dist/index.cjs.js
# - dist/index.d.ts
```

### 3. 개발 모드 (Watch)

```bash
# 파일 변경 시 자동 재빌드
npm run dev
```

## 🚀 NPM 배포

### SDK 배포

```bash
cd packages/sdk

# 버전 업데이트
npm version patch  # 1.0.0 → 1.0.1
npm version minor  # 1.0.0 → 1.1.0
npm version major  # 1.0.0 → 2.0.0

# NPM 배포
npm publish --access public

# NPM에 업로드됨:
# https://www.npmjs.com/package/@bugshot/browser-sdk
```

### React 플러그인 배포

```bash
cd packages/react

npm version patch
npm publish --access public

# https://www.npmjs.com/package/@bugshot/react
```

## 📡 CDN 배포

빌드된 `packages/sdk/dist/bugshot.min.js` 파일을:

1. **Cloudflare Pages/Workers** 또는 **Vercel**에 업로드
2. **jsDelivr** 사용:
   ```
   https://cdn.jsdelivr.net/npm/@bugshot/browser-sdk@1.0.0/dist/bugshot.min.js
   ```

3. **unpkg** 사용:
   ```
   https://unpkg.com/@bugshot/browser-sdk@1.0.0/dist/bugshot.min.js
   ```

## 🧪 테스트

### 로컬 테스트

```bash
# SDK 빌드
cd packages/sdk
npm run build

# HTTP 서버 실행
npx http-server . -p 8080

# 브라우저에서 예제 열기
# http://localhost:8080/examples/vanilla-js.html
```

### React 예제 테스트

```bash
# Create React App에서 테스트
npx create-react-app test-app
cd test-app

# 로컬 SDK 링크
cd ../packages/sdk
npm link

cd ../react
npm link

cd ../../test-app
npm link @bugshot/browser-sdk
npm link @bugshot/react

# 앱에서 사용
# import { ErrorBoundary } from '@bugshot/react';
```

## 📝 사용 예제

### 1. CDN (즉시 사용)

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.bugshot.com/sdk/1.0.0/bugshot.min.js"></script>
  <script>
    BugShot.init({
      apiKey: 'ew_your_api_key_here',
      environment: 'production'
    });
  </script>
</head>
<body>
  <h1>My Website</h1>
</body>
</html>
```

### 2. NPM (React)

```bash
npm install @bugshot/react @bugshot/browser-sdk
```

```tsx
import { BugShotProvider, ErrorBoundary } from '@bugshot/react';

function App() {
  return (
    <BugShotProvider config={{ apiKey: 'ew_your_key' }}>
      <ErrorBoundary>
        <YourApp />
      </ErrorBoundary>
    </BugShotProvider>
  );
}
```

### 3. NPM (바닐라 JS)

```bash
npm install @bugshot/browser-sdk
```

```javascript
import BugShot from '@bugshot/browser-sdk';

BugShot.init({
  apiKey: 'ew_your_key',
  environment: 'production',
  release: '1.0.0'
});

// 수동 에러 캡처
try {
  riskyOperation();
} catch (error) {
  BugShot.captureError(error);
}
```

## 🎯 주요 기능

### 1. 자동 에러 캡처
- ✅ `window.onerror` - 모든 JavaScript 에러
- ✅ `unhandledrejection` - Promise rejection
- ✅ React Error Boundary - React 컴포넌트 에러

### 2. 세션 리플레이
- ✅ 클릭 이벤트 녹화
- ✅ 입력 이벤트 녹화 (비밀번호 제외)
- ✅ 페이지 네비게이션
- ✅ SPA 라우팅 감지

### 3. 컨텍스트 수집
- ✅ 브라우저 정보
- ✅ 운영체제
- ✅ 디바이스 정보
- ✅ 사용자 정보
- ✅ 커스텀 태그

### 4. 전송 최적화
- ✅ 재시도 로직 (실패 시)
- ✅ Beacon API (페이지 언로드 시)
- ✅ 샘플링 (데이터 절감)

## 🔧 설정 옵션

```typescript
BugShot.init({
  // 필수
  apiKey: 'ew_...',

  // 선택
  endpoint: 'http://localhost:8081',  // API 엔드포인트
  environment: 'production',          // 환경
  release: '1.0.0',                  // 릴리스 버전
  enableSessionReplay: true,         // 세션 리플레이
  enableAutoCapture: true,           // 자동 캡처
  sampleRate: 1.0,                   // 샘플링 (0~1)
  debug: false,                      // 디버그 로그

  // 후크
  beforeSend: (error) => {
    // 에러 필터링 또는 수정
    if (error.message.includes('ignore')) {
      return null; // 전송 취소
    }
    return error;
  },

  // 사용자 정보
  user: {
    id: '123',
    email: 'user@example.com'
  }
});
```

## 📊 API 참고

### BugShot.init(config)
SDK 초기화

### BugShot.captureError(error, additionalInfo?)
에러 캡처

### BugShot.captureMessage(message, level?)
메시지 캡처

### BugShot.setUser(user)
사용자 정보 설정

### BugShot.setContext(key, value)
커스텀 컨텍스트 추가

### BugShot.close()
SDK 종료

## 🐛 문제 해결

### SDK가 초기화되지 않음
```javascript
// 디버그 모드 활성화
BugShot.init({
  apiKey: 'your-key',
  debug: true  // 콘솔에 로그 출력
});
```

### 에러가 전송되지 않음
1. API 키 확인
2. 네트워크 탭에서 `/api/ingest` 요청 확인
3. CORS 에러 확인
4. `beforeSend` 훅에서 `null` 반환 확인

### TypeScript 타입 에러
```bash
# 타입 정의 재설치
npm install --save-dev @types/node
```

## 🔗 참고 링크

- [API 문서](http://localhost:8081/swagger-ui.html)
- [대시보드](http://localhost:3000/dashboard)
- [GitHub](https://github.com/bugshot/bugshot)
