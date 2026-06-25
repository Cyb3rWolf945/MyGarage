import { Router, Response, Request } from 'express';
import multer from 'multer';
import { MulterRequest } from '../types';
import { authMiddleware } from '../middleware/auth';
import * as imagesController from '../controllers/images.controller';

const router = Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 5 * 1024 * 1024,
  },
  fileFilter: (req, file, cb) => {
    const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
    if (allowedMimes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only JPEG, PNG, and WebP are allowed.'));
    }
  },
});

(router.post as any)('/upload', authMiddleware, upload.single('image'), imagesController.upload);
router.get('/proxy', imagesController.proxy);

export default router;
