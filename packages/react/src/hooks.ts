/**
 * BugShot React Hooks
 */

import { useCallback } from 'react';
import BugShot from '@bugshot/browser-sdk';

/**
 * BugShot Hook
 * 에러 캡처 함수를 제공하는 Hook
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { captureError, captureMessage } = useBugShot();
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
export function useBugShot() {
  const captureError = useCallback((error: Error | string, additionalInfo?: any) => {
    BugShot.captureError(error, additionalInfo);
  }, []);

  const captureMessage = useCallback(
    (message: string, level?: 'info' | 'warning' | 'error') => {
      BugShot.captureMessage(message, level);
    },
    []
  );

  const setUser = useCallback((user: any) => {
    BugShot.setUser(user);
  }, []);

  const setContext = useCallback((key: string, value: any) => {
    BugShot.setContext(key, value);
  }, []);

  return {
    captureError,
    captureMessage,
    setUser,
    setContext,
  };
}
