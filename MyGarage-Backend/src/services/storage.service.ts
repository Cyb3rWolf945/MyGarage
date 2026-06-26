import {
  S3Client,
  PutObjectCommand,
  GetObjectCommand,
  DeleteObjectCommand,
} from '@aws-sdk/client-s3';
import sharp from 'sharp';

const s3Client = new S3Client({
  region: process.env.S3_REGION || 'auto',
  endpoint: process.env.S3_ENDPOINT,
  credentials: {
    accessKeyId: process.env.S3_ACCESS_KEY_ID || '',
    secretAccessKey: process.env.S3_SECRET_ACCESS_KEY || '',
  },
});

// Validate S3 configuration on startup
if (!process.env.S3_ENDPOINT) {
  console.warn('WARNING: S3_ENDPOINT environment variable is not set');
}
if (!process.env.S3_ACCESS_KEY_ID) {
  console.warn('WARNING: S3_ACCESS_KEY_ID environment variable is not set');
}
if (!process.env.S3_SECRET_ACCESS_KEY) {
  console.warn('WARNING: S3_SECRET_ACCESS_KEY environment variable is not set');
}
if (!process.env.S3_BUCKET_NAME) {
  console.warn('WARNING: S3_BUCKET_NAME environment variable is not set');
}
if (!process.env.S3_PUBLIC_URL_PREFIX) {
  console.warn('WARNING: S3_PUBLIC_URL_PREFIX environment variable is not set');
}

export async function uploadImage(
  fileBuffer: Buffer,
  originalFileName: string,
  userId: string,
  imageType: string
): Promise<string> {
  if (!process.env.S3_BUCKET_NAME) {
    throw new Error('S3_BUCKET_NAME environment variable is not set');
  }

  try {
    console.log('Starting image upload for user:', userId);
    const optimizedBuffer = await optimizeImage(fileBuffer);
    const timestamp = Date.now();
    const sanitizedFileName = originalFileName
      .replace(/[^a-zA-Z0-9.-]/g, '_')
      .toLowerCase();
    const key = `images/${userId}/${imageType}/${timestamp}-${sanitizedFileName}`;

    console.log('S3 Upload params:', {
      bucket: process.env.S3_BUCKET_NAME,
      key,
      endpoint: process.env.S3_ENDPOINT,
      region: process.env.S3_REGION,
      bufferSize: optimizedBuffer.length,
    });

    await s3Client.send(
      new PutObjectCommand({
        Bucket: process.env.S3_BUCKET_NAME,
        Key: key,
        Body: optimizedBuffer,
        ContentType: 'image/jpeg',
        Metadata: {
          'original-filename': originalFileName,
          'user-id': userId,
          'image-type': imageType,
        },
      })
    );

    console.log('Image uploaded successfully to S3:', key);
    const publicUrl = `${process.env.S3_PUBLIC_URL_PREFIX || ''}${key}`;
    console.log('Public URL:', publicUrl);
    return publicUrl;
  } catch (error) {
    console.error('S3 upload error:', error);
    throw new Error(`Failed to upload image: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

export async function optimizeImage(buffer: Buffer): Promise<Buffer> {
  try {
    return await sharp(buffer)
      .resize(2000, 2000, {
        fit: 'inside',
        withoutEnlargement: true,
      })
      .jpeg({ quality: 80, progressive: true })
      .toBuffer();
  } catch (error) {
    console.error('Image optimization error:', error);
    throw new Error(`Failed to optimize image: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

export async function validateImage(
  buffer: Buffer,
  mimeType: string
): Promise<{ valid: boolean; error?: string }> {
  const maxSize = 5 * 1024 * 1024;
  const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];

  if (buffer.length > maxSize) {
    return { valid: false, error: 'File size exceeds 5MB limit' };
  }

  if (!allowedMimes.includes(mimeType)) {
    return {
      valid: false,
      error: 'File type not supported. Allowed: JPEG, PNG, WebP',
    };
  }

  try {
    const metadata = await sharp(buffer).metadata();
    const width = metadata.width || 0;
    const height = metadata.height || 0;

    if (width > 4000 || height > 4000) {
      return {
        valid: false,
        error: 'Image dimensions exceed 4000x4000 pixels',
      };
    }

    return { valid: true };
  } catch (error) {
    return {
      valid: false,
      error: `Invalid image file: ${error instanceof Error ? error.message : 'Unknown error'}`,
    };
  }
}

export async function deleteImage(imageUrl: string): Promise<void> {
  if (!process.env.S3_PUBLIC_URL_PREFIX) {
    throw new Error('S3_PUBLIC_URL_PREFIX environment variable is not set');
  }

  try {
    const key = imageUrl.replace(process.env.S3_PUBLIC_URL_PREFIX, '');

    if (!key || key === imageUrl) {
      throw new Error('Invalid image URL');
    }

    await s3Client.send(
      new DeleteObjectCommand({
        Bucket: process.env.S3_BUCKET_NAME,
        Key: key,
      })
    );
  } catch (error) {
    console.error('S3 delete error:', error);
    throw new Error(`Failed to delete image: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * Proxies an image from S3 by extracting the key from the stored public URL.
 * Returns the image body as a Buffer with its content type.
 */
export async function getImage(
  imageUrl: string
): Promise<{ buffer: Buffer; contentType: string } | null> {
  if (!process.env.S3_BUCKET_NAME || !process.env.S3_PUBLIC_URL_PREFIX) {
    throw new Error('S3 configuration is incomplete');
  }

  try {
    // Strip quotes that may have been stored in old URLs
    const cleanUrl = imageUrl.replace(/"/g, '');
    const key = cleanUrl.replace(process.env.S3_PUBLIC_URL_PREFIX, '');

    if (!key || key === cleanUrl) {
      console.warn('Could not extract S3 key from URL:', cleanUrl);
      return null;
    }

    const response = await s3Client.send(
      new GetObjectCommand({
        Bucket: process.env.S3_BUCKET_NAME,
        Key: key,
      })
    );

    if (!response.Body) {
      return null;
    }

    // Convert stream/body to buffer
    const buffer = await response.Body.transformToByteArray();
    const contentType = response.ContentType || 'image/jpeg';

    return { buffer: Buffer.from(buffer), contentType };
  } catch (error) {
    console.error('S3 proxy error:', error);
    return null;
  }
}
