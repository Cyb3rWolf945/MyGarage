# ── Stage 1: Build ──────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Install dependencies (including devDependencies for tsc)
COPY package.json package-lock.json* ./
RUN npm ci

# Copy source & config files
COPY tsconfig.json ./
COPY prisma.config.ts ./
COPY prisma/ prisma/
COPY src/ src/

# Generate Prisma client & compile TypeScript
RUN npx prisma generate
RUN npm run build

# ── Stage 2: Production ─────────────────────────────────────────
FROM node:22-alpine

# Install postgresql-client for pg_isready (used by entrypoint)
RUN apk add --no-cache postgresql-client

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy package files and install only production deps
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev && npm cache clean --force

# Copy built artifacts from builder
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/prisma.config.ts ./prisma.config.ts

# Entrypoint script
COPY docker-entrypoint.sh ./docker-entrypoint.sh
RUN chmod +x docker-entrypoint.sh

USER appuser

EXPOSE 3000

ENTRYPOINT ["./docker-entrypoint.sh"]
