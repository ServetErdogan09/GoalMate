package com.example.goalmate.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId

object NetworkUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTime(context: Context): Long {
        val currentLocalTime = System.currentTimeMillis()
        
        return if (isInternetAvailable(context)) {
            try {

                val serverTime = getServerTime()
                if (serverTime != null) {
                    // Sunucu zamanı ile yerel zaman arasında büyük fark varsa (manipülasyon)
                    val timeDifference = kotlin.math.abs(serverTime - currentLocalTime)
                    if (timeDifference > 1000 * 60 * 5) { // 5 dakikadan fazla fark varsa
                        // Sunucu zamanını kullan
                        Log.e("normalizeTime","sunucu zamanı alındı")
                        normalizeTime(serverTime)
                    } else {
                        // Yerel zamanı kullan
                        normalizeTime(currentLocalTime)
                    }
                } else {
                    Log.e("normalizeTime","yerel zamanı alındı")
                    normalizeTime(currentLocalTime)
                }
            } catch (e: Exception) {
                Log.e("NetworkUtils", "Server time fetch failed: ${e.message}")
                normalizeTime(currentLocalTime)
            }
        } else {
            normalizeTime(currentLocalTime)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun normalizeTime(timeMillis: Long): Long {
        // Zamanı gün başlangıcına normalize et (saat, dakika, saniye sıfırla)
        val time =  Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        Log.e("normalizeTime","normalizeTime : $time")
        return time
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }


    private suspend fun getServerTime(): Long? {
        val database = FirebaseDatabase.getInstance().reference
        val timeRef = database.child("serverTime")

        return try {
            timeRef.setValue(ServerValue.TIMESTAMP).await()
            val snapshot = timeRef.get().await()
            snapshot.value as? Long
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Firebase time fetch failed: ${e.message}")
            null
        }
    }
}