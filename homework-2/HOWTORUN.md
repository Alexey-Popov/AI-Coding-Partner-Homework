# How to Run

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

Check versions:
```bash
java -version
mvn -version
```

## Build and Run

### 1. Build the project
```bash
cd homework-2
mvn clean install
```

### 2. Run the application
```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/ticket-system-1.0.0.jar
```

### 3. Verify it's running
```bash
curl http://localhost:8080/tickets
```

Expected: Empty array `[]`

## Run Tests

### All tests
```bash
mvn test
```

### With coverage report
```bash
mvn clean test jacoco:report
```

Open `target/site/jacoco/index.html` in browser to view coverage.

### Specific test class
```bash
mvn test -Dtest=TicketApiTest
mvn test -Dtest=IntegrationTest
```

## Quick API Test

### Create a ticket
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "test@example.com",
    "customerName": "Test User",
    "subject": "Test Ticket",
    "description": "This is a test ticket for verification"
  }'
```

### Import sample data
```bash
# CSV
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.csv" \
  -F "format=csv"

# JSON
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.json" \
  -F "format=json"

# XML
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.xml" \
  -F "format=xml"
```

### List all tickets
```bash
curl http://localhost:8080/tickets
```

### Filter tickets
```bash
# By category
curl "http://localhost:8080/tickets?category=TECHNICAL_ISSUE"

# By priority  
curl "http://localhost:8080/tickets?priority=URGENT"

# Combined
curl "http://localhost:8080/tickets?category=BUG_REPORT&priority=HIGH"
```

## Troubleshooting

### Port 8080 already in use
Change port in `src/main/resources/application.properties`:
```properties
server.port=8081
```

### Tests failing
```bash
# Clean and rebuild
mvn clean install -DskipTests

# Run tests again
mvn test
```

### H2 Database console (for debugging)
Access at: http://localhost:8080/h2-console

Settings:
- JDBC URL: `jdbc:h2:mem:ticketdb`
- Username: `sa`
- Password: (leave empty)

## Stop the Application

Press `Ctrl+C` in the terminal where the app is running.

## Clean Build Artifacts

```bash
mvn clean
```
