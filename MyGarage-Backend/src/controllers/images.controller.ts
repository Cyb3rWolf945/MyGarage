import { Response, Request } from 'express';
import { MulterRequest } from '../types';
import { uploadImage, validateImage, getImage } from '../services/storage.service';

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

/**
 * GET /api/images/proxy?url=<encoded-s3-url>
 *
 * Proxies an image from S3 so the client never needs direct S3 access.
 * The image URL stored in the database may not be publicly accessible,
 * but the backend can fetch it with S3 credentials.
 */
export async function proxy(req: Request, res: Response): Promise<void> {
  try {
    const url = req.query.url as string | undefined;
    if (!url) {
      res.status(400).json({ ok: false, error: 'Missing "url" query parameter' });
      return;
    }

    const result = await getImage(url);
    if (!result) {
      res.status(404).json({ ok: false, error: 'Image not found' });
      return;
    }

    res.setHeader('Content-Type', result.contentType);
    res.setHeader('Cache-Control', 'public, max-age=86400'); // 24h cache
    res.send(result.buffer);
  } catch (error) {
    console.error('Image proxy error:', error);
    res.status(500).json({
      ok: false,
      error: error instanceof Error ? error.message : 'Image proxy failed',
    });
  }
}
