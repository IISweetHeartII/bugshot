// SDK Import Test
const BugShot = require('./dist/index.cjs.js');

console.log('✅ SDK imported successfully!');
console.log('Available methods:', Object.keys(BugShot));

// Test basic structure
if (typeof BugShot.init === 'function') {
  console.log('✅ init() method exists');
}
if (typeof BugShot.captureError === 'function') {
  console.log('✅ captureError() method exists');
}
if (typeof BugShot.captureMessage === 'function') {
  console.log('✅ captureMessage() method exists');
}
if (typeof BugShot.setUser === 'function') {
  console.log('✅ setUser() method exists');
}

console.log('\n📦 SDK is ready to use!');
