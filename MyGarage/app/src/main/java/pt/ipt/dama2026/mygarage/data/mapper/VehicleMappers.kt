package pt.ipt.dama2026.mygarage.data.mapper

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.PieceUseWithDetail as EntityPieceUseWithDetail
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts as EntityServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithPieces as EntityServiceLogWithPieces
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices as EntityVehicleWithServices
import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.Piece
import pt.ipt.dama2026.mygarage.domain.model.PieceUseWithDetail
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogCrossRef
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithPieces
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices

// ── Vehicle ──────────────────────────────────────────────────────

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    plate = plate,
    name = name,
    year = year,
    mileage = mileage,
    mileageKm = mileageKm,
    inspectionDate = inspectionDate,
    oilType = oilType,
    owner = owner,
    seatCount = seatCount,
    doorCount = doorCount,
    fuelType = fuelType,
    engineCapacity = engineCapacity,
    iucValue = iucValue,
    locationAddress = locationAddress,
    latitude = latitude,
    longitude = longitude,
    localImageFileNames = localImageFileNames,
    remoteImageUrl = remoteImageUrl
)

fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    plate = plate,
    name = name,
    year = year,
    mileage = mileage,
    mileageKm = mileageKm,
    inspectionDate = inspectionDate,
    oilType = oilType,
    owner = owner,
    seatCount = seatCount,
    doorCount = doorCount,
    fuelType = fuelType,
    engineCapacity = engineCapacity,
    iucValue = iucValue,
    locationAddress = locationAddress,
    latitude = latitude,
    longitude = longitude,
    localImageFileNames = localImageFileNames,
    remoteImageUrl = remoteImageUrl
)

// ── ServiceLog ───────────────────────────────────────────────────

fun ServiceLogEntity.toDomain(): ServiceLog = ServiceLog(
    id = id,
    vehicleId = vehicleId,
    date = date,
    description = description,
    mileage = mileage,
    mileageKm = mileageKm,
    type = type
)

fun ServiceLog.toEntity(): ServiceLogEntity = ServiceLogEntity(
    id = id,
    vehicleId = vehicleId,
    date = date,
    description = description,
    mileage = mileage,
    mileageKm = mileageKm,
    type = type
)

// ── Part ─────────────────────────────────────────────────────────

fun PartEntity.toDomain(): Part = Part(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference
)

fun Part.toEntity(): PartEntity = PartEntity(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference
)

// ── Piece ────────────────────────────────────────────────────────

fun PieceEntity.toDomain(): Piece = Piece(
    id = id,
    name = name,
    price = price
)

fun Piece.toEntity(): PieceEntity = PieceEntity(
    id = id,
    name = name,
    price = price
)

// ── ServiceLogCrossRef ───────────────────────────────────────────

fun ServiceLogPieceCrossRef.toDomain(): ServiceLogCrossRef = ServiceLogCrossRef(
    serviceLogId = serviceLogId,
    pieceId = pieceId,
    quantity = quantity
)

fun ServiceLogCrossRef.toEntity(): ServiceLogPieceCrossRef = ServiceLogPieceCrossRef(
    serviceLogId = serviceLogId,
    pieceId = pieceId,
    quantity = quantity
)

// ── Relations ────────────────────────────────────────────────────

fun EntityVehicleWithServices.toDomain(): VehicleWithServices = VehicleWithServices(
    vehicle = vehicle.toDomain(),
    services = services.filter { !it.isDeleted }.map { it.toDomain() }
)

fun EntityServiceLogWithParts.toDomain(): ServiceLogWithParts = ServiceLogWithParts(
    serviceLog = serviceLog.toDomain(),
    parts = parts.map { it.toDomain() }
)

fun EntityServiceLogWithPieces.toDomain(): ServiceLogWithPieces = ServiceLogWithPieces(
    serviceLog = serviceLog.toDomain(),
    pieceUses = pieces.map { use ->
        PieceUseWithDetail(
            crossRef = use.crossRef.toDomain(),
            piece = use.piece.toDomain()
        )
    }
)
