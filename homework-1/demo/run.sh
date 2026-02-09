#!/usr/bin/env bash
set -euo pipefail

# Run Spring Boot app from project root using Gradle
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."
cd "$PROJECT_ROOT"

# Prefer Gradle wrapper if present; fall back to system Gradle
if [[ -x "./gradlew" ]]; then
  echo "Using Gradle wrapper..."
  ./gradlew bootRun
elif command -v gradle >/dev/null 2>&1; then
  echo "Using system Gradle..."
  gradle bootRun
else
  echo "Gradle is not installed. Please install Gradle or generate the wrapper with 'gradle wrapper'." >&2
  exit 1
fi
