package ipt.pt.mygarage.data.repository

import ipt.pt.mygarage.data.local.dao.VehicleDao
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.ServiceLogWithParts
import ipt.pt.mygarage.data.local.relation.ServiceLogWithPieces
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import ipt.pt.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Data layer implementation of [VehicleRepository] delegating to [VehicleDao].
 * Applies business rules for database updates upon registering new service logs.
 */
class OfflineVehicleRepository(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insertVehicle(vehicle)
    }

    override suspend fun updateVehicle(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)
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

    /**
     * Executes business rules based on the type of registered service.
     */
    private suspend fun handleServiceLogSideEffects(serviceLog: ServiceLogEntity) {
        if (serviceLog.type.equals("revision", ignoreCase = true)) {
            // Rule: Increase 4 349.59835 miles to mileageToNextService
            val vehicle = vehicleDao.getVehicleById(serviceLog.vehicleId) ?: return
            
            val currentMileageStr = vehicle.mileageToNextService ?: return
            // Clean non-numeric characters (except decimals/periods)
            val cleanStr = currentMileageStr.replace(",", "").replace(Regex("[^0-9.]"), "")
            val currentMileage = cleanStr.toDoubleOrNull() ?: 0.0
            
            val newMileage = currentMileage + 4349.59835
            // Format back retaining comma formatting (e.g. "12,549.59835 mi")
            val formattedMileage = String.format(Locale.US, "%,.5f mi", newMileage)
            
            val updatedVehicle = vehicle.copy(mileageToNextService = formattedMileage)
            vehicleDao.updateVehicle(updatedVehicle)
            
        } else if (serviceLog.type.equals("Inspection", ignoreCase = true)) {
            // Rule: Update inspectionDate to a year after the register service date
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
