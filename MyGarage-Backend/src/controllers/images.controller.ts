import { Response } from 'express';
import { MulterRequest } from '../types';
import { uploadImage, validateImage } from '../services/storage.service';

export async function upload(req: MulterRequest, res: Response): Promise<void> {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({ ok: false, error: 'User not authenticated' });
      return;
    }

    const file = req.file;
    if (!file) {
      res.status(400).json({ ok: false, error: 'No image file provided' });
      return;
    }

    const validation = await validateImage(file.buffer, file.mimetype);
    if (!validation.valid) {
      res.status(400).json({ ok: false, error: validation.error });
      return;
    }

    // Strip quotes — Retrofit/Gson wraps @Part String values in JSON quotes
    const imageTypeRaw = (req.body.imageType as string) || 'general';
    const imageType = imageTypeRaw.replace(/^"|"$/g, '').replace(/"/g, '');
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
