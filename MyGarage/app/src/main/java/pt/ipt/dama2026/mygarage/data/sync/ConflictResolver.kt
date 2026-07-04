package pt.ipt.dama2026.mygarage.data.sync

import pt.ipt.dama2026.mygarage.data.local.entity.PartEntity
import pt.ipt.dama2026.mygarage.data.local.entity.ServiceLogEntity
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.model.PartPayload
import pt.ipt.dama2026.mygarage.data.model.ServiceLogPayload
import pt.ipt.dama2026.mygarage.data.model.VehiclePayload
import pt.ipt.dama2026.mygarage.data.model.parseIso8601
import pt.ipt.dama2026.mygarage.data.model.*

/**
 * Resolve conflitos entre dados locais e remotos durante a sincronização.
 *
 * Estratégia: last-write-wins (o último a gravar ganha).
 * Compara o campo updatedAt de cada registo — se a versão remota for mais
 * recente, substitui a local. Se a versão local for mais recente, mantém-se.
 * Se um registo só existir de um lado, é mantido.
 *
 * Usado pelo SyncRepository nos cenários de guest merge e offline fallback.
 *
 * Nota: este resolvedor NÃO sabe nada de rede ou BD — só recebe listas
 * e devolve a lista merged. É uma função pura e não tem dependências.
 */
object ConflictResolver {

    /**
     * Junta veículos locais e remotos.
     *
     * Lógica:
     * 1. Põe todos os locais num mapa (chave = ID).
     * 2. Para cada veículo remoto:
     *    - Se não existir localmente → adiciona.
     *    - Se existir e o remote.updatedAt > local.updatedAt → substitui.
     *    - Se o local for mais recente → ignora o remoto.
     */
    fun mergeVehicles(
        local: List<VehicleEntity>,
        remote: List<VehiclePayload>
    ): List<VehicleEntity> {
        val merged = mutableMapOf<String, VehicleEntity>()
        local.forEach { merged[it.id] = it }
        
        remote.forEach { remoteVehicle ->
            val localVehicle = merged[remoteVehicle.id]
            val remoteTime = parseIso8601(remoteVehicle.updatedAt)
            
            if (localVehicle == null || remoteTime > localVehicle.updatedAt) {
                merged[remoteVehicle.id] = remoteVehicle.toEntity()
            }
        }
        return merged.values.toList()
    }

    /**
     * Junta serviços locais e remotos.
     *
     * Lógica (igual à dos veículos):
     * 1. Põe todos os locais num mapa (chave = ID).
     * 2. Para cada serviço remoto:
     *    - Se não existir localmente → adiciona.
     *    - Se existir e remote.updatedAt > local.updatedAt → substitui.
     *    - Se o local for mais recente → ignora o remoto.
     */
    fun mergeServiceLogs(
        local: List<ServiceLogEntity>,
        remote: List<ServiceLogPayload>
    ): List<ServiceLogEntity> {
        val merged = mutableMapOf<String, ServiceLogEntity>()
        local.forEach { merged[it.id.toString()] = it }
        
        remote.forEach { remoteLog ->
            val localLog = merged[remoteLog.id]
            val remoteTime = parseIso8601(remoteLog.updatedAt)
            
            if (localLog == null || remoteTime > localLog.updatedAt) {
                merged[remoteLog.id] = remoteLog.toEntity()
            }
        }
        return merged.values.toList()
    }

    /**
     * Junta peças locais e remotas.
     *
     * Lógica (igual à dos veículos e serviços):
     * 1. Põe todos os locais num mapa (chave = ID).
     * 2. Para cada peça remota:
     *    - Se não existir localmente → adiciona.
     *    - Se existir e remote.updatedAt > local.updatedAt → substitui.
     *    - Se o local for mais recente → ignora a remota.
     */
    fun mergeParts(
        local: List<PartEntity>,
        remote: List<PartPayload>
    ): List<PartEntity> {
        val merged = mutableMapOf<String, PartEntity>()
        local.forEach { merged[it.id] = it }
        
        remote.forEach { remotePart ->
            val localPart = merged[remotePart.id]
            val remoteTime = parseIso8601(remotePart.updatedAt)
            
            if (localPart == null || remoteTime > localPart.updatedAt) {
                merged[remotePart.id] = remotePart.toEntity()
            }
        }
        return merged.values.toList()
    }
}
