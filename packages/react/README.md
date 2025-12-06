# @bugshot/react

Official BugShot integration for React applications.

[![npm version](https://badge.fury.io/js/%40bugshot%2Freact.svg)](https://www.npmjs.com/package/@bugshot/react)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🚀 Features

- ✅ **Error Boundary** - Catch React component errors
- ✅ **Provider Component** - Easy setup
- ✅ **React Hooks** - Functional components support
- ✅ **TypeScript Support** - Full type definitions
- ✅ **Automatic Error Capture** - Works out of the box
- ✅ **Custom Fallback UI** - Show friendly error messages

## 📦 Installation

```bash
npm install @bugshot/react @bugshot/browser-sdk
```

## 🔧 Quick Start

### 1. Basic Setup with Provider

```tsx
import { BugShotProvider, ErrorBoundary } from '@bugshot/react';

function App() {
  return (
    <BugShotProvider config={{ apiKey: 'your-api-key' }}>
      <ErrorBoundary>
        <YourApp />
      </ErrorBoundary>
    </BugShotProvider>
  );
}
```

### 2. Error Boundary Only

```tsx
import { ErrorBoundary } from '@bugshot/react';
import BugShot from '@bugshot/browser-sdk';

// Initialize SDK first
BugShot.init({ apiKey: 'your-api-key' });

function App() {
  return (
    <ErrorBoundary fallback={<div>Oops! Something went wrong.</div>}>
      <YourApp />
    </ErrorBoundary>
  );
}
```

## 📖 Usage Examples

### Custom Fallback UI

```tsx
<ErrorBoundary
  fallback={(error, errorInfo) => (
    <div>
      <h1>Error Occurred</h1>
      <p>{error.message}</p>
      <button onClick={() => window.location.reload()}>
        Reload Page
      </button>
    </div>
  )}
>
  <YourApp />
</ErrorBoundary>
```

### With Error Callback

```tsx
<ErrorBoundary
  onError={(error, errorInfo) => {
    console.log('Error caught:', error);
    analytics.track('error_occurred', {
      error: error.message,
      componentStack: errorInfo.componentStack
    });
  }}
>
  <YourApp />
</ErrorBoundary>
```

### Using Hooks

```tsx
import { useBugShot } from '@bugshot/react';

function MyComponent() {
  const { captureError, captureMessage, setUser } = useBugShot();

  const handleClick = async () => {
    try {
      await fetchData();
    } catch (error) {
      captureError(error);
    }
  };

  useEffect(() => {
    setUser({
      id: '123',
      email: 'user@example.com'
    });

    captureMessage('Component mounted', 'info');
  }, []);

  return <button onClick={handleClick}>Click me</button>;
}
```

### Next.js App Router

```tsx
// app/providers.tsx
'use client';

import { BugShotProvider, ErrorBoundary } from '@bugshot/react';

export function Providers({ children }) {
  return (
    <BugShotProvider
      config={{
        apiKey: process.env.NEXT_PUBLIC_BUGSHOT_API_KEY!,
        environment: process.env.NODE_ENV
      }}
    >
      <ErrorBoundary>
        {children}
      </ErrorBoundary>
    </BugShotProvider>
  );
}
```

```tsx
// app/layout.tsx
import { Providers } from './providers';

export default function RootLayout({ children }) {
  return (
    <html>
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
```

### Next.js Pages Router

```tsx
// pages/_app.tsx
import { BugShotProvider, ErrorBoundary } from '@bugshot/react';

function MyApp({ Component, pageProps }) {
  return (
    <BugShotProvider
      config={{
        apiKey: process.env.NEXT_PUBLIC_BUGSHOT_API_KEY!,
        environment: process.env.NODE_ENV,
        release: process.env.NEXT_PUBLIC_VERCEL_GIT_COMMIT_SHA
      }}
    >
      <ErrorBoundary>
        <Component {...pageProps} />
      </ErrorBoundary>
    </BugShotProvider>
  );
}

export default MyApp;
```

## 🎯 API Reference

### `<BugShotProvider>`

Provider component to initialize BugShot SDK.

**Props:**
- `config` (BugShotConfig): SDK configuration
- `children` (ReactNode): Child components

```tsx
<BugShotProvider config={{ apiKey: 'your-key' }}>
  <App />
</BugShotProvider>
```

### `<ErrorBoundary>`

React Error Boundary component.

**Props:**
- `children` (ReactNode): Components to monitor
- `fallback` (ReactNode | Function): Fallback UI to show on error
- `onError` (Function): Callback when error occurs
- `reportToBugShot` (boolean): Send errors to BugShot (default: true)

```tsx
<ErrorBoundary
  fallback={<ErrorFallback />}
  onError={(error, errorInfo) => {
    console.log('Error:', error);
  }}
>
  <App />
</ErrorBoundary>
```

### `useBugShot()`

React Hook for error tracking in functional components.

**Returns:**
- `captureError(error, additionalInfo?)`: Capture an error
- `captureMessage(message, level?)`: Capture a message
- `setUser(user)`: Set user info
- `setContext(key, value)`: Add context

```tsx
const { captureError, captureMessage, setUser, setContext } = useBugShot();
```

## 🛠️ TypeScript

Full TypeScript support included:

```tsx
import type { BugShotConfig } from '@bugshot/react';

const config: BugShotConfig = {
  apiKey: 'your-api-key',
  environment: 'production',
  release: '1.0.0'
};
```

## 📝 License

MIT © BugShot Team

## 🔗 Links

- [Documentation](https://docs.bugshot.com)
- [Browser SDK](../sdk)
- [GitHub](https://github.com/bugshot/bugshot)
