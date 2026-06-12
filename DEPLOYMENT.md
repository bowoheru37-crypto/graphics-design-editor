# 🚀 Deployment Guide

## Prerequisites

- Docker & Docker Compose
- Node.js 18+
- Git
- AWS Account (for S3, CloudFront)
- Domain name

---

## Local Development

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/creativity-platform.git
cd creativity-platform
```

### 2. Setup Environment
```bash
# Create .env.local
cp .env.example .env.local

# Edit with your values
nano .env.local
```

### 3. Install Dependencies
```bash
# Frontend
cd frontend
npm install

# Backend
cd ../backend
npm install
```

### 4. Start Development
```bash
# Frontend
cd frontend
npm run dev

# Backend (in new terminal)
cd backend
npm run dev
```

---

## Docker Deployment

### 1. Build Images
```bash
docker-compose build
```

### 2. Start Services
```bash
docker-compose up -d
```

### 3. Initialize Database
```bash
docker-compose exec backend npm run migrate
```

### 4. View Logs
```bash
docker-compose logs -f
```

---

## Production Deployment (AWS)

### 1. Setup EC2 Instance
```bash
# Connect to instance
ssh -i key.pem ec2-user@instance-ip

# Install Docker
sudo yum update -y
sudo yum install docker -y
sudo systemctl start docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Deploy Application
```bash
# Clone repo
git clone <repo-url>
cd creativity-platform

# Copy environment
cp .env.production .env

# Start services
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 3. Setup SSL/TLS
```bash
# Using Let's Encrypt
sudo docker run -it --rm -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d yourdomain.com -d www.yourdomain.com
```

### 4. Configure Nginx
Update `docker/nginx.conf` with SSL configuration and restart

---

## CI/CD Pipeline (GitHub Actions)

### 1. Create Workflow
```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      
      - name: Install & Build
        run: |
          npm install
          npm run build
      
      - name: Deploy
        run: |
          # Deploy commands
```

---

## Database Backup

### 1. Backup MongoDB
```bash
docker exec creativity-mongodb mongodump \
  --out /backup/$(date +%Y%m%d_%H%M%S)
```

### 2. Restore MongoDB
```bash
docker exec creativity-mongodb mongorestore \
  --dir /backup/20240101_000000
```

---

## Monitoring

### 1. Setup Sentry
```javascript
// Monitor errors
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.NODE_ENV,
});
```

### 2. Setup DataDog
```bash
# Docker Compose
services:
  datadog:
    image: gcr.io/datadog/agent:latest
    environment:
      DD_API_KEY: ${DD_API_KEY}
```

---

## Scaling

### 1. Load Balancer
```nginx
upstream backend {
  server backend1:3000;
  server backend2:3000;
  server backend3:3000;
}
```

### 2. Database Replication
```bash
# MongoDB Replica Set
mongod --replSet rs0 --bind_ip localhost,127.0.0.1
```

---

## Health Checks

### 1. API Health
```bash
curl https://api.creativity.dev/health
```

### 2. Database Health
```bash
docker-compose exec mongodb mongosh \
  --eval "db.adminCommand('ping')"
```

---

## Performance Optimization

### 1. Image Compression
```bash
# Optimize images
for img in frontend/public/images/*; do
  imagemin "$img" --out-dir=frontend/public/images/optimized
done
```

### 2. CDN Setup (Cloudflare)
- Add domain to Cloudflare
- Enable caching
- Setup WAF rules

---

## Troubleshooting

### Services won't start
```bash
docker-compose logs
docker-compose restart
```

### Database connection issues
```bash
docker-compose exec mongodb mongosh
```

### Memory issues
```bash
docker stats
docker system prune
```
