/**
 * ErrorWatch React Hooks
 */

import { useCallback } from 'react';
import ErrorWatch from '@errorwatch/browser-sdk';

/**
 * ErrorWatch Hook
 * 에러 캡처 함수를 제공하는 Hook
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { captureError, captureMessage } = useErrorWatch();
 *
 *   const handleClick = async () => {
 *     try {
 *       await fetchData();
 *     } catch (error) {
 *       captureError(error);
 *     }
 *   };
 *
 *   return <button onClick={handleClick}>Click me</button>;
 * }
 * ```
 */
export function useErrorWatch() {
  const captureError = useCallback((error: Error | string, additionalInfo?: any) => {
    ErrorWatch.captureError(error, additionalInfo);
  }, []);

  const captureMessage = useCallback(
    (message: string, level?: 'info' | 'warning' | 'error') => {
      ErrorWatch.captureMessage(message, level);
    },
    []
  );

  const setUser = useCallback((user: any) => {
    ErrorWatch.setUser(user);
  }, []);

  const setContext = useCallback((key: string, value: any) => {
    ErrorWatch.setContext(key, value);
  }, []);

  return {
    captureError,
    captureMessage,
    setUser,
    setContext,
  };
}
