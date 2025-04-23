package com.example.goalmate.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.prenstatntion.AnalysisScreen.totalHabit
import com.example.goalmate.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@HiltWorker
class ExpireGroupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        try {
            Log.d("ExpireGroupWorker", "Grup kontrol işlemi başlatıldı")
            
            // Aktif grupları al
            val activeGroups = db.collection("groups")
                .whereEqualTo("groupStatus", "ACTIVE")
                .get()
                .await()
                .toObjects(Group::class.java)

            Log.d("ExpireGroupWorker", "Aktif grup sayısı: ${activeGroups.size}")

            val currentTime = NetworkUtils.getTime(applicationContext) // test amaçlı yerel saat kullanılıyor testen sonra değiştirilecek
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            activeGroups.forEach { group ->
                try {
                    val startDate = group.actualStartDate ?: return@forEach
                    val startCalendar = Calendar.getInstance().apply {
                        timeInMillis = startDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val diffInMillis = calendar.timeInMillis - startCalendar.timeInMillis
                    val daysBetween = (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
                    val totalDays = totalHabit(group.frequency)
                    val remainingDays = totalDays - daysBetween

                    Log.d("ExpireGroupWorker", "Grup ${group.groupId} için kalan gün: $remainingDays")

                    if (remainingDays <= 1) {
                        // Grubu kapat
                        closeGroup(group.groupId)
                        Log.d("ExpireGroupWorker", "Grup ${group.groupId} kapatıldı")
                    }
                } catch (e: Exception) {
                    Log.e("ExpireGroupWorker", "Grup işlenirken hata: ${e.message}")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("ExpireGroupWorker", "Worker çalışırken hata: ${e.message}")
            return Result.failure()
        }
    }

    private suspend fun closeGroup(groupId: String) {
        try {
            // Önce tüm kullanıcıların joinedGroups listesinden bu grubu çıkar
            val usersSnapshot = db.collection("users").get().await()
            val batch = db.batch()

            for (userDoc in usersSnapshot.documents) {
                val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: continue
                if (groupId in joinedGroups) {
                    val userRef = db.collection("users").document(userDoc.id)
                    batch.update(userRef, "joinedGroups", joinedGroups - groupId)

                    // Kullanıcının groupHabits koleksiyonundan da sil
                    val groupHabitsRef = userRef.collection("groupHabits").document(groupId)
                    batch.delete(groupHabitsRef)
                }
            }

            // Batch işlemini tamamla
            batch.commit().await()

            // Grubun mesajlarını sil
            val messagesSnapshot = db.collection("groups")
                .document(groupId)
                .collection("messages")
                .get()
                .await()

            if (!messagesSnapshot.isEmpty) {
                val messageBatch = db.batch()
                messagesSnapshot.documents.forEach { doc ->
                    messageBatch.delete(doc.reference)
                }
                messageBatch.commit().await()
            }

            // Grubun oylama verilerini sil
            val closeVoteRef = db.collection("groups")
                .document(groupId)
                .collection("closeVote")
                .document("status")
            
            if (closeVoteRef.get().await().exists()) {
                closeVoteRef.delete().await()
            }

            // Son olarak grubu tamamen sil
            db.collection("groups")
                .document(groupId)
                .delete()
                .await()

            Log.d("ExpireGroupWorker", "Grup $groupId ve ilişkili tüm veriler başarıyla silindi")
        } catch (e: Exception) {
            Log.e("ExpireGroupWorker", "Grup silinirken hata: ${e.message}")
            throw e
        }
    }
}