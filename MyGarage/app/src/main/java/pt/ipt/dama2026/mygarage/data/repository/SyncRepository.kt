package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ipt.dama2026.mygarage.data.local.dao.VehicleDao
import pt.ipt.dama2026.mygarage.data.model.SyncPushBody
import pt.ipt.dama2026.mygarage.data.model.toEntity
import pt.ipt.dama2026.mygarage.data.model.toPayload
import pt.ipt.dama2026.mygarage.data.network.NetworkModule
import pt.ipt.dama2026.mygarage.data.network.SyncApiService
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import pt.ipt.dama2026.mygarage.data.sync.ConflictResolver
import pt.ipt.dama2026.mygarage.domain.locale.DateFormats
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Esta é a classe que o AuthRepository vai usar para realizar a sincronização entre a BD local e o servidor.
 *
 * Tipos de sync:
 * - fullSync: push → pull, usado no sync periódico normal.
 * - syncWithGuestMerge: push + pull completo + merge (quando há dados de guest).
 * - syncWithOfflineFallback: pull sem timestamp, ignora erros de rede.
 *
 * Também trata do sync de perfil (nome, garagem, avatar) e download
 * de imagens remotas que ainda não existem em localmente.
 */
@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: SyncApiService,
    private val dao: VehicleDao,
    private val prefs: UserPreferencesRepository,
    private val imageStorage: ImageStorageManager
) {
    /**
     * Local → Servidor
     *
     * Recolhe todas as entidades locais (veículos, serviços, peças),
     * converte cada uma para o formato JSON esperado pelo servidor
     * e envia tudo num único pedido. Se uma lista estiver vazia,
     * envia null em vez de array vazio.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun pushAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = SyncPushBody(
                vehicles = dao.getAllVehiclesList().map { it.toPayload() }.ifEmpty { null },
                services = dao.getAllServiceLogsList().map { it.toPayload() }.ifEmpty { null },
                parts = dao.getAllPartsList().map { it.toPayload() }.ifEmpty { null }
            )
            val response = api.push(body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(SyncException("Push failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(SyncException("Push failed: ${e.message}"))
        }
    }

    /**
     * Servidor → Local
     *
     * Pede ao servidor apenas os dados alterados desde o último sync
     * (usa o timestamp guardado no DataStore). Filtra veículos que o
     * utilizador apagou localmente para não os restaurar. No final,
     * insere ou atualiza tudo na BD local com upsert.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun pullAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val timestamp: Long? = runBlocking { prefs.lastSyncTimestampFlow.firstOrNull() }
            val lastSyncStr = timestamp?.let { iso8601(it) }

            val response = api.pull(lastSyncStr)
            if (!response.isSuccessful) {
                return@withContext Result.failure(SyncException("Pull failed: ${response.code()}"))
            }

            val data = response.body() ?: return@withContext Result.success(Unit)

            val deletedIds = prefs.getDeletedVehicleIds()
            if (data.vehicles.isNotEmpty()) {
                val filtered = data.vehicles.filter { it.id !in deletedIds }
                if (filtered.isNotEmpty()) dao.upsertVehicles(filtered.map { it.toEntity() })
            }
            if (data.services.isNotEmpty()) dao.upsertServiceLogs(data.services.map { it.toEntity() })
            if (data.parts.isNotEmpty()) dao.upsertParts(data.parts.map { it.toEntity() })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SyncException("Pull failed: ${e.message}"))
        }
    }

    /**
     * Local ↔ Servidor
     *
     * Sync normal (usado periodicamente e após login sem dados guest).
     * Ordem: envia locais → envia perfil → recebe remotos → download
     * de imagens em falta → puxa perfil do servidor. Se qualquer passo
     * falhar, para e devolve o erro. No final, atualiza o timestamp.
     */
    suspend fun fullSync(): Result<Unit> {
        val pushResult = pushAll()
        if (pushResult.isFailure) return pushResult

        val profileResult = pushUserProfile()
        if (profileResult.isFailure) return profileResult

        val pullResult = pullAll()
        if (pullResult.isFailure) return pullResult

        downloadMissingImages()
        pullAndSyncUserProfile()

        prefs.setLastSyncTimestamp(System.currentTimeMillis())
        return Result.success(Unit)
    }

    /**
     * Local → Servidor
     *
     * Lê os dados atuais do perfil no DataStore (nome, garagem, avatar)
     * e envia para o servidor. Se não houver dados de perfil, devolve
     * sucesso sem fazer nada.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    private suspend fun pushUserProfile(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val prefsData = prefs.userPreferencesFlow.firstOrNull() ?: return@withContext Result.success(Unit)
            val profile = pt.ipt.dama2026.mygarage.data.network.UserProfileUpdate(
                name = prefsData.userName,
                garageName = prefsData.garageName,
                avatarUrl = prefsData.avatarRemoteUrl
            )
            val response = api.updateProfile(profile)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(SyncException("Profile sync failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(SyncException("Profile sync failed: ${e.message}"))
        }
    }

    /**
     * Servidor → Local
     *
     * Pede o perfil ao servidor (nome, garagem, avatar).
     * Se o avatar remoto existir, guarda o URL no DataStore.
     * Depois verifica se já há ficheiro local em cache — se não houver,
     * faz download através do proxy do backend e guarda o nome do ficheiro.
     * Erros são ignorados (não bloqueiam o sync).
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun pullAndSyncUserProfile(): Unit = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (!response.isSuccessful || response.body() == null) return@withContext

            val profile = response.body()!!
            val remoteUrl = profile.avatarUrl

            if (!remoteUrl.isNullOrBlank()) {
                prefs.updateAvatarRemoteUrl(remoteUrl)
                val currentPrefs = runBlocking { prefs.userPreferencesFlow.firstOrNull() }
                val hasLocalAvatar = currentPrefs?.avatarFileName?.let { fileName ->
                    imageStorage.getImagePath(fileName) != null
                } ?: false

                if (!hasLocalAvatar) {
                    val proxyUrl = NetworkModule.buildImageProxyUrl(context, remoteUrl.replace("\"", ""))
                    if (proxyUrl != null) {
                        val fileName = imageStorage.downloadImage(proxyUrl)
                        if (fileName != null) {
                            prefs.updateAvatarFileName(fileName)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Local ↔ Servidor
     *
     * Sync para utilizadores que criaram dados em modo guest e depois
     * fizeram login/registo.
     *
     * Passos:
     * 1. Local → Servidor: envia todos os dados locais.
     * 2. Servidor → Local: puxa TUDO do servidor (sem timestamp).
     * 3. Junta local + remoto com ConflictResolver (last-write-wins).
     * 4. Envia perfil, descarrega imagens, puxa perfil remoto.
     * 5. Limpa a flag requiresGuestMerge e atualiza o timestamp.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun syncWithGuestMerge(): Result<Unit> = withContext(Dispatchers.IO) {
        val pushResult = pushAll()
        if (pushResult.isFailure) return@withContext pushResult

        try {
            val response = api.pull(null)
            if (!response.isSuccessful) {
                return@withContext Result.failure(SyncException("Pull initial failed: ${response.code()}"))
            }

            val remote = response.body() ?: return@withContext Result.success(Unit)
            val local = dao.getAllVehiclesList()
            val localLogs = dao.getAllServiceLogsList()
            val localParts = dao.getAllPartsList()

            val mergedVehicles = ConflictResolver.mergeVehicles(local, remote.vehicles)
            val mergedLogs = ConflictResolver.mergeServiceLogs(localLogs, remote.services)
            val mergedParts = ConflictResolver.mergeParts(localParts, remote.parts)

            if (mergedVehicles.isNotEmpty()) dao.upsertVehicles(mergedVehicles)
            if (mergedLogs.isNotEmpty()) dao.upsertServiceLogs(mergedLogs)
            if (mergedParts.isNotEmpty()) dao.upsertParts(mergedParts)

            pushUserProfile()
            downloadMissingImages()
            pullAndSyncUserProfile()

            prefs.setRequiresGuestMerge(false)
            prefs.setLastSyncTimestamp(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SyncException("Guest merge failed: ${e.message}"))
        }
    }

    /**
     * Servidor → Local
     *
     * Sync para o primeiro login (quando não há timestamp de sync anterior).
     * Tenta fazer pull completo do servidor. Se não houver rede ou o pedido
     * falhar, simplesmente devolve sucesso — a app continua a funcionar offline.
     * Se o pull funcionar, aplica merge e atualiza perfil + imagens.
     * NUNCA devolve erro, porque o utilizador não deve ser bloqueado por
     * falhas de rede no primeiro login.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun syncWithOfflineFallback(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = try {
                api.pull(null)
            } catch (e: Exception) {
                return@withContext Result.success(Unit)
            }

            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                val local = dao.getAllVehiclesList()
                val localLogs = dao.getAllServiceLogsList()
                val localParts = dao.getAllPartsList()

                val mergedVehicles = ConflictResolver.mergeVehicles(local, remote.vehicles)
                val mergedLogs = ConflictResolver.mergeServiceLogs(localLogs, remote.services)
                val mergedParts = ConflictResolver.mergeParts(localParts, remote.parts)

                if (mergedVehicles.isNotEmpty()) dao.upsertVehicles(mergedVehicles)
                if (mergedLogs.isNotEmpty()) dao.upsertServiceLogs(mergedLogs)
                if (mergedParts.isNotEmpty()) dao.upsertParts(mergedParts)

                pushUserProfile()
                downloadMissingImages()
                pullAndSyncUserProfile()

                prefs.setLastSyncTimestamp(System.currentTimeMillis())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    private fun iso8601(epochMillis: Long): String = DateFormats.ISO_8601.format(java.util.Date(epochMillis))

    /**
     * Servidor → Local
     *
     * Encontra veículos que têm imagem remota (remoteImageUrl) mas ainda
     * não têm o ficheiro descarregado localmente. Para cada um:
     * 1. Constrói URL de proxy (para não expor o bucket S3).
     * 2. Faz download e guarda em internal storage.
     * 3. Atualiza a entidade com o nome do ficheiro local.
     * Erros são ignorados — se uma imagem falhar, tenta no próximo sync.
     * Dispatchers.IO vai garantir que o pedido de rede não bloqueia a UI.
     * Em caso de erro, devolve Result.failure com SyncException.
     */
    suspend fun downloadMissingImages(): Unit = withContext(Dispatchers.IO) {
        try {
            val allVehicles = dao.getAllVehiclesList()
                .filter { vehicle ->
                    if (vehicle.remoteImageUrl.isNullOrBlank()) return@filter false
                    val hasLocalFile = vehicle.localImageFileNames.any { fileName ->
                        imageStorage.getImagePath(fileName) != null
                    }
                    !hasLocalFile
                }

            for (vehicle in allVehicles) {
                val remoteUrl = vehicle.remoteImageUrl!!.replace("\"", "")
                val proxyUrl = NetworkModule.buildImageProxyUrl(context, remoteUrl) ?: continue

                val fileName = imageStorage.downloadImage(proxyUrl) ?: continue

                val updated = vehicle.copy(
                    localImageFileNames = listOf(fileName)
                )
                dao.updateVehicle(updated)
            }
        } catch (_: Exception) {
        }
    }

    /** Exceção tipificada para erros de sincronização. */
    class SyncException(message: String) : Exception(message)
}
