/**
 * ErrorWatch Session Replay
 * 사용자 행동 녹화 (간단 버전)
 */

import type { SessionReplayEvent } from './types';
import { generateUUID } from './utils';

export class SessionReplay {
  private events: SessionReplayEvent[] = [];
  private sessionId: string;
  private maxEvents = 1000;
  private startTime: number;
  private isRecording = false;

  constructor(sessionId: string) {
    this.sessionId = sessionId;
    this.startTime = Date.now();
  }

  /**
   * 녹화 시작
   */
  start(): void {
    if (this.isRecording) return;
    this.isRecording = true;

    // 클릭 이벤트 기록
    document.addEventListener('click', this.handleClick);

    // 입력 이벤트 기록 (비밀번호 제외)
    document.addEventListener('input', this.handleInput);

    // 페이지 뷰 기록
    this.recordEvent('pageview', {
      url: window.location.href,
      title: document.title,
    });

    // URL 변경 감지 (SPA)
    window.addEventListener('popstate', this.handleNavigation);

    // pushState, replaceState 감지
    this.wrapHistoryMethod('pushState');
    this.wrapHistoryMethod('replaceState');
  }

  /**
   * 녹화 중지
   */
  stop(): void {
    if (!this.isRecording) return;
    this.isRecording = false;

    document.removeEventListener('click', this.handleClick);
    document.removeEventListener('input', this.handleInput);
    window.removeEventListener('popstate', this.handleNavigation);
  }

  /**
   * 클릭 이벤트 핸들러
   */
  private handleClick = (event: MouseEvent): void => {
    const target = event.target as HTMLElement;

    this.recordEvent('click', {
      tagName: target.tagName,
      id: target.id,
      className: target.className,
      text: target.textContent?.substring(0, 100),
      x: event.clientX,
      y: event.clientY,
    });
  };

  /**
   * 입력 이벤트 핸들러
   */
  private handleInput = (event: Event): void => {
    const target = event.target as HTMLInputElement;

    // 비밀번호 필드는 녹화하지 않음
    if (target.type === 'password') return;

    this.recordEvent('input', {
      tagName: target.tagName,
      id: target.id,
      name: target.name,
      type: target.type,
      value: target.value.substring(0, 100), // 최대 100자
    });
  };

  /**
   * 네비게이션 이벤트 핸들러
   */
  private handleNavigation = (): void => {
    this.recordEvent('navigation', {
      url: window.location.href,
      title: document.title,
    });
  };

  /**
   * History API 래핑
   */
  private wrapHistoryMethod(method: 'pushState' | 'replaceState'): void {
    const original = history[method];
    const self = this;

    history[method] = function (...args: any[]) {
      const result = original.apply(this, args);
      self.handleNavigation();
      return result;
    };
  }

  /**
   * 이벤트 기록
   */
  private recordEvent(type: string, data: any): void {
    if (this.events.length >= this.maxEvents) {
      // 오래된 이벤트 제거
      this.events.shift();
    }

    this.events.push({
      type,
      timestamp: Date.now() - this.startTime,
      data,
    });
  }

  /**
   * 녹화된 이벤트 가져오기
   */
  getEvents(): SessionReplayEvent[] {
    return this.events;
  }

  /**
   * 녹화 시간 (ms)
   */
  getDuration(): number {
    return Date.now() - this.startTime;
  }

  /**
   * 세션 ID
   */
  getSessionId(): string {
    return this.sessionId;
  }

  /**
   * 이벤트 초기화
   */
  clear(): void {
    this.events = [];
    this.startTime = Date.now();
  }
}
