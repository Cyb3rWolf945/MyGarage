package pt.ipt.dama2026.mygarage.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * DAO central para veículos, serviços e peças.
 * As funções com suspend servem para que o codigo possa correr em background numa thread a parte pelo Room.
 * As que devolvem Flow são reativas: emitem atualizações sempre que a tabela muda,
 * para que a UI atualize o seu estado.
 * O filtro isDeleted = 0 esconde registos apagados da UI sem os remover da BD ( soft-delete ).
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
    suspend fun insertPart(part: PartEntity)

    @Transaction
    @Query("SELECT * FROM service_logs WHERE id = :serviceLogId")
    fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts>

    @Update
    suspend fun updateServiceLog(serviceLog: ServiceLogEntity)

    @Query("SELECT * FROM service_logs WHERE id = :id")
    suspend fun getServiceLogById(id: UUID): ServiceLogEntity?

    @Query("DELETE FROM service_parts WHERE serviceLogId = :serviceId")
    suspend fun deletePartsByServiceId(serviceId: String)

    @Query("UPDATE service_parts SET isDeleted = 1, updatedAt = :now WHERE serviceLogId = :serviceId AND isDeleted = 0")
    suspend fun softDeletePartsByServiceId(serviceId: String, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteServiceLog(serviceLog: ServiceLogEntity)

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesList(): List<VehicleEntity>

    @Query("SELECT * FROM service_logs")
    suspend fun getAllServiceLogsList(): List<ServiceLogEntity>

    @Query("SELECT * FROM service_parts")
    suspend fun getAllPartsList(): List<PartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicles(vehicles: List<VehicleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServiceLogs(logs: List<ServiceLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParts(parts: List<PartEntity>)
}
