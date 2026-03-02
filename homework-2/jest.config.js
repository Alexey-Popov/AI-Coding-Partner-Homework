module.exports = {
  testEnvironment: 'node',
  rootDir: '.',
  testMatch: ['<rootDir>/tests/test_*.js'],
  testPathIgnorePatterns: ['/node_modules/', '/tests/fixtures/'],
  collectCoverageFrom: ['<rootDir>/src/**/*.js'],
  coveragePathIgnorePatterns: ['/node_modules/'],
  moduleFileExtensions: ['js', 'json'],
  verbose: true,
  testTimeout: 10000
};
