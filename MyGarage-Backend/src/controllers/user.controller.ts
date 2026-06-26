import { Request, Response, NextFunction } from "express";
import * as userService from "../services/user.service";
import { AuthenticatedRequest } from "../types";

/**
 * GET /api/user/profile
 * Returns the authenticated user's profile including avatarUrl.
 */
export async function getProfile(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const userId = req.user.userId;
    const profile = await userService.getUserProfile(userId);
    res.status(200).json(profile);
  } catch (err) {
    next(err);
  }
}

/**
 * PATCH /api/user/profile
 * Updates authenticated user's profile (name, garageName, avatarUrl).
 */
export async function updateProfile(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const userId = req.user.userId;
    const { name, garageName, avatarUrl } = req.body;

    const result = await userService.updateUserProfile(userId, name, garageName, avatarUrl);
    res.status(200).json(result);
  } catch (err) {
    next(err);
  }
}

/**
 * DELETE /api/user/account
 * Permanently deletes the authenticated user's account and all data.
 */
export async function deleteAccount(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const userId = req.user.userId;
    const result = await userService.deleteUserAccount(userId);
    res.status(200).json(result);
  } catch (err) {
    next(err);
  }
}
