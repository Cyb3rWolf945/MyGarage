import { Request, Response, NextFunction } from "express";
import * as authService from "../services/auth.service";

/**
 * POST /api/auth/register
 * Body: { email: string, password: string }
 */
export async function register(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      res.status(400).json({ error: "email and password are required" });
      return;
    }

    const result = await authService.register(email, password);
    res.status(201).json(result);
  } catch (err) {
    if (
      err instanceof authService.ConflictError ||
      err instanceof authService.UnauthorizedError
    ) {
      res
        .status((err as authService.ConflictError).statusCode)
        .json({ error: err.message });
      return;
    }
    next(err);
  }
}

/**
 * POST /api/auth/login
 * Body: { email: string, password: string }
 */
export async function login(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      res.status(400).json({ error: "email and password are required" });
      return;
    }

    const result = await authService.login(email, password);
    res.status(200).json(result);
  } catch (err) {
    if (err instanceof authService.UnauthorizedError) {
      res.status(401).json({ error: err.message });
      return;
    }
    next(err);
  }
}
