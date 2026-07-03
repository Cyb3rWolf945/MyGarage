package pt.ipt.dama2026.mygarage.data.repository

import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts as EntityServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithPieces as EntityServiceLogWithPieces
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices as EntityVehicleWithServices
import pt.ipt.dama2026.mygarage.data.mapper.toDomain
import pt.ipt.dama2026.mygarage.data.mapper.toEntity
import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.Piece
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogCrossRef
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithPieces
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data-layer implementation of [VehicleRepository] using Room [VehicleDao].
 * Maps between domain models and Room entities at the boundary.
 */
@Singleton
class OfflineVehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : VehicleRepository {

    override suspend fun insertVehicle(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle.toEntity())
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        val existing = vehicleDao.getVehicleById(vehicle.id)
        val entity = if (existing != null) {
            existing.copy(
                plate = vehicle.plate,
                name = vehicle.name,
                year = vehicle.year,
                mileage = vehicle.mileage,
                mileageKm = vehicle.mileageKm,
                inspectionDate = vehicle.inspectionDate,
                oilType = vehicle.oilType,
                owner = vehicle.owner,
                seatCount = vehicle.seatCount,
                doorCount = vehicle.doorCount,
                fuelType = vehicle.fuelType,
                engineCapacity = vehicle.engineCapacity,
                iucValue = vehicle.iucValue,
                locationAddress = vehicle.locationAddress,
                latitude = vehicle.latitude,
                longitude = vehicle.longitude,
                localImageFileNames = vehicle.localImageFileNames,
                remoteImageUrl = vehicle.remoteImageUrl,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            vehicle.toEntity()
        }
        vehicleDao.updateVehicle(entity)
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) {
        userPreferencesRepository?.markVehicleDeleted(vehicle.id)
        val existing = vehicleDao.getVehicleById(vehicle.id) ?: return
        vehicleDao.updateVehicle(existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun getVehicleById(vehicleId: String): Vehicle? {
        return vehicleDao.getVehicleById(vehicleId)?.toDomain()
    }

    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { list -> list.map { it.toDomain() } }
    }

    override fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices> {
        return vehicleDao.getVehicleWithServices(vehicleId).map { it.toDomain() }
    }

    override fun getAllPieces(): Flow<List<Piece>> {
        return vehicleDao.getAllPieces().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertServiceLog(serviceLog: ServiceLog) {
        val entity = serviceLog.toEntity()
        vehicleDao.insertServiceLog(entity)
        handleServiceLogSideEffects(entity)
    }

    override suspend fun insertServiceLogWithPieces(
        serviceLog: ServiceLog,
        pieces: List<ServiceLogCrossRef>
    ) {
        val entity = serviceLog.toEntity()
        vehicleDao.insertServiceLog(entity)
        pieces.forEach { vehicleDao.insertServiceLogPieceCrossRef(it.toEntity()) }
        handleServiceLogSideEffects(entity)
    }

    override fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces> {
        return vehicleDao.getServiceLogWithPieces(serviceLogId).map { it.toDomain() }
    }

    override suspend fun insertPart(part: Part) {
        vehicleDao.insertPart(part.toEntity())
    }

    override fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts> {
        return vehicleDao.getServiceLogWithParts(serviceLogId).map { it.toDomain() }
    }

    override suspend fun updateServiceLogWithParts(
        serviceLog: ServiceLog,
        parts: List<Part>
    ) {
        val existing = vehicleDao.getServiceLogById(serviceLog.id)
        val entity = if (existing != null) {
            existing.copy(
                vehicleId = serviceLog.vehicleId,
                date = serviceLog.date,
                description = serviceLog.description,
                mileage = serviceLog.mileage,
                type = serviceLog.type,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            serviceLog.toEntity().copy(updatedAt = System.currentTimeMillis())
        }
        vehicleDao.updateServiceLog(entity)
        vehicleDao.deletePartsByServiceId(serviceLog.id.toString())
        parts.forEach { vehicleDao.insertPart(it.toEntity()) }
        handleServiceLogSideEffects(entity)
    }

    override suspend fun deleteServiceLog(serviceLog: ServiceLog) {
        val existing = vehicleDao.getServiceLogById(serviceLog.id) ?: return
        vehicleDao.updateServiceLog(existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
    }

    /**
     * Applies business rules based on service log type:
     * - Inspection: advances inspectionDate by 1 year
     */
    private suspend fun handleServiceLogSideEffects(serviceLog: ServiceLogEntity) {
        val vehicle = vehicleDao.getVehicleById(serviceLog.vehicleId) ?: return

        when {
            serviceLog.type.equals("Inspection", ignoreCase = true) -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                runCatching {
                    val date = dateFormat.parse(serviceLog.date) ?: return
                    val cal = Calendar.getInstance().apply { time = date; add(Calendar.YEAR, 1) }
                    vehicleDao.updateVehicle(vehicle.copy(inspectionDate = dateFormat.format(cal.time)))
                }
            }
        }
    }
}
