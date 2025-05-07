package com.example.goalmate.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val _userPoints = MutableStateFlow<Int>(0)
    val userPoints: StateFlow<Int> = _userPoints

    // Local'den puan çekme
    private fun getLocalUserPoints(context: Context): Int? {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return if (sharedPreferences.contains("user_points")) {
            sharedPreferences.getInt("user_points", 0)
        } else {
            null
        }
    }

    // Local'e puan kaydetme
    internal fun saveUserPointsToLocal(context: Context, points: Int) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putInt("user_points", points)
            apply()
        }
        Log.d("PointsRepository", "Puanlar local'e kaydedildi: $points")
    }

    // Kullanıcının kendi puanını başlatma/güncelleme
    suspend fun initializeUserPoints(context: Context) {
        try {
            val localPoints = getLocalUserPoints(context)
            if (localPoints != null && localPoints != 0) {
                _userPoints.value = localPoints
                Log.d("PointsRepository", "Puanlar local'den yüklendi: $localPoints")
            } else {
                // Local'de puan yoksa Firebase'den çek
                auth.currentUser?.let { user ->
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val points = userDoc.getLong("totalPoints")?.toInt() ?: 0
                    _userPoints.value = points
                    saveUserPointsToLocal(context, points)
                    Log.d("PointsRepository", "Puanlar Firebase'den yüklendi: $points")
                }
            }
        } catch (e: Exception) {
            Log.e("PointsRepository", "Puanlar yüklenirken hata oluştu", e)
        }
    }

    // Başka bir kullanıcının puanını çekme
    suspend fun getUserPoints(userId: String): Int {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            userDoc.getLong("totalPoints")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e("PointsRepository", "Kullanıcı puanı çekilirken hata oluştu", e)
            0
        }
    }


    suspend fun calculateExitPenalty(frequency: String) {
        val currentId = auth.currentUser?.uid ?: run {
            Log.e("PointsRepository", "Kullanıcı ID'si bulunamadı")
            return
        }

        val pointsBroken = when (frequency.lowercase()) {
            "günlük" -> -10
            "haftalık" -> -20
            "aylık" -> -30
            else -> 0
        }

        Log.d("PointsRepository", "Puan kırılma değeri hesaplandı: $pointsBroken")

        try {
            val userRef = db.collection("users").document(currentId)
                .get()
                .await()

            Log.d("PointsRepository", "Kullanıcı dökümanı başarıyla alındı")

            val currentPoint = userRef.getLong("totalPoints")?.toInt() ?: 0
            Log.d("PointsRepository", "Mevcut puan: $currentPoint")

            // Yeni toplam puanı hesapla
            val newTotalPoints = currentPoint + pointsBroken
            Log.d("PointsRepository", "Yeni toplam puan hesaplandı: $newTotalPoints")

            userRef.reference.update("totalPoints", newTotalPoints).await()
            Log.d("PointsRepository", "Firebase'de puan başarıyla güncellendi")

            // Güncel puanı state'e at
            _userPoints.value = newTotalPoints
            Log.d("PointsRepository", "Yeni puan local state'e başarıyla güncellendi: $newTotalPoints")

        } catch (e: Exception) {
            Log.e("PointsRepository", "Puan hesaplanırken hata oluştu", e)
        }
    }



    // Puan hesaplama ve güncelleme
    suspend fun calculateAndUpdatePoints(
        frequency: String,
        isCompleted: Boolean,
        isReversingPenalty: Boolean = false,
        context: Context
    ): Int {
        try {
            val userId = auth.currentUser?.uid ?: return 0

            // Kullanıcı dökümanını al
            val userDoc = db.collection("users").document(userId).get().await()
            val currentPoints = userDoc.getLong("totalPoints")?.toInt() ?: 0

            // Frekansa göre puan hesapla
            val pointChange = when (frequency.lowercase()) {
                "günlük" -> if (isCompleted) 5 else if (isReversingPenalty) 6 else -1
                "haftalık" -> if (isCompleted) 7 else if (isReversingPenalty) 9 else -2
                "aylık" -> if (isCompleted) 9 else if (isReversingPenalty) 11 else -2
                else -> 0
            }

            // Yeni puan hesapla
            val newPoints = currentPoints + pointChange

            // Firestore'u güncelle
            db.collection("users")
                .document(userId)
                .update("totalPoints", newPoints)
                .await()

            // Local'i ve StateFlow'u güncelle
            _userPoints.value = newPoints
            saveUserPointsToLocal(context, newPoints)

            Log.d("PointsRepository", """
                Puan Güncellemesi:
                - Frekans: $frequency
                - Tamamlandı mı: $isCompleted
                - Ceza Geri Alınıyor mu: $isReversingPenalty
                - Önceki Puan: $currentPoints
                - Puan Değişimi: $pointChange
                - Yeni Puan: $newPoints
            """.trimIndent())

            return newPoints

        } catch (e: Exception) {
            Log.e("PointsRepository", "Puan hesaplanırken hata oluştu", e)
            return 0
        }
    }




    // Firebase'den gelen puan değişikliklerini dinle
     fun listenToPointChanges(userId: String, context: Context) {
        try {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("PointsRepository", "Puan dinleme hatası", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val firebasePoints = snapshot.getLong("totalPoints")?.toInt() ?: 0

                        // Eğer bu kullanıcının kendi puanıysa, local'i de güncelle
                        if (userId == auth.currentUser?.uid) {
                            _userPoints.value = firebasePoints
                            saveUserPointsToLocal(context, firebasePoints)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("PointsRepository", "Puan dinleme başlatılırken hata", e)
        }
    }
}