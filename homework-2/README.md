# Customer Support Ticket System

Production-grade customer support ticket management system built with TypeScript, Express, and Node.js.

## Features

- RESTful API for ticket management
- Automatic ticket categorization and priority assignment
- Multi-format file import support (JSON, CSV, XML)
- Comprehensive test coverage (>85%)
- Type-safe TypeScript implementation

## Prerequisites

- Node.js >= 18.0.0
- npm >= 9.0.0

## Installation

```bash
npm install
```

## Scripts

- `npm run build` - Compile TypeScript to JavaScript
- `npm start` - Run the production server
- `npm run dev` - Run development server with ts-node
- `npm test` - Run tests
- `npm run test:watch` - Run tests in watch mode
- `npm run test:coverage` - Run tests with coverage report
- `npm run lint` - Lint TypeScript files
- `npm run lint:fix` - Fix linting issues automatically
- `npm run format` - Format code with Prettier
- `npm run format:check` - Check code formatting

## Project Structure

```
src/
├── models/         # Data models and type definitions
├── routes/         # Express route definitions
├── controllers/    # Request handlers
├── services/       # Business logic
└── utils/          # Utility functions

tests/              # Test files
docs/               # Documentation
```

## Development

1. Install dependencies: `npm install`
2. Run in development mode: `npm run dev`
3. Run tests: `npm test`

## Code Quality

This project uses:
- **TypeScript** with strict mode enabled
- **ESLint** for code linting
- **Prettier** for code formatting
- **Jest** for testing with >85% coverage requirement

## License

MIT
