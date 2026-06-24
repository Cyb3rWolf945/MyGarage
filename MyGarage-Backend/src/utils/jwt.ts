import jwt from "jsonwebtoken";
import { env } from "../config/env";
import { JwtPayload } from "../types";

/**
 * Sign a JWT containing the user's ID.
 */
export function signToken(payload: JwtPayload): string {
  // jsonwebtoken v9 types use a branded StringValue from 'ms' for expiresIn;
  // `as any` avoids a needless runtime dependency.
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return jwt.sign(payload, env.JWT_SECRET, {
    expiresIn: env.JWT_EXPIRES_IN as any,
  });
}

/**
 * Verify and decode a JWT.  Throws if the token is invalid or expired.
 */
export function verifyToken(token: string): JwtPayload {
  return jwt.verify(token, env.JWT_SECRET) as JwtPayload;
}
