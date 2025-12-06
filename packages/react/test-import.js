// React Plugin Import Test
const ReactPlugin = require('./dist/index.cjs.js');

console.log('✅ React plugin imported successfully!');
console.log('Available exports:', Object.keys(ReactPlugin));

// Test components
if (ReactPlugin.ErrorBoundary) {
  console.log('✅ ErrorBoundary component exists');
}
if (ReactPlugin.BugShotProvider) {
  console.log('✅ BugShotProvider component exists');
}
if (typeof ReactPlugin.useBugShot === 'function') {
  console.log('✅ useBugShot hook exists');
}

console.log('\n📦 React plugin is ready to use!');
