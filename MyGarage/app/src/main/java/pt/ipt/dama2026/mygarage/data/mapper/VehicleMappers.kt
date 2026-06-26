package pt.ipt.dama2026.mygarage.data.mapper

import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.domain.model.Vehicle

/**
 * Maps a Room [VehicleEntity] to the domain-layer [Vehicle].
 */
fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    plate = plate,
    name = name,
    year = year,
    mileage = mileage,
    inspectionDate = inspectionDate,
    oilType = oilType,
    owner = owner,
    seatCount = seatCount,
    doorCount = doorCount,
    fuelType = fuelType,
    engineCapacity = engineCapacity,
    iucValue = iucValue,
    mileageToNextService = mileageToNextService,
    locationAddress = locationAddress,
    latitude = latitude,
    longitude = longitude,
    localImageFileNames = localImageFileNames,
    remoteImageUrl = remoteImageUrl
)

/**
 * Maps a domain [Vehicle] back to a Room [VehicleEntity].
 */
fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    plate = plate,
    name = name,
    year = year,
    mileage = mileage,
    inspectionDate = inspectionDate,
    oilType = oilType,
    owner = owner,
    seatCount = seatCount,
    doorCount = doorCount,
    fuelType = fuelType,
    engineCapacity = engineCapacity,
    iucValue = iucValue,
    mileageToNextService = mileageToNextService,
    locationAddress = locationAddress,
    latitude = latitude,
    longitude = longitude,
    localImageFileNames = localImageFileNames,
    remoteImageUrl = remoteImageUrl
)
