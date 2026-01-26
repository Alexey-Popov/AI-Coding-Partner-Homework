# 🐳 Docker Deployment Guide

## Overview

This banking application is production-ready and fully containerized using Docker Compose. The setup follows best practices for security, scalability, and maintainability.

## Architecture

### Mode A: Single Entrypoint (Implemented)

```
┌─────────────────────────────────────────────────┐
│                  Host Machine                    │
│                                                  │
│  Port 80 ──────┐                                │
│                │                                 │
│  ┌─────────────▼────────────────────────────┐  │
│  │         Docker Network (bank-net)         │  │
│  │                                            │  │
│  │  ┌──────────────┐    ┌──────────────┐   │  │
│  │  │   Backend    │◄───┤  H2 Database │   │  │
│  │  │   + UI       │    │  (TCP Mode)  │   │  │
│  │  │ Port 8000    │    │  Port 9092   │   │  │
│  │  └──────────────┘    └──────────────┘   │  │
│  │         │                     │          │  │
│  │    Serves API            Persistent      │  │
│  │    & Frontend              Volume        │  │
│  └────────────────────────────────────────────┘
│                                                  │
│  Volume: h2-data (Database Persistence)         │
└─────────────────────────────────────────────────┘
```

**Components**:
1. **H2 Database Container**:
   - Runs H2 in TCP server mode
   - Persists data to Docker volume `h2-data`
   - No ports exposed to host in production
   - Healthcheck ensures availability

2. **Backend Container**:
   - Multi-stage build: builds frontend, then backend
   - Serves API at `/v1/*`, `/docs`, `/health`
   - Serves frontend static files for all other routes
   - Connects to H2 via private network
   - Runs as non-root user
   - Port 8000 exposed as port 80 on host

3. **Network**:
   - Private bridge network `bank-net`
   - Backend and H2 communicate internally
   - Only port 80 exposed to host

## Prerequisites

- **Docker**: 20.10+
- **Docker Compose**: 2.0+

Install:
```bash
# Verify installation
docker --version
docker compose version
```

## Quick Start

### 1. Environment Setup

```bash
# Copy environment template
cp .env.example .env

# Generate secure JWT secret
python -c "import secrets; print(secrets.token_urlsafe(32))"

# Edit .env
nano .env
```

**Required Environment Variables**:
```env
JWT_SECRET=<your-secure-secret-at-least-32-chars>
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=StrongPassword123!
```

### 2. Start Services

**Production Mode**:
```bash
# Build and start
docker compose --profile prod up -d --build

# Wait for services to be healthy
docker compose ps

# Check logs
docker compose logs -f backend
```

**Development Mode** (includes H2 web console):
```bash
# Start with dev profile
docker compose --profile dev up -d --build

# H2 console available at http://localhost:8082
```

### 3. Verify Deployment

```bash
# Check health
curl http://localhost/health

# Expected response:
# {"status":"healthy","database":"healthy","timestamp":"..."}

# Access UI
open http://localhost

# Access API docs
open http://localhost/docs
```

## Profiles

The docker-compose.yml supports two profiles:

### Production Profile (`--profile prod`)

**Characteristics**:
- ✅ H2 database with no exposed ports
- ✅ Backend serves UI and API on port 80
- ✅ Minimal attack surface
- ✅ Restart policies: `unless-stopped`
- ✅ Health checks enabled
- ✅ Non-root containers

**Usage**:
```bash
docker compose --profile prod up -d --build
```

### Development Profile (`--profile dev`)

**Characteristics**:
- ✅ All production features
- ✅ H2 web console exposed on port 8082
- ✅ Easier debugging and database inspection

**Usage**:
```bash
docker compose --profile dev up -d --build

# Access H2 console
open http://localhost:8082

# Connection details:
# JDBC URL: jdbc:h2:tcp://h2:9092/bank
# Username: sa
# Password: (empty or as set in .env)
```

## Management Commands

### Viewing Status

```bash
# List running containers
docker compose ps

# View all logs
docker compose logs -f

# View backend logs only
docker compose logs -f backend

# View H2 logs
docker compose logs -f h2

# Check resource usage
docker stats
```

### Starting/Stopping

```bash
# Stop services (keeps containers)
docker compose --profile prod stop

# Start stopped services
docker compose --profile prod start

# Restart services
docker compose --profile prod restart

# Stop and remove containers (keeps volumes)
docker compose --profile prod down

# Stop and remove everything including volumes (⚠️ DELETES DATABASE)
docker compose --profile prod down -v
```

### Rebuilding

```bash
# Rebuild after code changes
docker compose --profile prod up -d --build

# Force rebuild without cache
docker compose --profile prod build --no-cache
docker compose --profile prod up -d
```

### Database Management

```bash
# List volumes
docker volume ls

# Inspect database volume
docker volume inspect homework-1_h2-data

# Backup database
docker run --rm \
  -v homework-1_h2-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/h2-backup-$(date +%Y%m%d-%H%M%S).tar.gz -C /data .

# Restore database
docker run --rm \
  -v homework-1_h2-data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/h2-backup-YYYYMMDD-HHMMSS.tar.gz -C /data

# Access database shell (dev mode)
docker compose exec h2 /bin/sh
```

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# JWT Configuration
JWT_SECRET=your-secure-secret-at-least-32-characters-long
JWT_ALG=HS256
JWT_TTL=900

# Database (defaults work for Docker setup)
DB_USER=sa
DB_PASSWORD=

# CORS (adjust for your domain)
CORS_ORIGINS=http://localhost,http://localhost:80,https://yourdomain.com

# Logging
LOG_LEVEL=INFO

# Admin seeding (optional)
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=ChangeThisPassword123!
```

### Customizing docker-compose.yml

**Change exposed port**:
```yaml
services:
  backend:
    ports:
      - "8080:8000"  # Host:Container
```

**Add TLS certificates**:
```yaml
services:
  backend:
    volumes:
      - ./certs:/certs:ro
    environment:
      - SSL_CERT_FILE=/certs/cert.pem
      - SSL_KEY_FILE=/certs/key.pem
```

**Resource limits**:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

## Security Best Practices

### Implemented Security Features

1. **Non-root Containers**:
   - Backend runs as user `appuser` (UID 1000)
   - Principle of least privilege

2. **Network Isolation**:
   - Private bridge network
   - Database not exposed to host in production
   - Only necessary ports exposed

3. **Health Checks**:
   - Backend health check ensures DB connectivity
   - H2 health check ensures TCP server is running
   - Auto-restart on failure

4. **Secret Management**:
   - Secrets loaded from `.env` file
   - `.env` excluded from git
   - No hardcoded credentials

5. **Image Security**:
   - Pinned base images (not `latest`)
   - Minimal images (`python:3.11-slim`, `alpine`)
   - Multi-stage builds reduce attack surface

### Additional Recommendations

1. **Use Docker Secrets** (for Swarm/K8s):
```yaml
secrets:
  jwt_secret:
    external: true

services:
  backend:
    secrets:
      - jwt_secret
```

2. **Run Security Scan**:
```bash
# Scan images for vulnerabilities
docker scan bank-backend:latest
docker scan oscarfonts/h2:2.2.224
```

3. **Enable TLS**:
   - Use reverse proxy (nginx, Traefik, Caddy)
   - Terminate SSL at proxy level
   - Forward to backend over private network

4. **Implement Rate Limiting**:
   - Already implemented in backend (SlowAPI)
   - Consider adding at proxy level too

5. **Regular Updates**:
```bash
# Update base images
docker compose --profile prod pull
docker compose --profile prod up -d --build
```

## Troubleshooting

### Services Won't Start

```bash
# Check logs
docker compose logs backend
docker compose logs h2

# Common issues:
# 1. Port already in use
lsof -i :80  # Check what's using port 80

# 2. Environment variables not set
cat .env  # Verify .env exists and has values

# 3. Build failures
docker compose build --no-cache backend
```

### Database Connection Failures

```bash
# Check H2 is running
docker compose ps h2

# Check health
docker compose exec h2 /bin/sh -c "echo test"

# Verify network
docker network inspect homework-1_bank-net

# Check backend can reach H2
docker compose exec backend ping h2
```

### Frontend Not Loading

```bash
# Check if UI was built
docker compose exec backend ls -la /app/ui/dist

# Rebuild with frontend
docker compose --profile prod up -d --build --no-cache

# Check backend logs for errors
docker compose logs backend | grep -i error
```

### Permission Issues

```bash
# Check volume permissions
docker volume inspect homework-1_h2-data

# Reset volume (⚠️ deletes data)
docker compose down -v
docker compose --profile prod up -d
```

## Performance Optimization

### Reduce Build Time

```bash
# Use BuildKit
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Build with cache
docker compose build
```

### Optimize Images

```bash
# View image sizes
docker images | grep bank

# Multi-stage builds already implemented
# Consider .dockerignore for smaller context
```

### Database Tuning

Add to docker-compose.yml:
```yaml
services:
  h2:
    environment:
      - H2_OPTIONS=-ifNotExists -tcp -tcpAllowOthers -tcpPort 9092 -baseDir /opt/h2-data -cacheSize 65536
```

## Scaling (Future)

### Horizontal Scaling

To scale backend (requires external DB like PostgreSQL):

```yaml
services:
  backend:
    deploy:
      replicas: 3
    
  load-balancer:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
```

### Production Orchestration

For production at scale, consider:
- **Docker Swarm**: Built-in orchestration
- **Kubernetes**: Advanced orchestration
- **Managed Services**: AWS ECS, Google Cloud Run, Azure Container Instances

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and push
        run: |
          docker compose build
          docker compose push
      
      - name: Deploy
        run: |
          ssh user@server 'cd /app && docker compose --profile prod up -d'
```

## Monitoring

### Health Checks

```bash
# Automated health monitoring
watch -n 5 'curl -s http://localhost/health | jq'

# Expected output:
# {
#   "status": "healthy",
#   "database": "healthy",
#   "timestamp": "2026-01-25T21:00:00"
# }
```

### Logging

```bash
# Centralized logging
docker compose logs -f --tail=100

# Export logs
docker compose logs --no-color > logs-$(date +%Y%m%d).txt

# JSON logging (backend uses structured logging)
docker compose logs backend | grep "request_id"
```

### Metrics

Consider adding:
- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **ELK Stack**: Log aggregation and analysis

## Migration from Local to Docker

If you have an existing local database:

```bash
# 1. Stop local services
# 2. Copy database file
cp bank.db.mv.db docker-db-backup.mv.db

# 3. Start Docker services
docker compose --profile prod up -d

# 4. Copy database into volume
docker cp docker-db-backup.mv.db \
  bank-h2:/opt/h2-data/bank.mv.db

# 5. Restart H2
docker compose restart h2
```

## Support

For issues:
1. Check logs: `docker compose logs`
2. Verify environment: `cat .env`
3. Test health: `curl http://localhost/health`
4. Review this guide
5. Check main [README.md](README.md)

## References

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [H2 Database Documentation](https://www.h2database.com/)
- [FastAPI Deployment](https://fastapi.tiangolo.com/deployment/)
- [Main Project README](README.md)
