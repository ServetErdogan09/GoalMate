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
    private var lastKnownServerTime: Long = 0
    private var lastServerTimeOffset: Long = 0

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTime(context: Context): Long {
        Log.d("SunucuZaman", "getTime fonksiyonu başladı")
        try {
            if (isNetworkAvailable(context)) {
                Log.d("SunucuZaman", "Sunucu zamanı alınmaya çalışılıyor...")
                val serverTime = getServerTime()
                if (serverTime != null) {
                    lastKnownServerTime = serverTime
                    lastServerTimeOffset = serverTime - System.currentTimeMillis()
                    Log.d("SunucuZaman", "Sunucu zamanı başarıyla alındı: $serverTime, Ofset: $lastServerTimeOffset")
                    return serverTime
                } else {
                    Log.e("SunucuZaman", "Sunucu zamanı null döndü")
                }
            } else {
                Log.w("SunucuZaman", "Ağ bağlantısı yok, tahmini zaman kullanılacak")
            }
        } catch (e: Exception) {
            Log.e("SunucuZaman", "Sunucu zamanı alırken hata: ${e.message}\nStack trace: ${e.stackTraceToString()}")
        }

        val estimatedServerTime = System.currentTimeMillis() + lastServerTimeOffset
        Log.d("SunucuZaman", "Tahmini sunucu zamanı: $estimatedServerTime (Offset: $lastServerTimeOffset)")
        return estimatedServerTime
    }

    private suspend fun getServerTime(): Long? {
        Log.d("SunucuZaman", "getServerTime fonksiyonu başladı")
        val database = FirebaseDatabase.getInstance().reference
        val serverTimeRef = database.child("serverTime").child("timestamp")

        return try {
            serverTimeRef.setValue(ServerValue.TIMESTAMP).await()
            val snapshot = serverTimeRef.get().await()
            val serverTime = snapshot.getValue(Long::class.java)
            Log.d("SunucuZaman", "Server zamanı alındı: $serverTime")
            serverTime
        } catch (e: Exception) {
            Log.e("SunucuZaman", "Firebase time fetch failed: ${e.message}\nStack trace: ${e.stackTraceToString()}")
            null
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}