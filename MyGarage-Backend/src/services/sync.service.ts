import prisma from "../prisma";
import {
  VehiclePayload,
  ServiceLogPayload,
  PartPayload,
  PiecePayload,
  ServiceLogPieceCrossRefPayload,
  SyncPushBody,
} from "../types";

// ── PULL ─────────────────────────────────────────────────────────
// Returns all records for the authenticated user that were updated
// AFTER the given timestamp.

export interface SyncPullResult {
  vehicles: object[];
  services: object[];
  parts: object[];
  pieces: object[];
  servicePieceCrossRefs: object[];
}

export async function pull(
  userId: string,
  lastSyncTimestamp?: string
): Promise<SyncPullResult> {
  const since = lastSyncTimestamp ? new Date(lastSyncTimestamp) : new Date(0);

  const whereClause = {
    userId,
    updatedAt: { gt: since },
  };

  const [vehicles, services, parts, pieces, servicePieceCrossRefs] =
    await Promise.all([
      prisma.vehicle.findMany({ where: whereClause }),
      prisma.serviceLog.findMany({ where: whereClause }),
      prisma.part.findMany({ where: whereClause }),
      prisma.piece.findMany({ where: whereClause }),
      prisma.serviceLogPieceCrossRef.findMany({ where: whereClause }),
    ]);

  return {
    vehicles: vehicles.map(serializeDate),
    services: services.map(serializeDate),
    parts: parts.map(serializeDate),
    pieces: pieces.map(serializeDate),
    servicePieceCrossRefs: servicePieceCrossRefs.map(serializeDate),
  };
}

// ── PUSH ─────────────────────────────────────────────────────────
// Accepts arrays of records and performs an upsert for each.
// The `userId` is FORCED to `req.user.id` so a user cannot
// overwrite another user's data.

export async function push(userId: string, body: SyncPushBody): Promise<void> {
  const operations: Promise<unknown>[] = [];

  if (body.vehicles) {
    for (const v of body.vehicles) {
      operations.push(upsertVehicle(userId, v));
    }
  }
  if (body.services) {
    for (const s of body.services) {
      operations.push(upsertServiceLog(userId, s));
    }
  }
  if (body.parts) {
    for (const p of body.parts) {
      operations.push(upsertPart(userId, p));
    }
  }
  if (body.pieces) {
    for (const pc of body.pieces) {
      operations.push(upsertPiece(userId, pc));
    }
  }
  if (body.servicePieceCrossRefs) {
    for (const x of body.servicePieceCrossRefs) {
      operations.push(upsertCrossRef(userId, x));
    }
  }

  await Promise.all(operations);
}

// ── Upsert helpers ───────────────────────────────────────────────

async function upsertVehicle(userId: string, v: VehiclePayload) {
  await prisma.vehicle.upsert({
    where: { id: v.id },
    create: {
      id: v.id,
      plate: v.plate,
      name: v.name,
      year: v.year,
      mileage: v.mileage,
      mileageKm: v.mileageKm,
      inspectionDate: v.inspectionDate ?? null,
      oilType: v.oilType ?? null,
      owner: v.owner,
      seatCount: v.seatCount ?? null,
      doorCount: v.doorCount ?? null,
      fuelType: v.fuelType,
      engineCapacity: v.engineCapacity,
      iucValue: v.iucValue ?? null,
      mileageToNextService: v.mileageToNextService ?? null,
      locationAddress: v.locationAddress ?? null,
      latitude: v.latitude ?? null,
      longitude: v.longitude ?? null,
      localImageFileNames: v.localImageFileNames ?? [],
      remoteImageUrl: v.remoteImageUrl ?? null,
      userId, // ⬅ FORCED
    },
    update: {
      plate: v.plate,
      name: v.name,
      year: v.year,
      mileage: v.mileage,
      mileageKm: v.mileageKm,
      inspectionDate: v.inspectionDate ?? null,
      oilType: v.oilType ?? null,
      owner: v.owner,
      seatCount: v.seatCount ?? null,
      doorCount: v.doorCount ?? null,
      fuelType: v.fuelType,
      engineCapacity: v.engineCapacity,
      iucValue: v.iucValue ?? null,
      mileageToNextService: v.mileageToNextService ?? null,
      locationAddress: v.locationAddress ?? null,
      latitude: v.latitude ?? null,
      longitude: v.longitude ?? null,
      localImageFileNames: v.localImageFileNames ?? [],
      remoteImageUrl: v.remoteImageUrl ?? null,
      // userId is intentionally NOT in the update block —
      // ownership cannot be changed via sync.
    },
  });
}

async function upsertServiceLog(userId: string, s: ServiceLogPayload) {
  await prisma.serviceLog.upsert({
    where: { id: s.id },
    create: {
      id: s.id,
      vehicleId: s.vehicleId,
      date: s.date,
      description: s.description,
      mileage: s.mileage,
      mileageKm: s.mileageKm,
      type: s.type,
      userId, // ⬅ FORCED
    },
    update: {
      vehicleId: s.vehicleId,
      date: s.date,
      description: s.description,
      mileage: s.mileage,
      mileageKm: s.mileageKm,
      type: s.type,
    },
  });
}

async function upsertPart(userId: string, p: PartPayload) {
  await prisma.part.upsert({
    where: { id: p.id },
    create: {
      id: p.id,
      serviceLogId: p.serviceLogId,
      name: p.name,
      quantity: p.quantity,
      reference: p.reference ?? null,
      userId, // ⬅ FORCED
    },
    update: {
      serviceLogId: p.serviceLogId,
      name: p.name,
      quantity: p.quantity,
      reference: p.reference ?? null,
    },
  });
}

async function upsertPiece(userId: string, pc: PiecePayload) {
  await prisma.piece.upsert({
    where: { id: pc.id },
    create: {
      id: pc.id,
      name: pc.name,
      price: pc.price,
      userId, // ⬅ FORCED
    },
    update: {
      name: pc.name,
      price: pc.price,
    },
  });
}

async function upsertCrossRef(userId: string, x: ServiceLogPieceCrossRefPayload) {
  await prisma.serviceLogPieceCrossRef.upsert({
    where: {
      serviceLogId_pieceId: {
        serviceLogId: x.serviceLogId,
        pieceId: x.pieceId,
      },
    },
    create: {
      serviceLogId: x.serviceLogId,
      pieceId: x.pieceId,
      quantity: x.quantity,
      userId, // ⬅ FORCED
    },
    update: {
      quantity: x.quantity,
    },
  });
}

// ── Helpers ──────────────────────────────────────────────────────

/** Convert Date fields to ISO strings so the JSON response is clean. */
function serializeDate(record: Record<string, unknown>): Record<string, unknown> {
  const clone: Record<string, unknown> = { ...record };
  for (const key of Object.keys(clone)) {
    if (clone[key] instanceof Date) {
      clone[key] = (clone[key] as Date).toISOString();
    }
  }
  return clone;
}
