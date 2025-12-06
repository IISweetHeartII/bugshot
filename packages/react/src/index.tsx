/**
 * BugShot React Integration
 *
 * @example
 * ```tsx
 * import { ErrorBoundary, BugShotProvider } from '@bugshot/react';
 *
 * function App() {
 *   return (
 *     <BugShotProvider config={{ apiKey: 'your-api-key' }}>
 *       <ErrorBoundary>
 *         <YourApp />
 *       </ErrorBoundary>
 *     </BugShotProvider>
 *   );
 * }
 * ```
 */

export { ErrorBoundary } from './ErrorBoundary';
export { BugShotProvider } from './BugShotProvider';
export { useBugShot } from './hooks';

// Re-export types from SDK
export type { BugShotConfig, UserInfo } from '@bugshot/browser-sdk';
