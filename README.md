# 🚗 MyGarage

**MyGarage** is a mobile app vehicle management built for drivers who want full control over their fleet. Scan license plates with your camera, log services, manage parts, and sync everything to the cloud — all from a modern, offline-first Android app backed by a robust REST API.

---

## 🧱 Architecture

```
┌──────────────────────────────┐     ┌──────────────────────────────┐
│        MyGarage (Android)     │────▶│    MyGarage-Backend (API)    │
│  • Kotlin + Jetpack Compose   │     │  • Node.js + Express + TS     │
│  • Room DB (offline-first)    │◀────│  • Prisma 7 + PostgreSQL 16   │
│  • ML Kit / CameraX           │     │  • Docker + Railway Deploy    │
│  • DataStore Preferences      │     │  • AWS S3 Image Storage       │
└──────────────────────────────┘     └──────────────────────────────┘
```

### Key Features

| Feature | Description |
|---|---|
| 📷 **License Plate Scanner** | Scan plates using the camera — powered by Google ML Kit Text Recognition |
| 🚙 **Vehicle Management** | Track plate, model, year, mileage, fuel type, engine capacity, inspection dates, IUC, and more |
| 🔧 **Service Logs** | Keep a full maintenance history with parts, prices, and quantities |
| 🛒 **Parts Inventory** | Manage reusable parts across service entries |
| 📍 **Location Awareness** | Tag vehicles with GPS location and street address |
| 🖼️ **Image Attachments** | Capture and attach photos to each vehicle (local + S3 cloud storage) |
| 🔐 **User Authentication** | JWT-based auth with multi-tenancy — your data is yours alone |
| 🔄 **Offline-First Sync** | Full CRUD locally via Room, then sync to the cloud when connected |
| 🌐 **REST API** | Complete backend with auth, vehicles, service logs, parts, and sync endpoints |
| 🐳 **Dockerized** | One-command `docker compose up` for PostgreSQL + API |
| 🚂 **Railway Ready** | Pre-configured for [Railway](https://railway.app) deployment |

---

## 📱 Android App (`MyGarage/`)

### Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Repository Pattern
- **Local DB:** Room (offline-first)
- **DI / Async:** Coroutines + Flow
- **Camera:** CameraX
- **OCR:** Google ML Kit Text Recognition
- **Preferences:** DataStore
- **Location:** Fused Location Provider

### Project Structure

```
app/src/main/java/pt/ipt/dama2026/mygarage/
├── MainActivity.kt              # Entry point, Compose + pager navigation
├── MyGarageApplication.kt       # Application class
├── data/                        # Data layer
│   ├── local/                   # Room DAOs, database, entities
│   ├── model/                   # API DTOs, sync models, auth models
│   ├── remote/                  # Retrofit API service
│   ├── storage/                 # Local image file management
│   └── location/                # Android location provider
├── domain/                      # Domain layer
│   ├── model/                   # Vehicle & business models
│   ├── repository/              # VehicleRepository, ImageStorageManager
│   ├── camera/                  # LicensePlateAnalyzer (ML Kit)
│   ├── engine/                  # EngineCapacityHelper
│   ├── licenseplates/           # License plate API service
│   ├── location/                # LocationManager, LocationResult
│   └── locale/                  # DistanceFormatter, LocaleManager
└── ui/                          # Presentation layer
    ├── screens/                 # Garage, vehicle detail, service logs, etc.
    ├── components/              # Reusable Compose components
    ├── theme/                   # Material 3 theming
    └── navigation/              # Nav graph & routes
```

### Build & Run

```bash
# 1. Open in Android Studio (or use Gradle)
cd MyGarage

# 2. Set up local.properties (see local.properties.example)
#    - MATRICULA_USERNAME=<your_username>
#    - MYGARAGE_API_URL=<backend_url>

# 3. Build & install
./gradlew installDebug
```

Default API URL: `https://mygaragebackend-production.up.railway.app`

---

## 🖥️ Backend (`MyGarage-Backend/`)

### Tech Stack

- **Runtime:** Node.js + Express 5
- **Language:** TypeScript 6
- **ORM:** Prisma 7
- **Database:** PostgreSQL 16
- **Auth:** JWT + bcryptjs
- **Storage:** AWS S3 (image uploads)
- **Image Processing:** Sharp
- **Container:** Docker + Docker Compose

### Project Structure

```
MyGarage-Backend/
├── src/
│   ├── server.ts                # Entry point
│   ├── app.ts                   # Express app config & middleware
│   ├── prisma.ts                # Prisma client singleton
│   ├── config/env.ts            # Environment variable validation
│   ├── controllers/             # Route handlers
│   │   ├── auth.controller.ts
│   │   ├── images.controller.ts
│   │   ├── sync.controller.ts
│   │   └── user.controller.ts
│   ├── services/                # Business logic
│   │   ├── auth.service.ts
│   │   ├── storage.service.ts
│   │   ├── sync.service.ts
│   │   └── user.service.ts
│   ├── middleware/
│   │   ├── auth.ts              # JWT verification middleware
│   │   └── errorHandler.ts
│   ├── routes/                  # Express route definitions
│   ├── types/                   # TypeScript type definitions
│   └── utils/jwt.ts             # JWT helper functions
├── prisma/
│   └── schema.prisma            # Database schema
├── docker-compose.yml           # PostgreSQL + API
├── Dockerfile
├── docker-entrypoint.sh
└── GUIDE.md                     # Full API & Docker guide
```

### Database Schema

| Model | Description |
|---|---|
| `User` | User account with email, name, garage name, avatar |
| `Vehicle` | Vehicle with plate, specs, mileage, location, images |
| `ServiceLog` | Maintenance record linked to a vehicle |
| `Part` | Part used in a service (name, quantity, reference) |
| `Piece` | Reusable piece/component with price |
| `ServiceLogPieceCrossRef` | Many-to-many: service logs ↔ pieces |

All models include `userId` (multi-tenancy), `createdAt`, `updatedAt`, and `isDeleted` (soft-delete for sync).

### Quick Start (Docker)

```bash
cd MyGarage-Backend

# 1. Copy and configure environment
cp .env.docker .env

# 2. Start PostgreSQL + API
docker compose up --build -d

# 3. Verify
curl http://localhost:3000/api/health
```

### Local Development

```bash
cd MyGarage-Backend

# 1. Install dependencies
npm install

# 2. Set up .env with your DATABASE_URL and JWT_SECRET

# 3. Run migrations
npm run db:migrate

# 4. Start dev server
npm run dev
```

### API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/health` | ❌ | Health check |
| `POST` | `/api/auth/register` | ❌ | Register new user |
| `POST` | `/api/auth/login` | ❌ | Login, get JWT |
| `GET` | `/api/user/profile` | ✅ | Get user profile |
| `PUT` | `/api/user/profile` | ✅ | Update profile |
| `POST` | `/api/sync/push` | ✅ | Push local changes to server |
| `GET` | `/api/sync/pull` | ✅ | Pull server changes |
| `POST` | `/api/images/upload` | ✅ | Upload vehicle images |
| `GET` | `/api/images/:key` | ✅ | Get presigned image URL |

> 📖 See [`GUIDE.md`](MyGarage-Backend/GUIDE.md) for full API documentation with `curl` examples.

---

## 🚀 Deployment

### Railway (Recommended)

The backend is pre-configured for [Railway](https://railway.app):

1. Connect your GitHub repo
2. Set environment variables in Railway dashboard
3. Railway auto-detects the Dockerfile

### Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DATABASE_URL` | ✅ | — | PostgreSQL connection string |
| `JWT_SECRET` | ✅ | — | Secret key for JWT signing |
| `JWT_EXPIRES_IN` | ❌ | `7d` | Token expiration |
| `PORT` | ❌ | `3000` | API server port |
| `AWS_REGION` | ❌ | — | S3 bucket region |
| `AWS_ACCESS_KEY_ID` | ❌ | — | S3 access key |
| `AWS_SECRET_ACCESS_KEY` | ❌ | — | S3 secret key |
| `AWS_S3_BUCKET` | ❌ | — | S3 bucket name |

---

## 🧑‍💻 Author

Built with ❤️ by **Cyb3rWolf** (@Cyb3rWolf945)

DAMA 2026 @ IPT (Instituto Politécnico de Tomar)

---

## 📄 License

This project is licensed under the terms of the [LICENSE](LICENSE) file.

---

<p align="center">
  <sub>🏍️ Keep your garage organized. Keep your rides on the road.</sub>
</p>
