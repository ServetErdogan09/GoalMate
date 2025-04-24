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
import com.example.goalmate.worker.ExpireGroupWorker
import com.example.goalmate.worker.GroupStatusWorker
import com.example.goalmate.worker.HabitsNotificationWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import java.util.Calendar

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

        // Setup workers
        setupHabitSyncWorker()
        setupGroupStatusWorker()
        setupHabitNotificationWorker()
        setupExpireGroupWorker()
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // normal alışkanlık bildirim takibi
    private fun setupHabitNotificationWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            // Periyodik worker oluştur
            val periodicWork = PeriodicWorkRequestBuilder<HabitsNotificationWorker>(
                3, TimeUnit.HOURS, // Her saat kontrol et
                15, TimeUnit.MINUTES // 15 dakikalık esneklik payı
            )

            /*
            val periodicWork = PeriodicWorkRequestBuilder<HabitsNotificationWorker>(
                15, TimeUnit.MINUTES, // Her saat kontrol et
                5, TimeUnit.MINUTES // 15 dakikalık esneklik payı
            )
            */
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES) // İlk çalışma için 5 dakika bekle
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            // WorkManager'ı yapılandır
            WorkManager.getInstance(applicationContext).apply {
                // Periyodik kontrolü başlat
                enqueueUniquePeriodicWork(
                    "HabitNotifications",
                    ExistingPeriodicWorkPolicy.UPDATE, // Eskisini sil, yenisini koy
                    periodicWork
                )
            }

            Log.d("NotificationWorker", "Periyodik bildirim worker'ı başarıyla ayarlandı")
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Bildirim worker'ı ayarlanırken hata: ${e.message}")
        }
    }

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
                "HABIT_CHANNEL",
                "Alışkanlık Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alışkanlıklarınız için hatırlatma bildirimleri"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // gurup için aktiflik duurmu
    private fun setupGroupStatusWorker() {
        try {
            // Hemen çalışacak bir OneTime worker oluştur
            val immediateCheck = OneTimeWorkRequestBuilder<GroupStatusWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            // Periyodik worker oluştur (10 dakikada bir)
            val periodicCheck = PeriodicWorkRequestBuilder<GroupStatusWorker>(
                3, TimeUnit.MINUTES, // Her 3 SAAT bir kontrol
                30, TimeUnit.MINUTES  // 30 dakika esneklik payı
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            // WorkManager'ı yapılandır
            WorkManager.getInstance(applicationContext).apply {
                // Hemen çalışacak kontrolü başlat
                enqueue(immediateCheck)
                
                // Periyodik kontrolü başlat
                enqueueUniquePeriodicWork(
                    "GroupStatusCheck",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicCheck
                )
            }

            Log.d("WorkManager", "GroupStatusWorker scheduled successfully (both immediate and periodic)")
        } catch (e: Exception) {
            Log.e("WorkManager", "Error scheduling GroupStatusWorker: ${e.message}", e)
        }
    }



// kalan gün sayısı 1 olduğunda gece yarısı çalışıp gurubu kapatacak
    private fun setupExpireGroupWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Her gün gece yarısı çalışacak periyodik worker  24 SAATE BİR ÇALIŞACAK
            val periodicWork = PeriodicWorkRequestBuilder<ExpireGroupWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "ExpireGroupWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )

            Log.d("WorkManager", "ExpireGroupWorker başarıyla ayarlandı")
        } catch (e: Exception) {
            Log.e("WorkManager", "ExpireGroupWorker ayarlanırken hata: ${e.message}")
        }
    }

    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Bir sonraki gün 00:00'ı hesapla
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis - now
    }
}