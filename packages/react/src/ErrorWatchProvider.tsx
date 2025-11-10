/**
 * ErrorWatch React Provider
 */

import React, { ReactNode, useEffect } from 'react';
import ErrorWatch, { ErrorWatchConfig } from '@errorwatch/browser-sdk';

interface ErrorWatchProviderProps {
  config: ErrorWatchConfig;
  children: ReactNode;
}

/**
 * ErrorWatch Provider Component
 *
 * @example
 * ```tsx
 * import { ErrorWatchProvider } from '@errorwatch/react';
 *
 * function App() {
 *   return (
 *     <ErrorWatchProvider config={{ apiKey: 'your-api-key' }}>
 *       <YourApp />
 *     </ErrorWatchProvider>
 *   );
 * }
 * ```
 */
export function ErrorWatchProvider({ config, children }: ErrorWatchProviderProps): JSX.Element {
  useEffect(() => {
    // ErrorWatch 초기화
    ErrorWatch.init(config);

    // Cleanup
    return () => {
      ErrorWatch.close();
    };
  }, [config.apiKey]); // API 키 변경 시 재초기화

  return <>{children}</>;
}
