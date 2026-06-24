import dotenv from "dotenv";

dotenv.config();

function requireEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}

export const env = {
  // DATABASE_URL is managed by Prisma (prisma.config.ts + adapter);
  // Railway injects it automatically when a PostgreSQL service is linked.
  DATABASE_URL: process.env.DATABASE_URL,
  JWT_SECRET: requireEnv("JWT_SECRET"),
  JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || "7d",
  PORT: parseInt(process.env.PORT || "3000", 10),
} as const;
