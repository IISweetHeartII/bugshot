# @errorwatch/browser-sdk

Official ErrorWatch SDK for JavaScript/TypeScript applications.

[![npm version](https://badge.fury.io/js/%40errorwatch%2Fbrowser-sdk.svg)](https://www.npmjs.com/package/@errorwatch/browser-sdk)
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
npm install @errorwatch/browser-sdk
```

### Yarn

```bash
yarn add @errorwatch/browser-sdk
```

### CDN

```html
<script src="https://cdn.errorwatch.com/sdk/1.0.0/errorwatch.min.js"></script>
<script>
  ErrorWatch.init({
    apiKey: 'your-api-key-here'
  });
</script>
```

## 🔧 Quick Start

### Basic Setup

```typescript
import ErrorWatch from '@errorwatch/browser-sdk';

ErrorWatch.init({
  apiKey: 'your-api-key-here',
  environment: 'production',
  release: '1.0.0'
});
```

### Configuration Options

```typescript
ErrorWatch.init({
  // Required
  apiKey: 'your-api-key-here',

  // Optional
  endpoint: 'https://api.errorwatch.com', // Default
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
  ErrorWatch.captureError(error);
}
```

### Capture Messages

```typescript
ErrorWatch.captureMessage('User clicked checkout button', 'info');
ErrorWatch.captureMessage('Payment failed', 'error');
```

### Set User Context

```typescript
ErrorWatch.setUser({
  id: '12345',
  email: 'user@example.com',
  username: 'john_doe',
  plan: 'premium'
});
```

### Add Custom Context

```typescript
ErrorWatch.setContext('purchase_id', 'order-123');
ErrorWatch.setContext('cart_value', 99.99);
```

### Vanilla JavaScript

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.errorwatch.com/sdk/1.0.0/errorwatch.min.js"></script>
  <script>
    ErrorWatch.init({
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
ErrorWatch.clearReplay();
```

### Error Filtering

```typescript
ErrorWatch.init({
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
ErrorWatch.init({
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
Initialize ErrorWatch SDK.

**Parameters:**
- `config` (object): Configuration options

**Returns:** `ErrorWatchClient`

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
ErrorWatch.init({
  debug: true,
  apiKey: 'your-api-key'
});
```

## 📝 License

MIT © ErrorWatch Team

## 🔗 Links

- [Documentation](https://docs.errorwatch.com)
- [Dashboard](https://app.errorwatch.com)
- [GitHub](https://github.com/errorwatch/errorwatch)
- [NPM](https://www.npmjs.com/package/@errorwatch/browser-sdk)
