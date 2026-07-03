package pt.ipt.dama2026.mygarage.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import pt.ipt.dama2026.mygarage.data.repository.SyncRepository
import pt.ipt.dama2026.mygarage.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun syncRepo(): SyncRepository
    fun prefsRepo(): UserPreferencesRepository
}

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val syncRepo: SyncRepository
    private val prefs: UserPreferencesRepository

    init {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SyncWorkerEntryPoint::class.java)
        syncRepo = entryPoint.syncRepo()
        prefs = entryPoint.prefsRepo()
    }

    override suspend fun doWork(): Result {
        val prefsData = prefs.userPreferencesFlow.firstOrNull() ?: return if (runAttemptCount < 3) Result.retry() else Result.failure()

        val result = when {
            prefsData.requiresGuestMerge -> syncRepo.syncWithGuestMerge()
            prefsData.lastSyncTimestamp == null && !prefsData.authToken.isNullOrEmpty() -> syncRepo.syncWithOfflineFallback()
            else -> syncRepo.fullSync()
        }

        if (!prefsData.authToken.isNullOrEmpty()) {
            syncRepo.pullAndSyncUserProfile()
        }

        return result.fold(
            onSuccess = { Result.success() },
            onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() }
        )
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val UNIQUE_WORK_NAME = "mygarage_sync"

        fun enqueueOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME + "_onetime",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun enqueueGuestMergeSyncWorker(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag("guest_merge")
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME + "_guest_merge",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun enqueueOfflineReinitSyncWorker(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag("offline_reinit")
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME + "_offline_reinit",
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }

        fun enqueuePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                6, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
