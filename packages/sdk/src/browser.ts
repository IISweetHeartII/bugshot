/**
 * BugShot Browser (IIFE) Bundle
 * CDN 배포용 - window.BugShot로 접근
 */

import { init, captureError, captureMessage, setUser, setContext, close } from './index';

// Global API
const BugShot = {
  init,
  captureError,
  captureMessage,
  setUser,
  setContext,
  close,
};

// Export to window
if (typeof window !== 'undefined') {
  (window as any).BugShot = BugShot;
}

export default BugShot;
