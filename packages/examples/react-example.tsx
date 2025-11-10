/**
 * ErrorWatch React Example
 *
 * 설치:
 * npm install @errorwatch/react @errorwatch/browser-sdk
 */

import React, { useState } from 'react';
import { ErrorWatchProvider, ErrorBoundary, useErrorWatch } from '@errorwatch/react';

// 1. 기본 사용법
function BasicExample() {
  return (
    <ErrorWatchProvider config={{ apiKey: 'your-api-key' }}>
      <ErrorBoundary>
        <App />
      </ErrorBoundary>
    </ErrorWatchProvider>
  );
}

// 2. 커스텀 폴백 UI
function CustomFallbackExample() {
  return (
    <ErrorBoundary
      fallback={(error, errorInfo) => (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h1>😢 앗! 문제가 발생했습니다</h1>
          <p>서비스 이용에 불편을 드려 죄송합니다.</p>
          <button onClick={() => window.location.reload()}>
            페이지 새로고침
          </button>
          <details style={{ marginTop: '20px' }}>
            <summary>에러 상세 정보</summary>
            <pre>{error.message}</pre>
          </details>
        </div>
      )}
    >
      <App />
    </ErrorBoundary>
  );
}

// 3. Hook 사용 예제
function HookExample() {
  const { captureError, captureMessage, setUser } = useErrorWatch();
  const [data, setData] = useState(null);

  const fetchData = async () => {
    try {
      const response = await fetch('/api/data');
      if (!response.ok) throw new Error('API 요청 실패');
      const result = await response.json();
      setData(result);

      captureMessage('데이터 로드 성공', 'info');
    } catch (error) {
      captureError(error, {
        context: 'data_fetching',
        url: '/api/data'
      });
    }
  };

  const handleLogin = (userId: string) => {
    setUser({
      id: userId,
      email: `${userId}@example.com`,
      plan: 'premium'
    });
  };

  return (
    <div>
      <button onClick={fetchData}>데이터 가져오기</button>
      <button onClick={() => handleLogin('user123')}>로그인</button>
    </div>
  );
}

// 4. Next.js App Router 예제
// app/providers.tsx
'use client';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <ErrorWatchProvider
      config={{
        apiKey: process.env.NEXT_PUBLIC_ERRORWATCH_API_KEY!,
        environment: process.env.NODE_ENV,
        release: process.env.NEXT_PUBLIC_VERCEL_GIT_COMMIT_SHA,
        enableSessionReplay: true
      }}
    >
      <ErrorBoundary
        fallback={
          <div className="error-page">
            <h1>문제가 발생했습니다</h1>
            <p>관리자에게 알림이 전송되었습니다.</p>
          </div>
        }
      >
        {children}
      </ErrorBoundary>
    </ErrorWatchProvider>
  );
}

// app/layout.tsx
import { Providers } from './providers';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}

// 5. 실전 예제: 쇼핑몰 체크아웃
function CheckoutPage() {
  const { captureError, captureMessage, setContext } = useErrorWatch();
  const [loading, setLoading] = useState(false);

  const handleCheckout = async (cartItems: any[], totalAmount: number) => {
    setLoading(true);

    // 컨텍스트 설정
    setContext('cart_items_count', cartItems.length);
    setContext('total_amount', totalAmount);
    setContext('payment_method', 'credit_card');

    try {
      captureMessage('결제 시작', 'info');

      const response = await fetch('/api/checkout', {
        method: 'POST',
        body: JSON.stringify({ items: cartItems, total: totalAmount })
      });

      if (!response.ok) {
        throw new Error(`결제 실패: ${response.statusText}`);
      }

      const result = await response.json();
      captureMessage('결제 성공', 'info');

      // 성공 처리
      window.location.href = '/order-complete';

    } catch (error) {
      captureError(error, {
        context: 'checkout_flow',
        cart_value: totalAmount,
        items_count: cartItems.length
      });

      alert('결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>주문 결제</h1>
      <button
        onClick={() => handleCheckout([], 99.99)}
        disabled={loading}
      >
        {loading ? '처리 중...' : '결제하기'}
      </button>
    </div>
  );
}

// 6. 고급 사용: 에러 콜백
function AdvancedExample() {
  return (
    <ErrorBoundary
      onError={(error, errorInfo) => {
        // 커스텀 분석 도구에도 전송
        if (window.gtag) {
          window.gtag('event', 'exception', {
            description: error.message,
            fatal: true
          });
        }

        // 슬랙 알림 등
        fetch('/api/notify-team', {
          method: 'POST',
          body: JSON.stringify({ error: error.message })
        });
      }}
    >
      <App />
    </ErrorBoundary>
  );
}

function App() {
  return <div>Your App</div>;
}

export default BasicExample;
