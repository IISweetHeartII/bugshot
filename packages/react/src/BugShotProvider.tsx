/**
 * BugShot React Provider
 */

import React, { ReactNode, useEffect } from 'react';
import BugShot, { BugShotConfig } from '@bugshot/browser-sdk';

interface BugShotProviderProps {
  config: BugShotConfig;
  children: ReactNode;
}

/**
 * BugShot Provider Component
 *
 * @example
 * ```tsx
 * import { BugShotProvider } from '@bugshot/react';
 *
 * function App() {
 *   return (
 *     <BugShotProvider config={{ apiKey: 'your-api-key' }}>
 *       <YourApp />
 *     </BugShotProvider>
 *   );
 * }
 * ```
 */
export function BugShotProvider({ config, children }: BugShotProviderProps): JSX.Element {
  useEffect(() => {
    // BugShot 초기화
    BugShot.init(config);

    // Cleanup
    return () => {
      BugShot.close();
    };
  }, [config.apiKey]); // API 키 변경 시 재초기화

  return <>{children}</>;
}
