import express from "express";
import cors from "cors";
import authRoutes from "./routes/auth.routes";
import syncRoutes from "./routes/sync.routes";
import { errorHandler } from "./middleware/errorHandler";

const app = express();

// ── Global middleware ────────────────────────────────────────────
app.use(cors());
app.use(express.json({ limit: "10mb" }));

// ── Health check ─────────────────────────────────────────────────
app.get("/api/health", (_req, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// ── Routes ───────────────────────────────────────────────────────
app.use("/api/auth", authRoutes);
app.use("/api/sync", syncRoutes);

// ── Global error handler (must be last) ──────────────────────────
app.use(errorHandler);

export default app;
