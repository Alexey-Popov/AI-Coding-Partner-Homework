# ▶️ How to Run the application

These steps use Java 21 and Gradle 9.

## Prerequisites
- Install Java 21 (e.g., via Homebrew: `brew install openjdk@21`).
- Ensure `JAVA_HOME` points to JDK 21.
- Option A: Use local Gradle 9 to generate the wrapper.
- Option B: Use an existing Gradle 9 installation directly.

## Generate Gradle Wrapper (recommended)
If Gradle is installed locally, generate the wrapper pinned to Gradle 9.3.0:

```bash
gradle wrapper
```

This creates `gradlew` scripts so you can run with a consistent Gradle version.

## Run the application
Using the Gradle wrapper (after generating):

```bash
./gradlew bootRun
```

Or with a local Gradle 9 installation:

```bash
gradle bootRun
```

## Run tests

```bash
./gradlew test
```

## Build a JAR

```bash
./gradlew build
```