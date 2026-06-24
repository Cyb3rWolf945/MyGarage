import { Router } from "express";
import { authMiddleware } from "../middleware/auth";
import * as syncController from "../controllers/sync.controller";

const router = Router();

// All sync routes are protected by JWT
router.use("/", authMiddleware as any);

router.get("/pull", syncController.pull as any);
router.post("/push", syncController.push as any);

export default router;
