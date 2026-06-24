# 🚗 MyGarage Backend — Docker Guide

Stack: **Node.js + Express + TypeScript + Prisma 7 + PostgreSQL 16**

---

## 1. Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/install/)
- `curl` or [Postman](https://www.postman.com/) / [Insomnia](https://insomnia.rest/) for testing

---

## 2. Quick Start

```bash
# 1. Copy the Docker env file (or edit it first)
cp .env.docker .env

# 2. Build & start everything (PostgreSQL + API)
docker compose up --build -d

# 3. Check logs to confirm everything is up
docker compose logs -f api
```

You should see:
```
🔄 Waiting for PostgreSQL to be ready...
✅ PostgreSQL is ready.
🔧 Running database migrations...
🚗 MyGarage API running on http://localhost:3000
```

---

## 3. Useful Docker Commands

| Command | What it does |
|---|---|
| `docker compose up --build -d` | Build images & start containers in the background |
| `docker compose down` | Stop all containers |
| `docker compose down -v` | Stop containers **and wipe the database volume** |
| `docker compose logs -f api` | Follow API logs |
| `docker compose logs postgres` | View PostgreSQL logs |
| `docker compose restart api` | Restart only the API |
| `docker compose ps` | See running containers |
| `docker compose exec postgres psql -U postgres -d mygarage` | Open a `psql` shell in the database |

---

## 4. API Endpoints

All endpoints are prefixed with `http://localhost:3000/api`.

### 4.1 Health Check (public)

```bash
curl http://localhost:3000/api/health
```

**Response:**
```json
{ "status": "ok", "timestamp": "2026-06-22T..." }
```

---

### 4.2 Auth — Register

Creates a new user and returns a JWT token.

```bash
curl -s -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "secret123"}' | jq .
```

**Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "a1b2c3d4-...",
    "email": "john@example.com"
  }
}
```

**Errors:**
| Status | Message | Cause |
|---|---|---|
| 400 | `email and password are required` | Missing body fields |
| 409 | `A user with this email already exists` | Duplicate email |

---

### 4.3 Auth — Login

Authenticates an existing user and returns a JWT.

```bash
curl -s -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "secret123"}' | jq .
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "a1b2c3d4-...",
    "email": "john@example.com"
  }
}
```

**Errors:**
| Status | Message |
|---|---|
| 400 | `email and password are required` |
| 401 | `Invalid email or password` |

> **💡 Save the token** for the sync endpoints:
> ```bash
> export TOKEN="eyJhbGciOiJIUzI1NiIs..."
> ```

---

### 4.4 Sync — Pull (authenticated)

Fetches all records changed since a given timestamp.  
If `lastSyncTimestamp` is omitted, returns **all** records for the user.

```bash
# Full pull (all records)
curl -s http://localhost:3000/api/sync/pull \
  -H "Authorization: Bearer $TOKEN" | jq .

# Incremental pull (only records since a date)
curl -s "http://localhost:3000/api/sync/pull?lastSyncTimestamp=2026-01-01T00:00:00.000Z" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Response (200):**
```json
{
  "vehicles": [],
  "services": [],
  "parts": [],
  "pieces": [],
  "servicePieceCrossRefs": []
}
```

**Errors:**
| Status | Message |
|---|---|
| 401 | `Missing or malformed Authorization header` |
| 401 | `Invalid or expired token` |

---

### 4.5 Sync — Push (authenticated)

Upserts vehicle, service, part, piece, and cross-reference records.  
The backend **forces** `userId` from the token — you cannot write another user's data.

```bash
curl -s -X POST http://localhost:3000/api/sync/push \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicles": [
      {
        "id": "11111111-1111-1111-1111-111111111111",
        "plate": "ABC-1234",
        "name": "Toyota Corolla",
        "year": "2022",
        "mileage": "45000",
        "mileageKm": 72420.0,
        "owner": "John Doe",
        "fuelType": "Gasoline",
        "engineCapacity": "1.8L",
        "localImageFileNames": [],
        "updatedAt": "2026-06-22T12:00:00.000Z"
      }
    ],
    "services": [],
    "parts": [],
    "pieces": [],
    "servicePieceCrossRefs": []
  }' | jq .
```

**Response (200):**
```json
{ "ok": true }
```

**Errors:**
| Status | Message |
|---|---|
| 401 | `Missing or malformed Authorization header` |
| 401 | `Invalid or expired token` |

---

## 5. Full End-to-End Test Script

Save this as `test.sh`, make it executable (`chmod +x test.sh`), then run:

```bash
#!/bin/bash
set -e

BASE="http://localhost:3000/api"

echo "=== 1. Health Check ==="
curl -s "$BASE/health" | jq .

echo ""
echo "=== 2. Register User ==="
REGISTER=$(curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@mygarage.dev","password":"testpass123"}')
echo "$REGISTER" | jq .
TOKEN=$(echo "$REGISTER" | jq -r '.token')

echo ""
echo "=== 3. Login (same user) ==="
curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@mygarage.dev","password":"testpass123"}' | jq .

echo ""
echo "=== 4. Push a Vehicle ==="
curl -s -X POST "$BASE/sync/push" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicles": [{
      "id": "11111111-1111-1111-1111-111111111111",
      "plate": "XYZ-9999",
      "name": "Honda Civic",
      "year": "2023",
      "mileage": "12000",
      "mileageKm": 19312.0,
      "owner": "Test User",
      "fuelType": "Gasoline",
      "engineCapacity": "2.0L",
      "localImageFileNames": [],
      "updatedAt": "2026-06-22T12:00:00.000Z"
    }],
    "services": [],
    "parts": [],
    "pieces": [],
    "servicePieceCrossRefs": []
  }' | jq .

echo ""
echo "=== 5. Pull All Records ==="
curl -s "$BASE/sync/pull" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "=== 6. Push a Service Log ==="
curl -s -X POST "$BASE/sync/push" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicles": [],
    "services": [{
      "id": "22222222-2222-2222-2222-222222222222",
      "vehicleId": "11111111-1111-1111-1111-111111111111",
      "date": "2026-06-15",
      "description": "Oil change + filter replacement",
      "mileage": "12000",
      "mileageKm": 19312.0,
      "type": "regular",
      "updatedAt": "2026-06-22T12:30:00.000Z"
    }],
    "parts": [{
      "id": "33333333-3333-3333-3333-333333333333",
      "serviceLogId": "22222222-2222-2222-2222-222222222222",
      "name": "Oil Filter",
      "quantity": 1,
      "reference": "OF-15400-PLM-A01",
      "updatedAt": "2026-06-22T12:30:00.000Z"
    }],
    "pieces": [],
    "servicePieceCrossRefs": []
  }' | jq .

echo ""
echo "=== 7. Pull Again (verify all 3 records) ==="
curl -s "$BASE/sync/pull" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "✅ All tests completed!"
```

---

## 6. Project Structure (Docker-relevant)

```
MyGarage-Backend/
├── Dockerfile              # Multi-stage Node.js build
├── docker-compose.yml      # PostgreSQL 16 + API
├── docker-entrypoint.sh    # Waits for DB, runs migrations, starts API
├── .env.docker             # Template for Docker environment variables
├── .env                    # Your actual env (copy from .env.docker)
├── prisma/
│   ├── schema.prisma       # Database schema
│   └── migrations/         # Auto-generated migration files
├── prisma.config.ts        # Prisma 7 CLI config (datasource URL)
├── src/                    # TypeScript source
├── dist/                   # Compiled JS (created by `npm run build`)
├── package.json
└── tsconfig.json
```

---

## 7. Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USER` | `postgres` | PostgreSQL user |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `DB_NAME` | `mygarage` | Database name |
| `DB_EXTERNAL_PORT` | `5432` | Host port for PostgreSQL |
| `API_PORT` | `3000` | Host port for the API |
| `JWT_SECRET` | `change-me-in-production` | Secret for signing JWTs |
| `JWT_EXPIRES_IN` | `7d` | JWT expiration duration |

---

## 8. Troubleshooting

| Problem | Fix |
|---|---|
| `Error: Cannot find module dist/server.js` | Run `docker compose build --no-cache api` |
| Database connection refused | Ensure PostgreSQL health check passes: `docker compose ps` — wait for `healthy` |
| Migration already exists error | Wipe the DB: `docker compose down -v && docker compose up --build -d` |
| Port already in use | Change `API_PORT` in `.env` or stop the conflicting process |
