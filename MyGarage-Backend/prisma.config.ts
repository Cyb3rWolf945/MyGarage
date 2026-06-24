import { defineConfig } from "prisma/config";

// Railway injects DATABASE_URL automatically when a PostgreSQL service is linked.
// No fallback — if it's missing, Prisma will fail with a clear error pointing to the env var.
export default defineConfig({
  datasource: {
    url: process.env.DATABASE_URL!,
  },
});
