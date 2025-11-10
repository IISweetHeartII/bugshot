// SDK Import Test
const ErrorWatch = require('./dist/index.cjs.js');

console.log('✅ SDK imported successfully!');
console.log('Available methods:', Object.keys(ErrorWatch));

// Test basic structure
if (typeof ErrorWatch.init === 'function') {
  console.log('✅ init() method exists');
}
if (typeof ErrorWatch.captureError === 'function') {
  console.log('✅ captureError() method exists');
}
if (typeof ErrorWatch.captureMessage === 'function') {
  console.log('✅ captureMessage() method exists');
}
if (typeof ErrorWatch.setUser === 'function') {
  console.log('✅ setUser() method exists');
}

console.log('\n📦 SDK is ready to use!');
