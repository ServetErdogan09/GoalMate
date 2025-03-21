package com.example.goalmate.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.goalmate.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Worker starting...")
            habitRepository.syncWithFirestore()
            Log.d(TAG, "Worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "HabitSyncWorker"
    }
} 