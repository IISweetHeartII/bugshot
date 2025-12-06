/**
 * BugShot Error Capture System
 */

import type { CapturedError, ErrorContext, BugShotConfig } from './types';
import { getBrowserInfo, getDeviceInfo, parseStackTrace, generateUUID } from './utils';

export class ErrorCapture {
  private config: BugShotConfig;
  private sessionId: string;
  private listeners: Array<(error: CapturedError, context: ErrorContext) => void> = [];
  private originalErrorHandler: OnErrorEventHandler = null;
  private originalRejectionHandler: ((event: PromiseRejectionEvent) => void) | null = null;

  constructor(config: BugShotConfig) {
    this.config = config;
    this.sessionId = generateUUID();
  }

  /**
   * 자동 에러 캡처 시작
   */
  start(): void {
    if (!this.config.enableAutoCapture) return;

    // window.onerror 핸들러
    this.originalErrorHandler = window.onerror;
    window.onerror = (message, source, lineno, colno, error) => {
      this.captureError(error || new Error(String(message)), {
        file: source,
        line: lineno,
        column: colno,
      });

      // 원래 핸들러 호출
      if (this.originalErrorHandler) {
        return this.originalErrorHandler(message, source, lineno, colno, error);
      }
      return false;
    };

    // Unhandled Promise Rejection
    this.originalRejectionHandler = window.onunhandledrejection;
    window.addEventListener('unhandledrejection', (event: PromiseRejectionEvent) => {
      const error = event.reason instanceof Error
        ? event.reason
        : new Error(String(event.reason));

      this.captureError(error, {
        type: 'UnhandledPromiseRejection',
      });

      // 원래 핸들러 호출
      if (this.originalRejectionHandler) {
        this.originalRejectionHandler(event);
      }
    });

    // Console.error 캡처 (선택적)
    if (this.config.debug) {
      const originalConsoleError = console.error;
      console.error = (...args: any[]) => {
        this.captureError(new Error(args.join(' ')), {
          type: 'ConsoleError',
        });
        originalConsoleError.apply(console, args);
      };
    }
  }

  /**
   * 자동 에러 캡처 중지
   */
  stop(): void {
    if (this.originalErrorHandler !== null) {
      window.onerror = this.originalErrorHandler;
      this.originalErrorHandler = null;
    }

    if (this.originalRejectionHandler !== null) {
      window.removeEventListener('unhandledrejection', this.originalRejectionHandler as any);
      this.originalRejectionHandler = null;
    }
  }

  /**
   * 에러 캡처 (수동)
   */
  captureError(
    error: Error,
    additionalInfo?: Partial<CapturedError>
  ): void {
    const stackInfo = parseStackTrace(error);

    const capturedError: CapturedError = {
      type: error.name || 'Error',
      message: error.message || 'Unknown error',
      stackTrace: error.stack,
      file: additionalInfo?.file || stackInfo.file,
      line: additionalInfo?.line || stackInfo.line,
      column: additionalInfo?.column || stackInfo.column,
      method: additionalInfo?.method || stackInfo.method,
    };

    const context: ErrorContext = {
      url: window.location.href,
      httpMethod: 'GET', // Could be enhanced
      userAgent: navigator.userAgent,
      sessionId: this.sessionId,
      timestamp: new Date().toISOString(),
      browserInfo: getBrowserInfo(),
      deviceInfo: getDeviceInfo(),
    };

    // beforeSend 콜백 실행
    let finalError = capturedError;
    if (this.config.beforeSend) {
      const result = this.config.beforeSend(capturedError);
      if (result === null) {
        return; // 에러 전송 취소
      }
      finalError = result;
    }

    // 리스너들에게 알림
    this.listeners.forEach((listener) => {
      try {
        listener(finalError, context);
      } catch (err) {
        console.error('Error in BugShot listener:', err);
      }
    });
  }

  /**
   * 에러 리스너 등록
   */
  onError(listener: (error: CapturedError, context: ErrorContext) => void): void {
    this.listeners.push(listener);
  }

  /**
   * 세션 ID 가져오기
   */
  getSessionId(): string {
    return this.sessionId;
  }
}
