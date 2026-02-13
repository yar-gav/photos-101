package com.example.photos101.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.photos101.R
import com.example.photos101.data.local.ActiveSearchPollStateDataSource
import com.example.photos101.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val CHANNEL_ID = "photos_poll"
private const val NOTIFICATION_ID = 1
private const val LOG_TAG = "PhotosPollWorker"

/**
 * Background worker that runs every 15 minutes only when there is an active search:
 * fetches first page for the persisted search query, compares with persisted result,
 * posts a notification if there are new photos, then updates persisted state.
 */
class PhotosPollWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: PhotoRepository,
    private val activeSearchPollStateDataSource: ActiveSearchPollStateDataSource,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(LOG_TAG, "doWork: starting")
        val state = activeSearchPollStateDataSource.getActiveSearchPollState() ?: run {
            Log.d(LOG_TAG, "doWork: no poll state, skipping")
            return@withContext Result.success()
        }
        if (state.activeSearchQuery.isBlank() || state.firstPagePhotoIds.isEmpty()) {
            Log.d(LOG_TAG, "doWork: no active search or empty ids, skipping")
            return@withContext Result.success()
        }
        Log.d(LOG_TAG, "doWork: polling for query='${state.activeSearchQuery}', previous first-page size=${state.firstPagePhotoIds.size}")

        val result = repository.searchPhotos(
            query = state.activeSearchQuery,
            page = 1,
            perPage = FIRST_PAGE_SIZE,
        )

        result.fold(
            onSuccess = { paged ->
                val newIds = paged.items.map { it.id }
                val previousIds = state.firstPagePhotoIds.toSet()
                val addedIds = newIds.filter { it !in previousIds }
                Log.d(LOG_TAG, "doWork: fetched ${newIds.size} ids, ${addedIds.size} new since last run")
                if (addedIds.isNotEmpty()) {
                    ensureNotificationChannel()
                    showNotification(applicationContext, addedIds.size)
                }
                activeSearchPollStateDataSource.savePollState(state.activeSearchQuery, newIds)
                Result.success()
            },
            onFailure = { t ->
                Log.w(LOG_TAG, "doWork: fetch failed", t)
                Result.retry()
            },
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_photos_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = applicationContext.getString(R.string.notification_channel_photos_description) }
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, newCount: Int) {
        val title = context.getString(R.string.notification_new_photos_title)
        val text = context.getString(R.string.notification_new_photos_search, newCount)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val FIRST_PAGE_SIZE = 30
    }
}
