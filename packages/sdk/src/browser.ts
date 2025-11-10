/**
 * ErrorWatch Browser (IIFE) Bundle
 * CDN 배포용 - window.ErrorWatch로 접근
 */

import { init, captureError, captureMessage, setUser, setContext, close } from './index';

// Global API
const ErrorWatch = {
  init,
  captureError,
  captureMessage,
  setUser,
  setContext,
  close,
};

// Export to window
if (typeof window !== 'undefined') {
  (window as any).ErrorWatch = ErrorWatch;
}

export default ErrorWatch;
