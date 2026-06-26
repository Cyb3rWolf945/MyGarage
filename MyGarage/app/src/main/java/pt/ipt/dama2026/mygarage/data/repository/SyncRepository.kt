package pt.ipt.dama2026.mygarage.data.repository

import android.content.Context
import pt.ipt.dama2026.mygarage.data.local.db.AppDatabase
import pt.ipt.dama2026.mygarage.data.local.entity.VehicleEntity
import pt.ipt.dama2026.mygarage.data.model.SyncPushBody
import pt.ipt.dama2026.mygarage.data.model.toEntity
import pt.ipt.dama2026.mygarage.data.model.toPayload
import pt.ipt.dama2026.mygarage.data.network.NetworkModule
import pt.ipt.dama2026.mygarage.data.storage.LocalImageStorageManager
import pt.ipt.dama2026.mygarage.data.sync.ConflictResolver
import pt.ipt.dama2026.mygarage.domain.repository.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SyncRepository(private val context: Context) {

    private val api = NetworkModule.createSyncApiService(context)
    private val dao = AppDatabase.getDatabase(context).vehicleDao()
    private val prefs = UserPreferencesRepository(context)
    private val imageStorage: ImageStorageManager = LocalImageStorageManager(context)

    suspend fun pushAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = SyncPushBody(
                vehicles = dao.getAllVehiclesList().map { it.toPayload() }.ifEmpty { null },
                services = dao.getAllServiceLogsList().map { it.toPayload() }.ifEmpty { null },
                parts = dao.getAllPartsList().map { it.toPayload() }.ifEmpty { null },
                pieces = dao.getAllPiecesList().map { it.toPayload() }.ifEmpty { null },
                servicePieceCrossRefs = dao.getAllCrossRefsList().map { it.toPayload() }.ifEmpty { null }
            )
            val response = api.push(body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(SyncException("Push failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(SyncException("Push failed: ${e.message}"))
        }
    }

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
            if (data.pieces.isNotEmpty()) dao.upsertPieces(data.pieces.map { it.toEntity() })
            if (data.servicePieceCrossRefs.isNotEmpty()) dao.upsertCrossRefs(data.servicePieceCrossRefs.map { it.toEntity() })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SyncException("Pull failed: ${e.message}"))
        }
    }

    suspend fun fullSync(): Result<Unit> {
        val pushResult = pushAll()
        if (pushResult.isFailure) return pushResult

        val profileResult = pushUserProfile()
        if (profileResult.isFailure) return profileResult

        val pullResult = pullAll()
        if (pullResult.isFailure) return pullResult

        // Download remote images for offline access
        downloadMissingImages()

        // Pull & download user profile avatar
        pullAndSyncUserProfile()

        prefs.setLastSyncTimestamp(System.currentTimeMillis())
        return Result.success(Unit)
    }

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
     * Pulls the user profile from the backend and downloads the avatar
     * if a remote URL exists but no local copy is present.
     */
    suspend fun pullAndSyncUserProfile(): Unit = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (!response.isSuccessful || response.body() == null) return@withContext

            val profile = response.body()!!
            val remoteUrl = profile.avatarUrl

            if (!remoteUrl.isNullOrBlank()) {
                // Store the remote URL locally
                prefs.updateAvatarRemoteUrl(remoteUrl)

                // Download avatar if we don't have a local copy yet
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
            // Non-critical — avatar will be pulled on next sync
        }
    }

    suspend fun syncWithGuestMerge(): Result<Unit> = withContext(Dispatchers.IO) {
        val pushResult = pushAll()
        if (pushResult.isFailure) return@withContext pushResult

        try {
            val response = api.pullInitial(null)
            if (!response.isSuccessful) {
                return@withContext Result.failure(SyncException("Pull initial failed: ${response.code()}"))
            }

            val remote = response.body() ?: return@withContext Result.success(Unit)
            val local = dao.getAllVehiclesList()
            val localLogs = dao.getAllServiceLogsList()
            val localParts = dao.getAllPartsList()
            val localPieces = dao.getAllPiecesList()
            val localCrossRefs = dao.getAllCrossRefsList()

            val mergedVehicles = ConflictResolver.mergeVehicles(local, remote.vehicles)
            val mergedLogs = ConflictResolver.mergeServiceLogs(localLogs, remote.services)
            val mergedParts = ConflictResolver.mergeParts(localParts, remote.parts)
            val mergedPieces = ConflictResolver.mergePieces(localPieces, remote.pieces)
            val mergedCrossRefs = ConflictResolver.mergeCrossRefs(localCrossRefs, remote.servicePieceCrossRefs)

            if (mergedVehicles.isNotEmpty()) dao.upsertVehicles(mergedVehicles)
            if (mergedLogs.isNotEmpty()) dao.upsertServiceLogs(mergedLogs)
            if (mergedParts.isNotEmpty()) dao.upsertParts(mergedParts)
            if (mergedPieces.isNotEmpty()) dao.upsertPieces(mergedPieces)
            if (mergedCrossRefs.isNotEmpty()) dao.upsertCrossRefs(mergedCrossRefs)

            pushUserProfile()

            // Download remote images for offline access
            downloadMissingImages()

            // Pull & download user profile avatar
            pullAndSyncUserProfile()

            prefs.setRequiresGuestMerge(false)
            prefs.setLastSyncTimestamp(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SyncException("Guest merge failed: ${e.message}"))
        }
    }

    suspend fun syncWithOfflineFallback(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = try {
                api.pullInitial(null)
            } catch (e: Exception) {
                return@withContext Result.success(Unit)
            }

            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                val local = dao.getAllVehiclesList()
                val localLogs = dao.getAllServiceLogsList()
                val localParts = dao.getAllPartsList()
                val localPieces = dao.getAllPiecesList()
                val localCrossRefs = dao.getAllCrossRefsList()

                val mergedVehicles = ConflictResolver.mergeVehicles(local, remote.vehicles)
                val mergedLogs = ConflictResolver.mergeServiceLogs(localLogs, remote.services)
                val mergedParts = ConflictResolver.mergeParts(localParts, remote.parts)
                val mergedPieces = ConflictResolver.mergePieces(localPieces, remote.pieces)
                val mergedCrossRefs = ConflictResolver.mergeCrossRefs(localCrossRefs, remote.servicePieceCrossRefs)

                if (mergedVehicles.isNotEmpty()) dao.upsertVehicles(mergedVehicles)
                if (mergedLogs.isNotEmpty()) dao.upsertServiceLogs(mergedLogs)
                if (mergedParts.isNotEmpty()) dao.upsertParts(mergedParts)
                if (mergedPieces.isNotEmpty()) dao.upsertPieces(mergedPieces)
                if (mergedCrossRefs.isNotEmpty()) dao.upsertCrossRefs(mergedCrossRefs)

                pushUserProfile()

                // Download remote images for offline access
                downloadMissingImages()

                // Pull & download user profile avatar
                pullAndSyncUserProfile()

                prefs.setLastSyncTimestamp(System.currentTimeMillis())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    private fun iso8601(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(epochMillis))
    }

    /**
     * Downloads remote images for vehicles that have a remoteImageUrl
     * but no local copies yet. This enables offline-first: after a sync
     * pull fetches vehicle data, we cache the images locally so they
     * work without internet.
     */
    suspend fun downloadMissingImages(): Unit = withContext(Dispatchers.IO) {
        try {
            // Filter vehicles that have a remote URL but whose local files
            // are either missing or don't exist on disk (e.g., after reinstall).
            val allVehicles = dao.getAllVehiclesList()
                .filter { vehicle ->
                    if (vehicle.remoteImageUrl.isNullOrBlank()) return@filter false
                    // Check if ANY local file actually exists on disk
                    val hasLocalFile = vehicle.localImageFileNames.any { fileName ->
                        imageStorage.getImagePath(fileName) != null
                    }
                    !hasLocalFile
                }

            for (vehicle in allVehicles) {
                val remoteUrl = vehicle.remoteImageUrl!!.replace("\"", "")
                val proxyUrl = NetworkModule.buildImageProxyUrl(context, remoteUrl) ?: continue

                val fileName = imageStorage.downloadImage(proxyUrl) ?: continue

                // Add the downloaded file name to the vehicle's local images
                val updated = vehicle.copy(
                    localImageFileNames = listOf(fileName)
                )
                dao.updateVehicle(updated)
            }
        } catch (_: Exception) {
            // Non-critical — images will be downloaded on next sync attempt
        }
    }

    class SyncException(message: String) : Exception(message)
}
