package com.example.goalmate.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ForegroundInfo
import com.example.goalmate.R
import com.example.goalmate.data.localdata.DaoHabits
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.hilt.work.HiltWorker

@HiltWorker
class HabitsNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val daoHabits: DaoHabits
) : CoroutineWorker(context, params) {

    private fun createPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_click", true)
            putExtra("destination", "home")
        }

        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("HabitsNotificationWorker", "Worker başladı - ${LocalTime.now()}")
            
            createNotificationChannel()

            val uncompletedHabits = daoHabits.getUncompletedHabits()
            val countHabits = daoHabits.getActiveHabitCount()
            
            Log.d("HabitsNotificationWorker", """
                Durum Kontrolü:
                - Aktif Alışkanlık Sayısı: $countHabits
                - Tamamlanmamış Alışkanlık Sayısı: ${uncompletedHabits.size}
                - Saat: ${LocalTime.now()}
            """.trimIndent())

            if (!hasNotificationPermission()) {
                Log.w("HabitsNotificationWorker", "Bildirim izni yok, worker sonlandırılıyor")
                return@withContext Result.retry()
            }

            val pendingIntent = createPendingIntent()

            when {
                countHabits == 0 -> {
                    Log.d("HabitsNotificationWorker", "Hiç alışkanlık yok, hatırlatma gönderiliyor")
                    val notificationMessage = "Yeni bir alışkanlık eklemeye ne dersiniz? Henüz hiçbir alışkanlığınız yok, haydi başlayalım! 🎯"
                    showNoHabitsReminder(
                        title = "Alışkanlık Maceran Başlasın! 🌟",
                        notificationMessage,
                        pendingIntent
                    )
                }
                uncompletedHabits.isNotEmpty() -> {
                    Log.d("HabitsNotificationWorker", "Tamamlanmamış ${uncompletedHabits.size} alışkanlık için bildirim gönderiliyor")
                    for (habit in uncompletedHabits) {
                        val notificationMessage = when {
                            isEveningTime() -> "Bugün ${habit.name} alışkanlığınızı tamamlamadınız! 🌙"
                            isAfternoonTime() -> "Öğleden sonra ${habit.name} alışkanlığınızı tamamlamaya ne dersiniz? 🌤️"
                            else -> "Günaydın! ${habit.name} alışkanlığınızı tamamlamayı unutmayın! 🌅"
                        }

                        showNotification(
                            habit.id,
                            "Hedefine Ulaş!",
                            notificationMessage,
                            pendingIntent
                        )
                        Log.d("HabitsNotificationWorker", "Bildirim gönderildi: ${habit.name}")
                    }
                }
                else -> {
                    Log.d("HabitsNotificationWorker", "Tüm alışkanlıklar tamamlanmış, bildirim gönderilmiyor")
                }
            }

            Log.d("HabitsNotificationWorker", "Worker başarıyla tamamlandı - ${LocalTime.now()}")
            Result.success()
        } catch (e: Exception) {
            Log.e("HabitsNotificationWorker", "Worker hatası: ${e.message}", e)
            Result.failure()
        }
    }


    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alışkanlık Bildirimleri"
            val descriptionText = "Alışkanlıklarınız için hatırlatma bildirimleri"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("HABIT_CHANNEL", name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("HabitsNotificationWorker", "Bildirim kanalı oluşturuldu/güncellendi")
        }
    }

    private fun showNoHabitsReminder(title: String, content: String, pendingIntent: PendingIntent) {
        try {
            if (!hasNotificationPermission()) {
                Log.w("HabitsNotificationWorker", "Bildirim izni yok")
                return
            }

            val notification = NotificationCompat.Builder(context, "HABIT_CHANNEL")
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.bildl) // Küçük ikon (zorunlu)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(1, notification)
                Log.d("HabitsNotificationWorker", "Alışkanlık ekleme hatırlatması gönderildi")
            } catch (se: SecurityException) {
                Log.e("HabitsNotificationWorker", "Bildirim gösterme izni reddedildi", se)
            }
        } catch (e: Exception) {
            Log.e("HabitsNotificationWorker", "Bildirim gönderilemedi: ${e.message}", e)
        }
    }

    private fun showNotification(id: Int, title: String, content: String, pendingIntent: PendingIntent) {
        try {
            if (!hasNotificationPermission()) {
                Log.w("HabitsNotificationWorker", "Bildirim izni yok")
                return
            }

            val notification = NotificationCompat.Builder(context, "HABIT_CHANNEL")
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.bildl)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(id, notification)
                Log.d("HabitsNotificationWorker", "Bildirim gönderildi, ID: $id")
            } catch (se: SecurityException) {
                Log.e("HabitsNotificationWorker", "Bildirim gösterme izni reddedildi", se)
            }
        } catch (e: Exception) {
            Log.e("HabitsNotificationWorker", "Bildirim gönderilemedi: ${e.message}", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isEveningTime(): Boolean {
        val currentHour = LocalTime.now().hour
        return currentHour >= 18
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isAfternoonTime(): Boolean {
        val currentHour = LocalTime.now().hour
        return currentHour in 12..17
    }

}