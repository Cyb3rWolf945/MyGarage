package pt.ipt.dama2026.mygarage.domain.repository

import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import kotlinx.coroutines.flow.Flow

/**
 * Interface de domínio para operações com veículos, serviços e peças.
 *
 * Define o contrato que a camada de dados tem de cumprir.
 * Só depende de modelos de domínio — nunca de entidades Room.
 *
 * Implementação concreta: OfflineVehicleRepository.
 * Ligação feita via Hilt no RepositoryModule (@Binds).
 */
interface VehicleRepository {

    suspend fun insertVehicle(vehicle: Vehicle)

    suspend fun updateVehicle(vehicle: Vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle)

    suspend fun getVehicleById(vehicleId: String): Vehicle?

    fun getAllVehicles(): Flow<List<Vehicle>>

    fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices>

    suspend fun insertServiceLog(serviceLog: ServiceLog)

    suspend fun insertPart(part: Part)

    fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts>

    suspend fun updateServiceLogWithParts(serviceLog: ServiceLog, parts: List<Part>)

    suspend fun deleteServiceLog(serviceLog: ServiceLog)
}
