# How to Run

## Prerequisites
- Node.js 16+
- npm 7+

## Install
```bash
cd homework-2
npm install
```

## Start the Server
```bash
npm start
```
Server listens on `http://localhost:3000` by default.

## Run Tests
```bash
npm test
npm run test:coverage
```

## Quick Smoke Test
```bash
curl http://localhost:3000/tickets
```

## Sample Data Import
```bash
curl -X POST "http://localhost:3000/tickets/import?type=csv" \
  -F "file=@sample_data/sample_tickets.csv"
```

## Project Structure
```
homework-2/
  src/
  tests/
  sample_data/
  docs/
  demo/
```
