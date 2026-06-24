import { Response, NextFunction } from "express";
import * as syncService from "../services/sync.service";
import { AuthenticatedRequest, SyncPushBody } from "../types";

/**
 * GET /api/sync/pull?lastSyncTimestamp=2025-01-01T00:00:00Z
 *
 * Returns all records belonging to the authenticated user that were
 * modified after `lastSyncTimestamp`.
 */
export async function pull(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const lastSyncTimestamp = req.query.lastSyncTimestamp as string | undefined;

    const data = await syncService.pull(req.user.userId, lastSyncTimestamp);
    res.status(200).json(data);
  } catch (err) {
    next(err);
  }
}

/**
 * POST /api/sync/push
 *
 * Accepts { vehicles, services, parts, pieces, servicePieceCrossRefs }
 * and upserts each record.  The backend forces userId = req.user.userId
 * for every record to prevent cross-tenant writes.
 */
export async function push(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const body = req.body as SyncPushBody;

    await syncService.push(req.user.userId, body);
    res.status(200).json({ ok: true });
  } catch (err) {
    next(err);
  }
}
