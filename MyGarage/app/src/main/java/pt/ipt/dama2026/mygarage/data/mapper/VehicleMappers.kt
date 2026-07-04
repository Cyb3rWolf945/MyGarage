package pt.ipt.dama2026.mygarage.data.mapper

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts as EntityServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices as EntityVehicleWithServices
import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices

/**
 * Funções de extensão para converter entre entidades Room (data layer)
 * e modelos de domínio (domain layer).
 *
 * Usadas pelo OfflineVehicleRepository para isolar a camada de dados da de domínio.
 */

// ── Vehicle ──────────────────────────────────────────────────────

/** Entity → Domain */
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

/** Domain → Entity */
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

/** Entity → Domain */
fun ServiceLogEntity.toDomain(): ServiceLog = ServiceLog(
    id = id,
    vehicleId = vehicleId,
    date = date,
    description = description,
    mileage = mileage,
    mileageKm = mileageKm,
    type = type
)

/** Domain → Entity */
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

/** Entity → Domain */
fun PartEntity.toDomain(): Part = Part(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference
)

/** Domain → Entity */
fun Part.toEntity(): PartEntity = PartEntity(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference
)

// ── Relations ────────────────────────────────────────────────────

/** Entity → Domain. Filtra serviços com isDeleted = true. */
fun EntityVehicleWithServices.toDomain(): VehicleWithServices = VehicleWithServices(
    vehicle = vehicle.toDomain(),
    services = services.filter { !it.isDeleted }.map { it.toDomain() }
)

/** Entity → Domain. Filtra peças com isDeleted = true. */
fun EntityServiceLogWithParts.toDomain(): ServiceLogWithParts = ServiceLogWithParts(
    serviceLog = serviceLog.toDomain(),
    parts = parts.filter { !it.isDeleted }.map { it.toDomain() }
)
