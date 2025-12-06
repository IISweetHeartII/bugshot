/**
 * 유틸리티 함수들
 */

import type { BrowserInfo, DeviceInfo } from './types';

/**
 * UUID 생성
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * 브라우저 정보 추출
 */
export function getBrowserInfo(): BrowserInfo {
  const ua = navigator.userAgent;
  let browser = 'Unknown';
  let version = 'Unknown';
  let os = 'Unknown';

  // Browser detection
  if (ua.includes('Chrome')) {
    browser = 'Chrome';
    version = ua.match(/Chrome\/(\d+)/)?.[1] || 'Unknown';
  } else if (ua.includes('Firefox')) {
    browser = 'Firefox';
    version = ua.match(/Firefox\/(\d+)/)?.[1] || 'Unknown';
  } else if (ua.includes('Safari') && !ua.includes('Chrome')) {
    browser = 'Safari';
    version = ua.match(/Version\/(\d+)/)?.[1] || 'Unknown';
  } else if (ua.includes('Edge')) {
    browser = 'Edge';
    version = ua.match(/Edge\/(\d+)/)?.[1] || 'Unknown';
  }

  // OS detection
  if (ua.includes('Windows')) os = 'Windows';
  else if (ua.includes('Mac')) os = 'macOS';
  else if (ua.includes('Linux')) os = 'Linux';
  else if (ua.includes('Android')) os = 'Android';
  else if (ua.includes('iOS')) os = 'iOS';

  return { name: browser, version, os };
}

/**
 * 디바이스 정보 추출
 */
export function getDeviceInfo(): DeviceInfo {
  const width = window.innerWidth;
  const height = window.innerHeight;

  let type: 'mobile' | 'tablet' | 'desktop' = 'desktop';
  if (width <= 768) type = 'mobile';
  else if (width <= 1024) type = 'tablet';

  return {
    type,
    viewport: { width, height },
  };
}

/**
 * 스택 트레이스 파싱
 */
export function parseStackTrace(error: Error): {
  file?: string;
  line?: number;
  column?: number;
  method?: string;
} {
  if (!error.stack) return {};

  const stackLines = error.stack.split('\n');
  const firstLine = stackLines[1] || stackLines[0];

  // Chrome/Firefox 형식: at functionName (file.js:line:column)
  const chromeMatch = firstLine.match(/at\s+(.+?)\s+\((.+?):(\d+):(\d+)\)/);
  if (chromeMatch) {
    return {
      method: chromeMatch[1],
      file: chromeMatch[2],
      line: parseInt(chromeMatch[3]),
      column: parseInt(chromeMatch[4]),
    };
  }

  // Firefox 형식: functionName@file.js:line:column
  const firefoxMatch = firstLine.match(/(.+?)@(.+?):(\d+):(\d+)/);
  if (firefoxMatch) {
    return {
      method: firefoxMatch[1],
      file: firefoxMatch[2],
      line: parseInt(firefoxMatch[3]),
      column: parseInt(firefoxMatch[4]),
    };
  }

  return {};
}

/**
 * 샘플링 여부 결정
 */
export function shouldSample(sampleRate: number): boolean {
  return Math.random() < sampleRate;
}

/**
 * 안전한 JSON stringify
 */
export function safeStringify(obj: any): string {
  try {
    return JSON.stringify(obj);
  } catch (error) {
    return '{}';
  }
}

/**
 * 로그 (디버그 모드에서만)
 */
export function log(debug: boolean, ...args: any[]): void {
  if (debug) {
    console.log('[BugShot]', ...args);
  }
}
