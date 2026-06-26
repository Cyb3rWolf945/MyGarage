package pt.ipt.dama2026.mygarage.domain.repository

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.PieceEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogPieceCrossRef
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithPieces
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Domain layer repository interface.
 */
interface VehicleRepository {

    suspend fun insertVehicle(vehicle: VehicleEntity)

    suspend fun updateVehicle(vehicle: VehicleEntity)

    suspend fun deleteVehicle(vehicle: VehicleEntity)

    fun getAllVehicles(): Flow<List<VehicleEntity>>

    fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices>

    fun getAllPieces(): Flow<List<PieceEntity>>

    suspend fun insertServiceLog(serviceLog: ServiceLogEntity)

    suspend fun insertServiceLogWithPieces(serviceLog: ServiceLogEntity, pieces: List<ServiceLogPieceCrossRef>)

    fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces>

    suspend fun insertPart(part: PartEntity)

    fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts>

    suspend fun updateServiceLogWithParts(serviceLog: ServiceLogEntity, parts: List<PartEntity>)

    suspend fun deleteServiceLog(serviceLog: ServiceLogEntity)
}
