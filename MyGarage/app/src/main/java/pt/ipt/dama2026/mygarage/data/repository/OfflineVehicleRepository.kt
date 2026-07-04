package pt.ipt.dama2026.mygarage.data.repository

import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.local.relation.ServiceLogWithParts as EntityServiceLogWithParts
import pt.ipt.dama2026.mygarage.data.local.relation.VehicleWithServices as EntityVehicleWithServices
import pt.ipt.dama2026.mygarage.data.mapper.toDomain
import pt.ipt.dama2026.mygarage.data.mapper.toEntity
import pt.ipt.dama2026.mygarage.domain.model.Part
import pt.ipt.dama2026.mygarage.domain.model.ServiceLog
import pt.ipt.dama2026.mygarage.domain.model.ServiceLogWithParts
import pt.ipt.dama2026.mygarage.domain.model.Vehicle
import pt.ipt.dama2026.mygarage.domain.model.VehicleWithServices
import pt.ipt.dama2026.mygarage.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação local do repositório de veículos, usando Room.
 *
 * É a ponte entre a UI e a base de dados:
 * - A UI chama métodos com modelos de domínio (Vehicle, ServiceLog, Part).
 * - Este repositório converte para entidades Room (via mappers), chama o DAO,
 *   e converte os resultados de volta para modelos de domínio.
 * - A UI nunca vê uma entidade Room — não sabe que a BD existe.
 *
 * Também trata de regras de negócio:
 * - Soft-delete (marca isDeleted em vez de apagar).
 * - Efeitos colaterais de serviços (ex.: inspeção avança a data da próxima em 1 ano).
 */
@Singleton
class OfflineVehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : VehicleRepository {

    /** Converte o modelo de domínio para entidade e insere na BD. */
    override suspend fun insertVehicle(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle.toEntity())
    }

    /**
     * Se o veículo já existe, faz copy com os novos dados e atualiza o timestamp.
     * Se não existe, insere como novo.
     */
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

    /** Marca o veículo como apagado (isDeleted = true) e regista o ID para o sync ignorar. */
    override suspend fun deleteVehicle(vehicle: Vehicle) {
        userPreferencesRepository?.markVehicleDeleted(vehicle.id)
        val existing = vehicleDao.getVehicleById(vehicle.id) ?: return
        vehicleDao.updateVehicle(existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
    }

    /** Pesquisa veículo por ID e converte para modelo de domínio. */
    override suspend fun getVehicleById(vehicleId: String): Vehicle? {
        return vehicleDao.getVehicleById(vehicleId)?.toDomain()
    }


    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { list -> list.map { it.toDomain() } }
    }

    override fun getVehicleWithServices(vehicleId: String): Flow<VehicleWithServices> {
        return vehicleDao.getVehicleWithServices(vehicleId).map { it.toDomain() }
    }

    /** Insere serviço e aplica efeitos colaterais (ex.: avançar data de inspeção). */
    override suspend fun insertServiceLog(serviceLog: ServiceLog) {
        val entity = serviceLog.toEntity()
        vehicleDao.insertServiceLog(entity)
        handleServiceLogSideEffects(entity)
    }

    /** Insere uma peça associada a um serviço. */
    override suspend fun insertPart(part: Part) {
        vehicleDao.insertPart(part.toEntity())
    }

    override fun getServiceLogWithParts(serviceLogId: String): Flow<ServiceLogWithParts> {
        return vehicleDao.getServiceLogWithParts(serviceLogId).map { it.toDomain() }
    }

    /**
     * Atualiza o serviço, apaga as peças antigas (soft-delete) e insere as novas.
     */
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
                mileageKm = serviceLog.mileageKm,
                type = serviceLog.type,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            serviceLog.toEntity().copy(updatedAt = System.currentTimeMillis())
        }
        vehicleDao.updateServiceLog(entity)
        vehicleDao.softDeletePartsByServiceId(serviceLog.id.toString())
        parts.forEach { vehicleDao.insertPart(it.toEntity()) }
        handleServiceLogSideEffects(entity)
    }

    /** Marca o serviço e as peças como apagados (soft-delete). */
    override suspend fun deleteServiceLog(serviceLog: ServiceLog) {
        val existing = vehicleDao.getServiceLogById(serviceLog.id) ?: return
        vehicleDao.updateServiceLog(existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
        vehicleDao.softDeletePartsByServiceId(serviceLog.id.toString())
    }

    /**
     * Aplica regras de negócio com base no tipo de serviço.
     * Chamada após um insert de uma manutenção nova ou update.
     *
     * Se for "Inspection", avança a data da próxima inspeção do veículo em 1 ano.
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
