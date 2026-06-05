package ipt.pt.mygarage.domain.repository

import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.ServiceLogWithPieces
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Domain layer repository interface.
 */
interface VehicleRepository {

    suspend fun insertVehicle(vehicle: VehicleEntity)

    suspend fun updateVehicle(vehicle: VehicleEntity)

    fun getAllVehicles(): Flow<List<VehicleEntity>>

    fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices>

    fun getAllPieces(): Flow<List<PieceEntity>>

    suspend fun insertServiceLog(serviceLog: ServiceLogEntity)

    suspend fun insertServiceLogWithPieces(serviceLog: ServiceLogEntity, pieces: List<ServiceLogPieceCrossRef>)

    fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces>
}
