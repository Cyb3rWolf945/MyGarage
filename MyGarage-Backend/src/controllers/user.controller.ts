import { Request, Response, NextFunction } from "express";
import * as userService from "../services/user.service";
import { AuthenticatedRequest } from "../types";

/**
 * PATCH /api/user/profile
 * Updates authenticated user's profile (name, garageName).
 */
export async function updateProfile(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const userId = req.user.userId;
    const { name, garageName } = req.body;

    const result = await userService.updateUserProfile(userId, name, garageName);
    res.status(200).json(result);
  } catch (err) {
    next(err);
  }
}
