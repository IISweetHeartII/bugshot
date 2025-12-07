/**
 * BugShot Client - Main SDK Class
 */

import type { BugShotConfig, CapturedError, ErrorContext, IngestPayload, UserInfo } from './types';
import { ErrorCapture } from './error-capture';
import { SessionReplay } from './session-replay';
import { Transport } from './transport';
import { shouldSample, log, generateUUID } from './utils';

const ANONYMOUS_USER_KEY = 'bugshot_anonymous_user_id';

export class BugShotClient {
  private config: BugShotConfig;
  private errorCapture: ErrorCapture;
  private sessionReplay: SessionReplay | null = null;
  private transport: Transport;
  private initialized = false;
  private anonymousUserId: string;

  constructor(config: BugShotConfig) {
    // 기본값 설정
    this.config = {
      endpoint: 'http://localhost:8081',
      environment: 'production',
      enableSessionReplay: true,
      enableAutoCapture: true,
      sampleRate: 1.0,
      debug: false,
      ...config,
    };

    // Validate API key
    if (!this.config.apiKey) {
      throw new Error('BugShot: API key is required');
    }

    // 익명 사용자 ID 초기화 (localStorage에서 가져오거나 새로 생성)
    this.anonymousUserId = this.getOrCreateAnonymousUserId();

    this.errorCapture = new ErrorCapture(this.config);
    this.transport = new Transport(this.config.endpoint!, this.config.debug);

    log(this.config.debug!, 'BugShot SDK initialized', this.config);
  }

  /**
   * 익명 사용자 ID 가져오기 또는 생성
   */
  private getOrCreateAnonymousUserId(): string {
    try {
      let userId = localStorage.getItem(ANONYMOUS_USER_KEY);
      if (!userId) {
        userId = 'anon_' + generateUUID();
        localStorage.setItem(ANONYMOUS_USER_KEY, userId);
        log(this.config.debug!, 'Created anonymous user ID:', userId);
      }
      return userId;
    } catch (e) {
      // localStorage 접근 불가 시 (private browsing 등)
      return 'anon_' + generateUUID();
    }
  }

  /**
   * 현재 사용자 ID 가져오기 (설정된 user.id 또는 익명 ID)
   */
  private getCurrentUserId(): string {
    return this.config.user?.id || this.anonymousUserId;
  }

  /**
   * SDK 초기화 및 시작
   */
  init(): void {
    if (this.initialized) {
      log(this.config.debug!, 'BugShot already initialized');
      return;
    }

    // 에러 캡처 시작
    this.errorCapture.start();

    // 에러 리스너 등록
    this.errorCapture.onError((error, context) => {
      this.handleError(error, context);
    });

    // 세션 리플레이 시작 (에러 발생 시 함께 전송됨)
    if (this.config.enableSessionReplay) {
      this.sessionReplay = new SessionReplay(this.errorCapture.getSessionId());
      this.sessionReplay.start();
      log(this.config.debug!, 'Session replay started');
    }

    this.initialized = true;
    log(this.config.debug!, 'BugShot started successfully');
    log(this.config.debug!, 'User ID:', this.getCurrentUserId());
  }

  /**
   * 에러 처리 및 전송
   */
  private async handleError(error: CapturedError, context: ErrorContext): Promise<void> {
    // 샘플링 체크
    if (!shouldSample(this.config.sampleRate!)) {
      log(this.config.debug!, 'Error not sampled (sample rate:', this.config.sampleRate, ')');
      return;
    }

    // Payload 생성 - userId 포함!
    const payload: IngestPayload = {
      apiKey: this.config.apiKey,
      error,
      context: {
        ...context,
        sessionId: this.errorCapture.getSessionId(),
        userId: this.getCurrentUserId(),  // 사용자 ID 추가
      },
    };

    // 세션 리플레이 추가
    if (this.sessionReplay && this.config.enableSessionReplay) {
      payload.sessionReplay = {
        sessionId: this.sessionReplay.getSessionId(),
        durationMs: this.sessionReplay.getDuration(),
        events: this.sessionReplay.getEvents(),
      };
    }

    // 서버로 전송
    await this.transport.send(payload);
  }

  /**
   * 수동으로 에러 캡처
   */
  captureError(error: Error | string, additionalInfo?: any): void {
    const errorObj = typeof error === 'string' ? new Error(error) : error;
    this.errorCapture.captureError(errorObj, additionalInfo);
  }

  /**
   * 메시지 캡처 (에러가 아닌 정보성 메시지)
   */
  captureMessage(message: string, level: 'info' | 'warning' | 'error' = 'info'): void {
    const error = new Error(message);
    error.name = level.charAt(0).toUpperCase() + level.slice(1);
    this.errorCapture.captureError(error);
  }

  /**
   * 사용자 정보 설정
   */
  setUser(user: UserInfo | null): void {
    this.config.user = user || undefined;
    log(this.config.debug!, 'User set:', user);
  }

  /**
   * 컨텍스트 설정 (태그 등)
   */
  setContext(key: string, value: any): void {
    if (!this.config.user) {
      this.config.user = {};
    }
    this.config.user[key] = value;
    log(this.config.debug!, `Context set: ${key} =`, value);
  }

  /**
   * 세션 리플레이 초기화
   */
  clearReplay(): void {
    if (this.sessionReplay) {
      this.sessionReplay.clear();
      log(this.config.debug!, 'Session replay cleared');
    }
  }

  /**
   * SDK 종료
   */
  close(): void {
    this.errorCapture.stop();
    if (this.sessionReplay) {
      this.sessionReplay.stop();
    }
    this.initialized = false;
    log(this.config.debug!, 'BugShot closed');
  }
}
