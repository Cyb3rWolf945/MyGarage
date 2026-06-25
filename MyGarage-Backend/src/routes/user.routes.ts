import { Router } from "express";
import { authMiddleware } from "../middleware/auth";
import * as userController from "../controllers/user.controller";

const router = Router();

// All user routes are protected by JWT
router.use("/", authMiddleware as any);

router.patch("/profile", userController.updateProfile as any);

export default router;
