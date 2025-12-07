/**
 * BugShot SDK Types
 */

export interface BugShotConfig {
  /**
   * API 키 (필수)
   */
  apiKey: string;

  /**
   * API 엔드포인트 (기본값: https://bugshot-api.log8.kr)
   */
  endpoint?: string;

  /**
   * 환경 (production, staging, development 등)
   */
  environment?: string;

  /**
   * 릴리스 버전
   */
  release?: string;

  /**
   * 세션 리플레이 활성화 (기본값: true)
   */
  enableSessionReplay?: boolean;

  /**
   * 자동 에러 캡처 활성화 (기본값: true)
   */
  enableAutoCapture?: boolean;

  /**
   * 샘플링 비율 (0.0 ~ 1.0, 기본값: 1.0)
   */
  sampleRate?: number;

  /**
   * 디버그 모드
   */
  debug?: boolean;

  /**
   * 에러 발생 전 호출될 콜백
   */
  beforeSend?: (error: CapturedError) => CapturedError | null;

  /**
   * 사용자 정보
   */
  user?: UserInfo;
}

export interface CapturedError {
  type: string;
  message: string;
  stackTrace?: string;
  file?: string;
  line?: number;
  column?: number;
  method?: string;
}

export interface ErrorContext {
  userId?: string;
  url: string;
  httpMethod?: string;
  userAgent?: string;
  sessionId?: string;
  timestamp: string;
  browserInfo?: BrowserInfo;
  deviceInfo?: DeviceInfo;
}

export interface BrowserInfo {
  name: string;
  version: string;
  os: string;
}

export interface DeviceInfo {
  type: 'mobile' | 'tablet' | 'desktop';
  viewport: {
    width: number;
    height: number;
  };
}

export interface UserInfo {
  id?: string;
  email?: string;
  username?: string;
  [key: string]: any;
}

export interface SessionReplayEvent {
  type: string;
  timestamp: number;
  data: any;
}

export interface IngestPayload {
  apiKey: string;
  error: CapturedError;
  context: ErrorContext;
  sessionReplay?: {
    sessionId: string;
    durationMs: number;
    events: SessionReplayEvent[];
  };
}
