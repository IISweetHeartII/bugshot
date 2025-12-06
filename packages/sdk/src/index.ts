/**
 * BugShot Browser SDK
 *
 * @example
 * ```typescript
 * import BugShot from '@bugshot/browser-sdk';
 *
 * BugShot.init({
 *   apiKey: 'your-api-key',
 *   environment: 'production',
 *   release: '1.0.0'
 * });
 * ```
 */

import { BugShotClient } from './client';
import type { BugShotConfig, UserInfo } from './types';

// Singleton instance
let globalClient: BugShotClient | null = null;

/**
 * BugShot SDK 초기화
 */
export function init(config: BugShotConfig): BugShotClient {
  if (globalClient) {
    console.warn('BugShot is already initialized');
    return globalClient;
  }

  globalClient = new BugShotClient(config);
  globalClient.init();

  return globalClient;
}

/**
 * 에러 캡처
 */
export function captureError(error: Error | string, additionalInfo?: any): void {
  if (!globalClient) {
    console.warn('BugShot is not initialized. Call init() first.');
    return;
  }
  globalClient.captureError(error, additionalInfo);
}

/**
 * 메시지 캡처
 */
export function captureMessage(message: string, level?: 'info' | 'warning' | 'error'): void {
  if (!globalClient) {
    console.warn('BugShot is not initialized. Call init() first.');
    return;
  }
  globalClient.captureMessage(message, level);
}

/**
 * 사용자 정보 설정
 */
export function setUser(user: UserInfo | null): void {
  if (!globalClient) {
    console.warn('BugShot is not initialized. Call init() first.');
    return;
  }
  globalClient.setUser(user);
}

/**
 * 컨텍스트 설정
 */
export function setContext(key: string, value: any): void {
  if (!globalClient) {
    console.warn('BugShot is not initialized. Call init() first.');
    return;
  }
  globalClient.setContext(key, value);
}

/**
 * 현재 클라이언트 인스턴스 가져오기
 */
export function getCurrentClient(): BugShotClient | null {
  return globalClient;
}

/**
 * SDK 종료
 */
export function close(): void {
  if (globalClient) {
    globalClient.close();
    globalClient = null;
  }
}

// Named exports
export { BugShotClient };
export type { BugShotConfig, UserInfo };

// Default export (functional API)
export default {
  init,
  captureError,
  captureMessage,
  setUser,
  setContext,
  getCurrentClient,
  close,
};
