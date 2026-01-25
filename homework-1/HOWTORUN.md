# ▶️ How to Run the application

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 21** or higher ([Download here](https://adoptium.net/))
- **Maven 3.6** or higher ([Download here](https://maven.apache.org/download.cgi))


## Step 1: Navigate to Project Directory

```bash
cd /{path}/homework-1
```
---

## Step 2: Build the Project

Clean and compile the project:

```bash
mvn clean package
```

---

## Step 3: Run the Application

### Option 1: Using Maven (Recommended for Development)

```bash
mvn spring-boot:run
```

### Option 2: Open the project via VSCode or IntelliJ and run the TransactionApiApplication class.

Default port is 8080. You can change it in the resources/application.properties file.
