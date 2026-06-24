import { Request, Response, NextFunction } from "express";

/**
 * Global error-handling middleware.
 * Catches any unhandled errors thrown in route handlers.
 */
export function errorHandler(
  err: Error,
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  console.error("[ErrorHandler]", err.message, err.stack);

  res.status(500).json({
    error: "Internal server error",
    ...(process.env.NODE_ENV === "development" && { details: err.message }),
  });
}
