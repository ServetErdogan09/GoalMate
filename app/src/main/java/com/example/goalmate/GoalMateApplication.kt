package com.example.goalmate

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import android.app.NotificationChannel
import android.app.NotificationManager

@HiltAndroidApp
class GoalMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // Bildirim kanalını uygulama başlatıldığında oluştur
        createNotificationChannel()
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