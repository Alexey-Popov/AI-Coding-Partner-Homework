# 🐳 Docker Quick Reference

## Quick Start (TL;DR)

```bash
# 1. Setup
cp .env.example .env
nano .env  # Set JWT_SECRET

# 2. Deploy
docker compose --profile prod up -d --build

# 3. Access
open http://localhost
```

## Common Commands

```bash
# Start production
docker compose --profile prod up -d --build

# Start development (with H2 console)
docker compose --profile dev up -d --build

# Stop
docker compose down

# View logs
docker compose logs -f backend

# Restart
docker compose --profile prod restart

# Check status
docker compose ps

# Check health
curl http://localhost/health
```

## Access Points

| Service | URL | Notes |
|---------|-----|-------|
| Frontend | http://localhost | React UI |
| API Docs | http://localhost/docs | Swagger UI |
| Health | http://localhost/health | Health check |
| H2 Console | http://localhost:8082 | Dev profile only |

## H2 Console Connection (Dev Mode)

```
JDBC URL: jdbc:h2:tcp://h2:9092/bank
Username: sa
Password: (empty or from .env)
```

## Database Management

```bash
# Backup
docker run --rm \
  -v homework-1_h2-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/db-backup.tar.gz -C /data .

# Restore
docker run --rm \
  -v homework-1_h2-data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/db-backup.tar.gz -C /data
```

## Troubleshooting

```bash
# View logs
docker compose logs backend
docker compose logs h2

# Shell into container
docker compose exec backend /bin/bash

# Restart services
docker compose --profile prod restart

# Full reset (⚠️ deletes data)
docker compose down -v
docker compose --profile prod up -d --build
```

## Environment Variables (.env)

```env
# Required
JWT_SECRET=<generate with: python -c "import secrets; print(secrets.token_urlsafe(32))">

# Optional
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=YourStrongPassword123!
CORS_ORIGINS=http://localhost,http://localhost:80
LOG_LEVEL=INFO
```

## Architecture

```
Port 80 → Backend (FastAPI) → Serves UI + API
                ↓
           H2 Database (TCP mode on port 9092)
                ↓
         Docker Volume (persistence)
```

## Files

- `docker-compose.yml` - Orchestration
- `.env` - Configuration (gitignored)
- `.env.example` - Template
- `DOCKER_DEPLOYMENT.md` - Full documentation
- `DOCKER_SUMMARY.md` - Implementation details
