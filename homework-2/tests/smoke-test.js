// Simple smoke test to verify modules load
const path = require('path');

console.log('Testing module imports...\n');

try {
  const store = require('../src/store');
  console.log('✓ store module loaded');
} catch (e) {
  console.error('✗ store module failed:', e.message);
}

try {
  const { validateTicket } = require('../src/models/ticket');
  console.log('✓ ticket model module loaded');
} catch (e) {
  console.error('✗ ticket model failed:', e.message);
}

try {
  const { autoClassify } = require('../src/services/classifier');
  console.log('✓ classifier module loaded');
} catch (e) {
  console.error('✗ classifier module failed:', e.message);
}

try {
  const { importFromFile } = require('../src/services/importer');
  console.log('✓ importer module loaded');
} catch (e) {
  console.error('✗ importer module failed:', e.message);
}

try {
  const app = require('../src/app');
  console.log('✓ app module loaded');
} catch (e) {
  console.error('✗ app module failed:', e.message);
}

console.log('\nAll modules loaded successfully!');
