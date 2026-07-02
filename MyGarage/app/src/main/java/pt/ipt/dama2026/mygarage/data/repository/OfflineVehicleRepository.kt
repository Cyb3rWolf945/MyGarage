package pt.ipt.dama2026.mygarage.data.repository

import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithPieces
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Data-layer implementation of [VehicleRepository] using Room [VehicleDao].
 * Handles business rules for service log side effects (revision mileage bump,
 * inspection date update).
 */
class OfflineVehicleRepository(
    private val vehicleDao: VehicleDao,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : VehicleRepository {

    override suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insertVehicle(vehicle)
    }

    override suspend fun updateVehicle(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)
    }

    override suspend fun deleteVehicle(vehicle: VehicleEntity) {
        userPreferencesRepository?.markVehicleDeleted(vehicle.id)
        vehicleDao.updateVehicle(vehicle.copy(isDeleted = true))
    }

    override fun getAllVehicles(): Flow<List<VehicleEntity>> {
        return vehicleDao.getAllVehicles()
    }

    override fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices> {
        return vehicleDao.getVehicleWithServices(vehicleId)
    }

    override fun getAllPieces(): Flow<List<PieceEntity>> {
        return vehicleDao.getAllPieces()
    }

    override suspend fun insertServiceLog(serviceLog: ServiceLogEntity) {
        vehicleDao.insertServiceLog(serviceLog)
        handleServiceLogSideEffects(serviceLog)
    }

    override suspend fun insertServiceLogWithPieces(
        serviceLog: ServiceLogEntity,
        pieces: List<ServiceLogPieceCrossRef>
    ) {
        vehicleDao.insertServiceLog(serviceLog)
        pieces.forEach { vehicleDao.insertServiceLogPieceCrossRef(it) }
        handleServiceLogSideEffects(serviceLog)
    }

    override fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces> {
        return vehicleDao.getServiceLogWithPieces(serviceLogId)
    }

    override suspend fun insertPart(part: PartEntity) {
        vehicleDao.insertPart(part)
    }

    override fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts> {
        return vehicleDao.getServiceLogWithParts(serviceLogId)
    }

    override suspend fun updateServiceLogWithParts(
        serviceLog: ServiceLogEntity,
        parts: List<PartEntity>
    ) {
        // 1. Update the ServiceLogEntity
        vehicleDao.updateServiceLog(serviceLog)
        // 2. Delete all existing parts for this service log
        vehicleDao.deletePartsByServiceId(serviceLog.id.toString())
        // 3. Insert the updated list of parts
        parts.forEach { vehicleDao.insertPart(it) }
        handleServiceLogSideEffects(serviceLog)
    }

    override suspend fun deleteServiceLog(serviceLog: ServiceLogEntity) {
        vehicleDao.deleteServiceLog(serviceLog)
    }

    /**
     * Applies business rules based on service log type:
     * - revision: bumps mileageToNextService by 4349.59835 mi
     * - Inspection: advances inspectionDate by 1 year
     */
    private suspend fun handleServiceLogSideEffects(serviceLog: ServiceLogEntity) {
        if (serviceLog.type.equals("revision", ignoreCase = true)) {
            val vehicle = vehicleDao.getVehicleById(serviceLog.vehicleId) ?: return
            
            val currentMileageStr = vehicle.mileageToNextService ?: return
            val cleanStr = currentMileageStr.replace(",", "").replace(Regex("[^0-9.]"), "")
            val currentMileage = cleanStr.toDoubleOrNull() ?: 0.0
            
            val newMileage = currentMileage + 4349.59835
            val formattedMileage = String.format(Locale.US, "%,.5f mi", newMileage)
            
            val updatedVehicle = vehicle.copy(mileageToNextService = formattedMileage)
            vehicleDao.updateVehicle(updatedVehicle)
            
        } else if (serviceLog.type.equals("Inspection", ignoreCase = true)) {
            val vehicle = vehicleDao.getVehicleById(serviceLog.vehicleId) ?: return
            
            val serviceDateStr = serviceLog.date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            runCatching {
                val date = dateFormat.parse(serviceDateStr)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.add(Calendar.YEAR, 1)
                    val newInspectionDate = dateFormat.format(cal.time)
                    
                    val updatedVehicle = vehicle.copy(inspectionDate = newInspectionDate)
                    vehicleDao.updateVehicle(updatedVehicle)
                }
            }
        }
    }
}
