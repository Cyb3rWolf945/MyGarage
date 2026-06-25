import bcrypt from "bcryptjs";
import prisma from "../prisma";
import { signToken } from "../utils/jwt";

const SALT_ROUNDS = 12;

/**
 * Register a new user.  Returns a JWT on success or throws on conflict.
 */
export async function register(
  email: string,
  password: string,
  name?: string,
  garageName?: string
) {
  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) {
    throw new ConflictError("A user with this email already exists");
  }

  const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);

  const user = await prisma.user.create({
    data: { email, passwordHash, name, garageName },
  });

  return {
    token: signToken({ userId: user.id }),
    user: { id: user.id, email: user.email, name: user.name, garageName: user.garageName },
  };
}

/**
 * Authenticate an existing user.  Returns a JWT on success or throws.
 */
export async function login(email: string, password: string) {
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) {
    throw new UnauthorizedError("Invalid email or password");
  }

  const valid = await bcrypt.compare(password, user.passwordHash);
  if (!valid) {
    throw new UnauthorizedError("Invalid email or password");
  }

  return {
    token: signToken({ userId: user.id }),
    user: { id: user.id, email: user.email, name: user.name, garageName: user.garageName },
  };
}

// ── Custom error classes (caught by errorHandler) ──────────────

export class ConflictError extends Error {
  statusCode = 409;
  constructor(message: string) {
    super(message);
    this.name = "ConflictError";
  }
}

export class UnauthorizedError extends Error {
  statusCode = 401;
  constructor(message: string) {
    super(message);
    this.name = "UnauthorizedError";
  }
}
