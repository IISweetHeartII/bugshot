// React Plugin Import Test
const ReactPlugin = require('./dist/index.cjs.js');

console.log('✅ React plugin imported successfully!');
console.log('Available exports:', Object.keys(ReactPlugin));

// Test components
if (ReactPlugin.ErrorBoundary) {
  console.log('✅ ErrorBoundary component exists');
}
if (ReactPlugin.ErrorWatchProvider) {
  console.log('✅ ErrorWatchProvider component exists');
}
if (typeof ReactPlugin.useErrorWatch === 'function') {
  console.log('✅ useErrorWatch hook exists');
}

console.log('\n📦 React plugin is ready to use!');
