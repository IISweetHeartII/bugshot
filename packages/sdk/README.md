# @bugshot/browser-sdk

Official BugShot SDK for JavaScript/TypeScript applications.

[![npm version](https://badge.fury.io/js/%40bugshot%2Fbrowser-sdk.svg)](https://www.npmjs.com/package/@bugshot/browser-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🚀 Features

- ✅ **Automatic Error Capture** - Catches all JavaScript errors automatically
- ✅ **Session Replay** - Records user actions before errors occur
- ✅ **Source Maps Support** - De-minify production errors
- ✅ **Breadcrumbs** - Track user interactions leading to errors
- ✅ **Custom Context** - Add tags, user info, and metadata
- ✅ **TypeScript Support** - Full type definitions included
- ✅ **Zero Dependencies** - Lightweight and fast
- ✅ **CDN Support** - Use via CDN without npm

## 📦 Installation

### NPM

```bash
npm install @bugshot/browser-sdk
```

### Yarn

```bash
yarn add @bugshot/browser-sdk
```

### CDN

```html
<script src="https://cdn.bugshot.com/sdk/1.0.0/bugshot.min.js"></script>
<script>
  BugShot.init({
    apiKey: 'your-api-key-here'
  });
</script>
```

## 🔧 Quick Start

### Basic Setup

```typescript
import BugShot from '@bugshot/browser-sdk';

BugShot.init({
  apiKey: 'your-api-key-here',
  environment: 'production',
  release: '1.0.0'
});
```

### Configuration Options

```typescript
BugShot.init({
  // Required
  apiKey: 'your-api-key-here',

  // Optional
  endpoint: 'https://bugshot-api.log8.kr', // Default
  environment: 'production', // 'development', 'staging', etc.
  release: '1.0.0', // Your app version
  enableSessionReplay: true, // Record user sessions
  enableAutoCapture: true, // Auto-capture errors
  sampleRate: 1.0, // 0.0 to 1.0 (100% = capture all errors)
  debug: false, // Enable debug logs

  // Hooks
  beforeSend: (error) => {
    // Modify or filter errors before sending
    if (error.message.includes('ignore')) {
      return null; // Don't send this error
    }
    return error;
  },

  // User info
  user: {
    id: '12345',
    email: 'user@example.com',
    username: 'john_doe'
  }
});
```

## 📖 Usage Examples

### Manual Error Capture

```typescript
try {
  riskyOperation();
} catch (error) {
  BugShot.captureError(error);
}
```

### Capture Messages

```typescript
BugShot.captureMessage('User clicked checkout button', 'info');
BugShot.captureMessage('Payment failed', 'error');
```

### Set User Context

```typescript
BugShot.setUser({
  id: '12345',
  email: 'user@example.com',
  username: 'john_doe',
  plan: 'premium'
});
```

### Add Custom Context

```typescript
BugShot.setContext('purchase_id', 'order-123');
BugShot.setContext('cart_value', 99.99);
```

### Vanilla JavaScript

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.bugshot.com/sdk/1.0.0/bugshot.min.js"></script>
  <script>
    BugShot.init({
      apiKey: 'ew_your_api_key_here',
      environment: 'production'
    });
  </script>
</head>
<body>
  <button onclick="throwError()">Trigger Error</button>

  <script>
    function throwError() {
      throw new Error('Test error from button click!');
    }
  </script>
</body>
</html>
```

## 🎯 Advanced Features

### Session Replay

Session replay is enabled by default and records:
- Click events
- Input changes (excluding passwords)
- Navigation events
- Page views

```typescript
// Clear replay data
BugShot.clearReplay();
```

### Error Filtering

```typescript
BugShot.init({
  beforeSend: (error) => {
    // Ignore errors from extensions
    if (error.file?.includes('chrome-extension://')) {
      return null;
    }

    // Add extra context
    error.customData = {
      timestamp: Date.now(),
      viewport: window.innerWidth + 'x' + window.innerHeight
    };

    return error;
  }
});
```

### Sampling

Reduce data volume by sampling errors:

```typescript
BugShot.init({
  sampleRate: 0.5 // Only send 50% of errors
});
```

## 🔌 Integrations

- [React](../react) - Error Boundary and Hooks
- Vue - Coming soon
- Angular - Coming soon
- Next.js - Coming soon

## 📊 What Gets Captured?

### Automatic Error Capture
- JavaScript runtime errors
- Unhandled Promise rejections
- Network errors (fetch/XHR)
- Resource loading errors

### Context Information
- Browser name and version
- Operating system
- Screen resolution
- User agent
- Page URL
- User interactions (session replay)

## 🛠️ API Reference

### `init(config)`
Initialize BugShot SDK.

**Parameters:**
- `config` (object): Configuration options

**Returns:** `BugShotClient`

### `captureError(error, additionalInfo?)`
Manually capture an error.

**Parameters:**
- `error` (Error | string): Error object or message
- `additionalInfo` (object, optional): Extra context

### `captureMessage(message, level?)`
Capture an informational message.

**Parameters:**
- `message` (string): Message text
- `level` ('info' | 'warning' | 'error'): Severity level

### `setUser(user)`
Set user information.

**Parameters:**
- `user` (object): User data

### `setContext(key, value)`
Add custom context.

**Parameters:**
- `key` (string): Context key
- `value` (any): Context value

### `close()`
Stop error capturing and clean up.

## 🐛 Debugging

Enable debug mode to see SDK logs:

```typescript
BugShot.init({
  debug: true,
  apiKey: 'your-api-key'
});
```

## 📝 License

MIT © BugShot Team

## 🔗 Links

- [Documentation](https://docs.bugshot.com)
- [Dashboard](https://app.bugshot.com)
- [GitHub](https://github.com/bugshot/bugshot)
- [NPM](https://www.npmjs.com/package/@bugshot/browser-sdk)
