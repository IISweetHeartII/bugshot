# @errorwatch/react

Official ErrorWatch integration for React applications.

[![npm version](https://badge.fury.io/js/%40errorwatch%2Freact.svg)](https://www.npmjs.com/package/@errorwatch/react)
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
npm install @errorwatch/react @errorwatch/browser-sdk
```

## 🔧 Quick Start

### 1. Basic Setup with Provider

```tsx
import { ErrorWatchProvider, ErrorBoundary } from '@errorwatch/react';

function App() {
  return (
    <ErrorWatchProvider config={{ apiKey: 'your-api-key' }}>
      <ErrorBoundary>
        <YourApp />
      </ErrorBoundary>
    </ErrorWatchProvider>
  );
}
```

### 2. Error Boundary Only

```tsx
import { ErrorBoundary } from '@errorwatch/react';
import ErrorWatch from '@errorwatch/browser-sdk';

// Initialize SDK first
ErrorWatch.init({ apiKey: 'your-api-key' });

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
import { useErrorWatch } from '@errorwatch/react';

function MyComponent() {
  const { captureError, captureMessage, setUser } = useErrorWatch();

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

import { ErrorWatchProvider, ErrorBoundary } from '@errorwatch/react';

export function Providers({ children }) {
  return (
    <ErrorWatchProvider
      config={{
        apiKey: process.env.NEXT_PUBLIC_ERRORWATCH_API_KEY!,
        environment: process.env.NODE_ENV
      }}
    >
      <ErrorBoundary>
        {children}
      </ErrorBoundary>
    </ErrorWatchProvider>
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
import { ErrorWatchProvider, ErrorBoundary } from '@errorwatch/react';

function MyApp({ Component, pageProps }) {
  return (
    <ErrorWatchProvider
      config={{
        apiKey: process.env.NEXT_PUBLIC_ERRORWATCH_API_KEY!,
        environment: process.env.NODE_ENV,
        release: process.env.NEXT_PUBLIC_VERCEL_GIT_COMMIT_SHA
      }}
    >
      <ErrorBoundary>
        <Component {...pageProps} />
      </ErrorBoundary>
    </ErrorWatchProvider>
  );
}

export default MyApp;
```

## 🎯 API Reference

### `<ErrorWatchProvider>`

Provider component to initialize ErrorWatch SDK.

**Props:**
- `config` (ErrorWatchConfig): SDK configuration
- `children` (ReactNode): Child components

```tsx
<ErrorWatchProvider config={{ apiKey: 'your-key' }}>
  <App />
</ErrorWatchProvider>
```

### `<ErrorBoundary>`

React Error Boundary component.

**Props:**
- `children` (ReactNode): Components to monitor
- `fallback` (ReactNode | Function): Fallback UI to show on error
- `onError` (Function): Callback when error occurs
- `reportToErrorWatch` (boolean): Send errors to ErrorWatch (default: true)

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

### `useErrorWatch()`

React Hook for error tracking in functional components.

**Returns:**
- `captureError(error, additionalInfo?)`: Capture an error
- `captureMessage(message, level?)`: Capture a message
- `setUser(user)`: Set user info
- `setContext(key, value)`: Add context

```tsx
const { captureError, captureMessage, setUser, setContext } = useErrorWatch();
```

## 🛠️ TypeScript

Full TypeScript support included:

```tsx
import type { ErrorWatchConfig } from '@errorwatch/react';

const config: ErrorWatchConfig = {
  apiKey: 'your-api-key',
  environment: 'production',
  release: '1.0.0'
};
```

## 📝 License

MIT © ErrorWatch Team

## 🔗 Links

- [Documentation](https://docs.errorwatch.com)
- [Browser SDK](../sdk)
- [GitHub](https://github.com/errorwatch/errorwatch)
