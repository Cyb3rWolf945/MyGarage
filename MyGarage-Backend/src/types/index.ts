import { Request } from "express";

// ── JWT payload ──────────────────────────────────────────────
export interface JwtPayload {
  userId: string;
}

// ── Extended Request (after authMiddleware) ────────────────────
export interface AuthenticatedRequest extends Request {
  user: JwtPayload;
}

// ── Sync Push body shapes ─────────────────────────────────────
export interface VehiclePayload {
  id: string; // UUID
  plate: string;
  name: string;
  year: string;
  mileage: string;
  mileageKm: number;
  inspectionDate?: string | null;
  oilType?: string | null;
  owner: string;
  seatCount?: string | null;
  doorCount?: string | null;
  fuelType: string;
  engineCapacity: string;
  iucValue?: string | null;
  mileageToNextService?: string | null;
  locationAddress?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  localImageFileNames: string[];
  remoteImageUrl?: string | null;
  updatedAt: string; // ISO-8601
}

export interface ServiceLogPayload {
  id: string; // UUID
  vehicleId: string;
  date: string;
  description: string;
  mileage: string;
  mileageKm: number;
  type: string;
  updatedAt: string; // ISO-8601
}

export interface PartPayload {
  id: string; // UUID
  serviceLogId: string;
  name: string;
  quantity: number;
  reference?: string | null;
  updatedAt: string; // ISO-8601
}

export interface PiecePayload {
  id: string;
  name: string;
  price: number;
  updatedAt: string; // ISO-8601
}

export interface ServiceLogPieceCrossRefPayload {
  serviceLogId: string;
  pieceId: string;
  quantity: number;
  updatedAt: string; // ISO-8601
}

export interface SyncPushBody {
  vehicles?: VehiclePayload[];
  services?: ServiceLogPayload[];
  parts?: PartPayload[];
  pieces?: PiecePayload[];
  servicePieceCrossRefs?: ServiceLogPieceCrossRefPayload[];
}
