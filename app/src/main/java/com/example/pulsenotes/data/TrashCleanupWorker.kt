package com.example.pulsenotes.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pulsenotes.loadNotesFromDisk
import com.example.pulsenotes.saveNotesToDisk
import java.util.concurrent.TimeUnit

class TrashCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val data = loadNotesFromDisk(applicationContext) ?: return Result.success()

            val notes = data.first.toMutableList()
            val trashNotes = data.second.toMutableList()
            val customTags = data.third

            val now = System.currentTimeMillis()
            val limitMs = 30L * 24 * 60 * 60 * 1000L

            val beforePurge = trashNotes.size
            trashNotes.removeAll { note ->
                val deletedTime = note.deletedAt ?: now
                (now - deletedTime) >= limitMs
            }
            val purgedCount = beforePurge - trashNotes.size

            if (purgedCount > 0) {
                saveNotesToDisk(applicationContext, notes, trashNotes, customTags)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "trash_cleanup_daily"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}