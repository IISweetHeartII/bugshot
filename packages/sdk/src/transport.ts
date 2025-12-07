/**
 * BugShot Transport - 서버로 데이터 전송
 */

import type { IngestPayload } from './types';
import { log } from './utils';

export class Transport {
  private endpoint: string;
  private debug: boolean;
  private retryQueue: IngestPayload[] = [];
  private maxRetries = 3;

  constructor(endpoint: string, debug: boolean = false) {
    this.endpoint = endpoint;
    this.debug = debug;
  }

  /**
   * 에러 데이터 전송
   */
  async send(payload: IngestPayload): Promise<boolean> {
    try {
      log(this.debug, 'Sending error to BugShot...', payload);

      const response = await fetch(`${this.endpoint}/api/ingest`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'omit', // CORS: 쿠키/인증 정보 제외 (API Key로 인증)
      });

      if (response.ok) {
        log(this.debug, 'Error sent successfully');
        return true;
      } else {
        log(this.debug, 'Failed to send error:', response.status);

        // Retry on server errors (5xx)
        if (response.status >= 500) {
          this.queueRetry(payload);
        }

        return false;
      }
    } catch (error) {
      log(this.debug, 'Network error while sending:', error);
      this.queueRetry(payload);
      return false;
    }
  }

  /**
   * 재시도 큐에 추가
   */
  private queueRetry(payload: IngestPayload): void {
    if (this.retryQueue.length < 10) {
      this.retryQueue.push(payload);
      log(this.debug, 'Queued for retry. Queue size:', this.retryQueue.length);

      // 5초 후 재시도
      setTimeout(() => this.processRetryQueue(), 5000);
    }
  }

  /**
   * 재시도 큐 처리
   */
  private async processRetryQueue(): Promise<void> {
    if (this.retryQueue.length === 0) return;

    const payload = this.retryQueue.shift();
    if (payload) {
      const success = await this.send(payload);
      if (!success && this.retryQueue.length < this.maxRetries) {
        // 재시도 실패 시 다시 큐에 추가
        this.queueRetry(payload);
      }
    }
  }

  /**
   * Beacon API를 사용한 전송 (페이지 언로드 시)
   */
  sendBeacon(payload: IngestPayload): boolean {
    try {
      if (navigator.sendBeacon) {
        const blob = new Blob([JSON.stringify(payload)], {
          type: 'application/json',
        });

        return navigator.sendBeacon(`${this.endpoint}/api/ingest`, blob);
      }
      return false;
    } catch (error) {
      log(this.debug, 'Beacon send failed:', error);
      return false;
    }
  }
}
