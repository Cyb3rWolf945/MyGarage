package pt.ipt.dama2026.mygarage.domain.repository

import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.Piece
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogCrossRef
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithPieces
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Domain layer repository interface — depends only on domain models.
 */
interface VehicleRepository {

    suspend fun insertVehicle(vehicle: Vehicle)

    suspend fun updateVehicle(vehicle: Vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle)

    suspend fun getVehicleById(vehicleId: String): Vehicle?

    fun getAllVehicles(): Flow<List<Vehicle>>

    fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices>

    fun getAllPieces(): Flow<List<Piece>>

    suspend fun insertServiceLog(serviceLog: ServiceLog)

    suspend fun insertServiceLogWithPieces(serviceLog: ServiceLog, pieces: List<ServiceLogCrossRef>)

    fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces>

    suspend fun insertPart(part: Part)

    fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts>

    suspend fun updateServiceLogWithParts(serviceLog: ServiceLog, parts: List<Part>)

    suspend fun deleteServiceLog(serviceLog: ServiceLog)
}
