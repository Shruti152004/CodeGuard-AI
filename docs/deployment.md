# Production Deployment Guide — CodeGuard AI

Follow this guide to deploy CodeGuard AI in a hardened, production-ready environment.

## 1. Prerequisites
- **Docker** Engine v24+
- **Docker Compose** v2.20+
- A valid **domain name** and SSL certificates
- Production database backups enabled

## 2. Configuration Setup
1. Clone the repository.
2. Copy the configuration template:
   ```bash
   cp .env.example .env
   ```
3. Modify `.env` and assign secure, complex passwords:
   - Generate a strong `JWT_SECRET` (e.g. `openssl rand -base64 64`).
   - Configure a webhook HMAC key.
   - Configure your real Gemini/OpenAI API keys.

## 3. Service Orchestration
To build and spin up the entire cluster in background daemon mode:
```bash
docker compose up --build -d
```

## 4. Verification Checklists
- Verify container health status:
  ```bash
  docker compose ps
  ```
- Inspect logs to confirm Kafka broker subscriptions and database migration completions:
  ```bash
  docker compose logs -f spring-boot-core
  ```

## 5. Security Recommendations
- **Network Isolation**: Restrict host port exposures. Databases (`codeguard-postgres`) and message brokers (`codeguard-kafka`) should not map ports publicly on host production interfaces.
- **Resource Constraints**: Define CPU and memory limitations for each container to prevent memory leaks from starving other services.
