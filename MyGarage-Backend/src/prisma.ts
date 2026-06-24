import { PrismaClient } from "@prisma/client";
import { PrismaPg } from "@prisma/adapter-pg";

// Prisma 7 requires a Driver Adapter (or Accelerate URL) at runtime.
// prisma.config.ts is only used by the CLI (migrations, generate, etc.).
const pool = new PrismaPg({
  connectionString: process.env.DATABASE_URL,
});

const prisma = new PrismaClient({
  adapter: pool,
});

export default prisma;
