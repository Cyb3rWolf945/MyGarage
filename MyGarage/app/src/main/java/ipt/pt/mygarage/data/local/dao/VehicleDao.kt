package ipt.pt.mygarage.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ipt.pt.mygarage.data.local.entity.PartEntity
import ipt.pt.mygarage.data.local.entity.PieceEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogEntity
import ipt.pt.mygarage.data.local.entity.ServiceLogPieceCrossRef
import ipt.pt.mygarage.data.local.entity.VehicleEntity
import ipt.pt.mygarage.data.local.relation.ServiceLogWithParts
import ipt.pt.mygarage.data.local.relation.ServiceLogWithPieces
import ipt.pt.mygarage.data.local.relation.VehicleWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Data Access Object for vehicle, service log, and piece operations.
 */
@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    suspend fun getVehicleById(vehicleId: String): VehicleEntity?

    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceLog(serviceLog: ServiceLogEntity)

    @Transaction
    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPiece(piece: PieceEntity)

    @Query("SELECT * FROM pieces")
    fun getAllPieces(): Flow<List<PieceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceLogPieceCrossRef(crossRef: ServiceLogPieceCrossRef)

    @Transaction
    @Query("SELECT * FROM service_logs WHERE id = :serviceLogId")
    fun getServiceLogWithPieces(serviceLogId: UUID): Flow<ServiceLogWithPieces>

    // PartEntity operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: PartEntity)

    @Transaction
    @Query("SELECT * FROM service_logs WHERE id = :serviceLogId")
    fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts>
}
