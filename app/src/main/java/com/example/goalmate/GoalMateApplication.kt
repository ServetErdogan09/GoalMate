package com.example.goalmate

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.goalmate.worker.HabitSyncWorker
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import android.app.NotificationChannel
import android.app.NotificationManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GoalMateApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannel()

        // Initialize WorkManager
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(this, config)

        // Setup worker after initialization
        setupHabitSyncWorker()
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupHabitSyncWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<HabitSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "HabitSyncTest",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            Log.d("WorkManager", "HabitSyncWorker scheduled successfully")
        } catch (e: Exception) {
            Log.e("WorkManager", "Error scheduling HabitSyncWorker: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "group_notifications",
                "Grup Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Grup bildirimleri için kanal"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}