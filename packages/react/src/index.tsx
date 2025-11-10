/**
 * ErrorWatch React Integration
 *
 * @example
 * ```tsx
 * import { ErrorBoundary, ErrorWatchProvider } from '@errorwatch/react';
 *
 * function App() {
 *   return (
 *     <ErrorWatchProvider config={{ apiKey: 'your-api-key' }}>
 *       <ErrorBoundary>
 *         <YourApp />
 *       </ErrorBoundary>
 *     </ErrorWatchProvider>
 *   );
 * }
 * ```
 */

export { ErrorBoundary } from './ErrorBoundary';
export { ErrorWatchProvider } from './ErrorWatchProvider';
export { useErrorWatch } from './hooks';

// Re-export types from SDK
export type { ErrorWatchConfig, UserInfo } from '@errorwatch/browser-sdk';
