/**
 * ErrorWatch React Error Boundary
 */

import React, { Component, ReactNode, ErrorInfo } from 'react';
import ErrorWatch from '@errorwatch/browser-sdk';

interface ErrorBoundaryProps {
  /**
   * 자식 컴포넌트
   */
  children: ReactNode;

  /**
   * 에러 발생 시 보여줄 폴백 UI
   */
  fallback?: ReactNode | ((error: Error, errorInfo: ErrorInfo) => ReactNode);

  /**
   * 에러 발생 시 호출될 콜백
   */
  onError?: (error: Error, errorInfo: ErrorInfo) => void;

  /**
   * ErrorWatch로 전송할지 여부 (기본값: true)
   */
  reportToErrorWatch?: boolean;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

/**
 * React Error Boundary for ErrorWatch
 *
 * @example
 * ```tsx
 * import { ErrorBoundary } from '@errorwatch/react';
 *
 * function App() {
 *   return (
 *     <ErrorBoundary fallback={<div>Something went wrong</div>}>
 *       <YourApp />
 *     </ErrorBoundary>
 *   );
 * }
 * ```
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    this.setState({ errorInfo });

    // ErrorWatch로 전송
    if (this.props.reportToErrorWatch !== false) {
      ErrorWatch.captureError(error, {
        type: 'React Error Boundary',
        componentStack: errorInfo.componentStack,
      });
    }

    // 커스텀 콜백 실행
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  render(): ReactNode {
    if (this.state.hasError) {
      const { fallback } = this.props;
      const { error, errorInfo } = this.state;

      // Fallback이 함수인 경우
      if (typeof fallback === 'function' && error && errorInfo) {
        return fallback(error, errorInfo);
      }

      // Fallback이 ReactNode인 경우
      if (fallback) {
        return fallback;
      }

      // 기본 폴백 UI
      return (
        <div
          style={{
            padding: '20px',
            margin: '20px',
            border: '1px solid #f44336',
            borderRadius: '4px',
            backgroundColor: '#ffebee',
          }}
        >
          <h2 style={{ color: '#d32f2f', margin: '0 0 10px 0' }}>
            Oops! Something went wrong
          </h2>
          <p style={{ color: '#666', margin: '0 0 10px 0' }}>
            We've been notified and are working on it.
          </p>
          {error && (
            <details style={{ marginTop: '10px' }}>
              <summary style={{ cursor: 'pointer', color: '#666' }}>
                Error details
              </summary>
              <pre
                style={{
                  marginTop: '10px',
                  padding: '10px',
                  backgroundColor: '#f5f5f5',
                  borderRadius: '4px',
                  overflow: 'auto',
                  fontSize: '12px',
                }}
              >
                {error.toString()}
                {errorInfo && errorInfo.componentStack}
              </pre>
            </details>
          )}
        </div>
      );
    }

    return this.props.children;
  }
}
