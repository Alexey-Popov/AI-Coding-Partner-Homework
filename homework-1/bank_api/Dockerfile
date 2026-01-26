# ================================
# Stage 1: Build Frontend
# ================================
FROM node:20-alpine AS frontend-builder

WORKDIR /ui

# Copy UI package files
COPY ui/package*.json ./

# Install ALL dependencies (including devDependencies needed for build)
RUN npm ci && npm cache clean --force

# Copy UI source
COPY ui/ ./

# Build frontend (static assets)
RUN npm run build

# ================================
# Stage 2: Backend Runtime
# ================================
FROM python:3.11-slim

# Install Java (required for H2 JDBC), curl for healthcheck, and netcat for debugging
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    default-jre-headless \
    curl \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Download H2 JAR (pinned version for reproducibility)
RUN curl -fsSL -o h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar && \
    echo "Verifying H2 JAR..." && \
    test -f h2.jar

# Copy requirements and install Python dependencies
COPY bank_api/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt && \
    pip list

# Copy application code
COPY bank_api/app/ ./app/

# Copy built frontend from stage 1
COPY --from=frontend-builder /ui/dist ./ui/dist

# Create non-root user
RUN useradd -m -u 1000 -s /bin/bash appuser && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Set environment variables
ENV H2_JAR_PATH=/app/h2.jar \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
