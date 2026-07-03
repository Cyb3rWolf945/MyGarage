package pt.ipt.dama2026.mygarage.data.model

import com.google.gson.annotations.SerializedName
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.domain.locale.DateFormats
import java.util.UUID

data class VehiclePayload(
    @SerializedName("id") val id: String,
    @SerializedName("plate") val plate: String,
    @SerializedName("name") val name: String,
    @SerializedName("year") val year: String,
    @SerializedName("mileage") val mileage: String,
    @SerializedName("mileageKm") val mileageKm: Double,
    @SerializedName("inspectionDate") val inspectionDate: String? = null,
    @SerializedName("oilType") val oilType: String? = null,
    @SerializedName("owner") val owner: String,
    @SerializedName("seatCount") val seatCount: String? = null,
    @SerializedName("doorCount") val doorCount: String? = null,
    @SerializedName("fuelType") val fuelType: String,
    @SerializedName("engineCapacity") val engineCapacity: String,
    @SerializedName("iucValue") val iucValue: String? = null,
    @SerializedName("locationAddress") val locationAddress: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("localImageFileNames") val localImageFileNames: List<String> = emptyList(),
    @SerializedName("remoteImageUrl") val remoteImageUrl: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("updatedAt") val updatedAt: String // ISO-8601
)

data class ServiceLogPayload(
    @SerializedName("id") val id: String,
    @SerializedName("vehicleId") val vehicleId: String,
    @SerializedName("date") val date: String,
    @SerializedName("description") val description: String,
    @SerializedName("mileage") val mileage: String,
    @SerializedName("mileageKm") val mileageKm: Double,
    @SerializedName("type") val type: String,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PartPayload(
    @SerializedName("id") val id: String,
    @SerializedName("serviceLogId") val serviceLogId: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("updatedAt") val updatedAt: String
)

data class SyncPushBody(
    @SerializedName("vehicles") val vehicles: List<VehiclePayload>? = null,
    @SerializedName("services") val services: List<ServiceLogPayload>? = null,
    @SerializedName("parts") val parts: List<PartPayload>? = null
)

data class SyncResponse(
    @SerializedName("ok") val ok: Boolean
)

data class SyncPullResponse(
    @SerializedName("vehicles") val vehicles: List<VehiclePayload> = emptyList(),
    @SerializedName("services") val services: List<ServiceLogPayload> = emptyList(),
    @SerializedName("parts") val parts: List<PartPayload> = emptyList()
)

fun VehicleEntity.toPayload(): VehiclePayload = VehiclePayload(
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
    remoteImageUrl = remoteImageUrl,
    isDeleted = isDeleted,
    updatedAt = iso8601(updatedAt)
)

fun ServiceLogEntity.toPayload(): ServiceLogPayload = ServiceLogPayload(
    id = id.toString(),
    vehicleId = vehicleId,
    date = date,
    description = description,
    mileage = mileage,
    mileageKm = mileageKm,
    type = type,
    isDeleted = isDeleted,
    updatedAt = iso8601(updatedAt)
)

fun PartEntity.toPayload(): PartPayload = PartPayload(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference,
    isDeleted = isDeleted,
    updatedAt = iso8601(updatedAt)
)

fun VehiclePayload.toEntity(): VehicleEntity = VehicleEntity(
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
    remoteImageUrl = remoteImageUrl,
    updatedAt = parseIso8601(updatedAt),
    isDeleted = isDeleted
)

fun ServiceLogPayload.toEntity(): ServiceLogEntity = ServiceLogEntity(
    id = UUID.fromString(id),
    vehicleId = vehicleId,
    date = date,
    description = description,
    mileage = mileage,
    mileageKm = mileageKm,
    type = type,
    updatedAt = parseIso8601(updatedAt),
    isDeleted = isDeleted
)

fun PartPayload.toEntity(): PartEntity = PartEntity(
    id = id,
    serviceLogId = serviceLogId,
    name = name,
    quantity = quantity,
    reference = reference,
    updatedAt = parseIso8601(updatedAt),
    isDeleted = isDeleted
)

private fun iso8601(epochMillis: Long): String = DateFormats.ISO_8601.format(java.util.Date(epochMillis))

fun parseIso8601(isoString: String): Long = DateFormats.ISO_8601.parse(isoString)?.time ?: 0L
