import { Request, Response } from 'express';
import { uploadImage, validateImage } from '../services/storage.service';

export async function upload(req: Request, res: Response): Promise<void> {
  try {
    const userId = (req as any).user?.userId;
    if (!userId) {
      res.status(401).json({ ok: false, error: 'User not authenticated' });
      return;
    }

    const file = (req as any).file;
    if (!file) {
      res.status(400).json({ ok: false, error: 'No image file provided' });
      return;
    }

    const validation = await validateImage(file.buffer, file.mimetype);
    if (!validation.valid) {
      res.status(400).json({ ok: false, error: validation.error });
      return;
    }

    const imageType = (req.body.imageType as string) || 'general';
    const imageUrl = await uploadImage(
      file.buffer,
      file.originalname,
      userId,
      imageType
    );

    res.status(200).json({
      ok: true,
      imageUrl,
    });
  } catch (error) {
    console.error('Image upload error:', error);
    res.status(500).json({
      ok: false,
      error: error instanceof Error ? error.message : 'Image upload failed',
    });
  }
}
