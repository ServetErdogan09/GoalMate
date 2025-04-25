package com.example.goalmate.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun calculateAndUpdatePoints(frequency: String, isCompleted: Boolean, isReversingPenalty: Boolean = false): Int {
        try {
            val userId = auth.currentUser?.uid ?: return 0
            
            // Kullanıcı dökümanını al
            val userDoc = db.collection("users")
                .document(userId)
                .get()
                .await()

            val currentPoints = userDoc.getLong("totalPoints")?.toInt() ?: 0
            
            // Frekansa göre puan hesapla
            val pointChange = when (frequency.lowercase()) {
                "günlük" -> if (isCompleted) 5 else if (isReversingPenalty) 0 else 0
                "haftalık" -> if (isCompleted) 10 else if (isReversingPenalty) 14 else -4 // 4
                "aylık" -> if (isCompleted) 12 else if (isReversingPenalty) 18 else -6 // 6
                else -> 0
            }

            // Yeni puan hesapla
            val newPoints = currentPoints + pointChange

            // Firestore'u güncelle
            db.collection("users")
                .document(userId)
                .update("totalPoints", newPoints)
                .await()

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
}