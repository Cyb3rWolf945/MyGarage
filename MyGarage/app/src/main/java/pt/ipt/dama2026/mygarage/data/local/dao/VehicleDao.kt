package pt.ipt.dama2026.mygarage.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
 * Data Access Object for vehicle, service log, and piece operations.
 */
@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId AND isDeleted = 0")
    suspend fun getVehicleById(vehicleId: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE isDeleted = 0")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceLog(serviceLog: ServiceLogEntity)

    @Transaction
    @Query("SELECT * FROM vehicles WHERE id = :vehicleId AND isDeleted = 0")
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

    @Update
    suspend fun updateServiceLog(serviceLog: ServiceLogEntity)

    @Query("DELETE FROM service_parts WHERE serviceLogId = :serviceId")
    suspend fun deletePartsByServiceId(serviceId: String)

    @Delete
    suspend fun deleteServiceLog(serviceLog: ServiceLogEntity)

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesList(): List<VehicleEntity>

    @Query("SELECT * FROM service_logs")
    suspend fun getAllServiceLogsList(): List<ServiceLogEntity>

    @Query("SELECT * FROM service_parts")
    suspend fun getAllPartsList(): List<PartEntity>

    @Query("SELECT * FROM pieces")
    suspend fun getAllPiecesList(): List<PieceEntity>

    @Query("SELECT * FROM service_log_pieces")
    suspend fun getAllCrossRefsList(): List<ServiceLogPieceCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicles(vehicles: List<VehicleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServiceLogs(logs: List<ServiceLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParts(parts: List<PartEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPieces(pieces: List<PieceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRefs(refs: List<ServiceLogPieceCrossRef>)
}
