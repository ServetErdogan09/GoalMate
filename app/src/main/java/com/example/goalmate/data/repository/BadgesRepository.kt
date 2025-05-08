package com.example.goalmate.data.repository

import android.content.Context
import android.util.Log
import com.example.goalmate.data.localdata.Badges
import com.example.goalmate.data.localdata.BadgesDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BadgesRepository @Inject constructor(
    private val badgesDao: BadgesDao,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    fun getAllBadges(): Flow<List<Badges>> = badgesDao.getAllBadges()

    // JSON'dan gelen rozetleri locale ekle
    suspend fun addBadges(badges: List<Badges>) {
        try {
            if (badgesDao.getBadgesCount() == 0L) {
                // Önce Firebase'den tamamlanan rozet ID'lerini al
                auth.currentUser?.uid?.let { userId ->
                    val completedIds = getCompletedBadgeIdsFromFirebase(userId)
                    
                    // Rozetleri Firebase'den gelen completion durumuna göre güncelle
                    val updatedBadges = badges.map { badge ->
                        badge.copy(
                            isCompleted = completedIds.contains(badge.id),
                            isShown = completedIds.contains(badge.id)
                        )
                    }
                    
                    badgesDao.insertBadges(updatedBadges)
                    Log.d("BadgesRepository", "Rozetler başarıyla eklendi ve Firebase ile senkronize edildi")
                }
            }
        } catch (e: Exception) {
            Log.e("BadgesRepository", "Rozetleri eklerken hata oluştu", e)
            throw e
        }
    }

    // Firebase'den tamamlanan rozet ID'lerini al
    private suspend fun getCompletedBadgeIdsFromFirebase(userId: String): List<String> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("badges")
                .document("earnedBadges")
                .get()
                .await()

            (snapshot["badgeIds"] as? List<String>) ?: emptyList()
        } catch (e: Exception) {
            Log.e("BadgesRepository", "Firebase'den rozet ID'leri alınırken hata oluştu", e)
            emptyList()
        }
    }

    // Firebase'den locale tamamlanan idleri koyacağız
    suspend fun syncFirebaseBadgesToLocal(userId: String) {
        try {
            Log.d("Badges", "Firebase'den rozet ID'leri çekiliyor...")
            val completedIds = getCompletedBadgeIdsFromFirebase(userId)
            val localBadges = badgesDao.getAllBadges().first()
            if (localBadges.isEmpty()){
                if (completedIds.isNotEmpty()) {
                    Log.d("Badges", "Firebase'den ${completedIds.size} rozet ID'si alındı")
                    val localBadges = getAllBadges().first()

                    // Mevcut rozetleri Firebase'den gelen ID'lere göre güncelle
                    val updatedBadges = localBadges.map { badge ->
                        if (completedIds.contains(badge.id)) {
                            badge.copy(isCompleted = true, isShown = false)
                        } else {
                            badge.copy(isCompleted = false, isShown = false)
                        }
                    }

                    badgesDao.insertBadges(updatedBadges)
                    Log.d("Badges", "Local rozetler Firebase ile senkronize edildi")
                }
            }else{
                Log.d("Badges", "Firebase'den rozet ID'leri çekilmesi iptal edildi")
            }

        } catch (e: Exception) {
            Log.e("Badges", "Rozet senkronizasyonu sırasında hata: ${e.message}")
        }
    }






    // tamamlanan günlük, haftalık, aylık sayısına göre tamamlanıp tamamlanmadığına bakacağız ve buna göre limit artıracağız
    suspend fun checkGroupCompletionBadges(
        dailyGroupCount: Int,
        weeklyGroupCount: Int,
        monthlyGroupCount: Int
    ){
        val badgeIds = mutableListOf<String>()
        val allBadges = badgesDao.getAllBadges().first()

        Log.d("Badges", "Grup tamamlama rozetleri kontrol ediliyor...")

        // Günlük grup tamamlamalarına göre rozet kontrolü
        if (dailyGroupCount >= 5) {
            badgeIds.add("R002")
            Log.d("Badges", "Günlük grup sayısı ulaşıldı: R002 rozet kazandı.")
        }
        // Haftalık grup tamamlamalarına göre rozet kontrolü
        if (weeklyGroupCount >= 3) {
            badgeIds.add("R003")
            Log.d("Badges", "Haftalık grup sayısı ulaşıldı: R003 rozet kazandı.")
        }
        // Aylık grup tamamlamalarına göre rozet kontrolü
        if (monthlyGroupCount >= 1) {
            badgeIds.add("R004")
            Log.d("Badges", "Aylık grup sayısı ulaşıldı: R004 rozet kazandı.")
        }

        if (monthlyGroupCount + weeklyGroupCount >= 10){
            badgeIds.add("R005")
            Log.d("Badges", "10 grup sayısı ulaşıldı: R005 rozet kazandı.")
        }


        if (monthlyGroupCount + weeklyGroupCount >= 50){
            badgeIds.add("R013")
            Log.d("Badges", "10 grup sayısı ulaşıldı: R005 rozet kazandı.")
        }



        val completedBadges = getCompletedBadgesByIds(badgeIds, allBadges)

        if (completedBadges.isNotEmpty()) {
            // Firebase ve local veritabanını güncelle
            updateBadgesInBothDatabases(completedBadges)
        }

        Log.d("Badges", "Tamamlanan rozet sayısı: ${completedBadges.size}")
    }



    // gurup oluşturulduğunda ya da katıldığında rozeti ver
    suspend fun createGroup(){
        val badgesId = mutableListOf<String>()
        val allBadges = badgesDao.getAllBadges().first()

        badgesId.add("R001")

        val completedBadges = getCompletedBadgesByIds(badgesId,allBadges)

        if (completedBadges.isNotEmpty()){
            updateBadgesInBothDatabases(completedBadges)
        }

    }


    // admin olarak
    suspend fun checkAdminBadges(
        isAdmin: Boolean,
        adminCompletedGroups: Int,
        kickedMemberCount: Int
    ) {
        val badgeIds = mutableListOf<String>()
        val allBadges = badgesDao.getAllBadges().first()

        Log.d("Badges", "Admin rozet kontrolü başlatıldı. isAdmin=$isAdmin, completedGroups=$adminCompletedGroups, kickedCount=$kickedMemberCount")

        // admin olarak grup oluşturduysa
        if (isAdmin) {
            badgeIds.add("R09")
            Log.d("Badges", "Admin olarak grup oluşturma rozeti kazanıldı: R09")
        }

        // admin olarak guruptan atma
        if (kickedMemberCount >= 1) {
            badgeIds.add("R010")
            Log.d("Badges", "Üye atma rozeti kazanıldı: R010")
        }

        // bir grup tamamlama
        if (isAdmin && adminCompletedGroups >= 1) {
            badgeIds.add("R011")
            Log.d("Badges", "1 grup tamamlama rozeti kazanıldı: R011")
        }

        // tüm grupları tamamlama (örnek olarak 3)
        if (isAdmin && adminCompletedGroups >= 3) {
            badgeIds.add("R012")
            Log.d("Badges", "3 grup tamamlama rozeti kazanıldı: R012")
        }

        val completedBadges = getCompletedBadgesByIds(badgeIds, allBadges)

        if (completedBadges.isNotEmpty()) {
            Log.d("Badges", "Tamamlanan admin rozetleri güncelleniyor: ${completedBadges.map { it.id }}")
            updateBadgesInBothDatabases(completedBadges)
        } else {
            Log.d("Badges", "Yeni kazanılan admin rozeti yok.")
        }
    }



    // uygulama kullanma giriş yapma
    suspend fun checkAppUsageBadges(appUsageDays: Int) {

        val badgeIds = mutableListOf<String>()
        val allBadges = badgesDao.getAllBadges().first()

        Log.d("Badges", "Uygulama kullanım günü: $appUsageDays")

        // 7 gün uygulamaya giriş yapma
        if (appUsageDays >= 7) {
            badgeIds.add("R014")
            Log.d("Badges", "7 gün rozeti kazanıldı: R014")
        }

        // 30 gün uygulamaya giriş yapma
        if (appUsageDays >= 30) {
            badgeIds.add("R015")
            Log.d("Badges", "30 gün rozeti kazanıldı: R015")
        }

        // 120 gün uygulamaya giriş yapma
        if (appUsageDays >= 120) {
            badgeIds.add("R016")
            Log.d("Badges", "120 gün rozeti kazanıldı: R016")
        }

        // 360 gün uygulamaya giriş yapma
        if (appUsageDays >= 360) {
            badgeIds.add("R017")
            Log.d("Badges", "360 gün rozeti kazanıldı: R017")
        }

        val completedBadges = getCompletedBadgesByIds(badgeIds, allBadges)

        if (completedBadges.isNotEmpty()) {
            Log.d("Badges", "Tamamlanan rozetler güncelleniyor: ${completedBadges.map { it.id }}")
            updateBadgesInBothDatabases(completedBadges)
        } else {
            Log.d("Badges", "Yeni tamamlanan rozet bulunamadı.")
        }
    }


   suspend fun checkLimitIncreaseBadges(
        weeklyGroupCount: Int,
        monthlyGroupCount: Int
    ) {

       val badgesId = mutableListOf<String>()
       val allBadges = badgesDao.getAllBadges().first()
       Log.d("Badges", "Limit artırımı rozetleri kontrol ediliyor...")
       // 1 tane aylık ya da 4 tane haftalık grup tamamla.",
       if (monthlyGroupCount >=  1 || weeklyGroupCount >= 4 ){
           badgesId.add("R006")
           Log.d("Badges", "Limit rozeti R006 kazanıldı: 1 aylık veya 4 haftalık grup tamamlandı.")

       }

       //"3'ten 4'e geçiş: 2 tane aylık ya da 8 tane haftalık grup tamamla.

       if (monthlyGroupCount >= 2 || weeklyGroupCount >= 8){
           badgesId.add("R007")
           Log.d("Badges", "Limit rozeti R007 kazanıldı: 2 aylık veya 8 haftalık grup tamamlandı.")

       }

       //"4'ten 5'e geçiş: 4 tane aylık ya da 16 tane haftalık grup tamamla.",
       if (monthlyGroupCount >= 4  || weeklyGroupCount >= 16){
           badgesId.add("R008")
           Log.d("Badges", "Limit rozeti R008 kazanıldı: 4 aylık veya 16 haftalık grup tamamlandı.")

       }

       val completedBadges = getCompletedBadgesByIds(badgesId , allBadges)


       if (completedBadges.isNotEmpty()){
           updateBadgesInBothDatabases(completedBadges)
       }
       Log.d("Badges", "Tamamlanan rozet sayısı: ${completedBadges.size}")

    }

    // Firebase ve locale rozetlerini güncelleme
    suspend fun updateBadgesInBothDatabases(badges: List<Badges>) {
        auth.currentUser?.uid?.let { userId ->
            val badgeIds = badges.map { it.id }
            Log.d("Badges", "Kullanıcı için Firebase rozetleri güncelleniyor: $userId")

            // Firebase güncellemesi
            updateBadgesInFirebase(userId, badgeIds)
        }

        // Locale veritabanına rozet ekle
        Log.d("Badges", "Yerel veritabanına rozetler ekleniyor.")
        badgesDao.insertBadges(badges)
    }

    // Firebase e rozet ekleme
    suspend fun updateBadgesInFirebase(userId: String, badgeIds: List<String>) {
        val userBadgesRef = db.collection("users")
            .document(userId)
            .collection("badges")
            .document("earnedBadges")

        try {
            // Firebase'e rozetleri ekliyoruz
            Log.d("Badges", "Kullanıcı için Firebase'e rozetler ekleniyor: $userId")
            userBadgesRef.set(
                mapOf("badgeIds" to FieldValue.arrayUnion(*badgeIds.toTypedArray())),
                SetOptions.merge()  // Veriyi mevcut verilerle birleştirir
            ).await()  // Asenkron işlem olduğu için await kullanıyoruz

            Log.d("Badges", "Rozetler başarıyla Firebase'e güncellendi.")
        } catch (e: Exception) {
            Log.e("Badges", "Firebase rozet güncelleme hatası: ${e.message}")
        }
    }

    // firebase deki verileri locale koyacağız eğer localde veri yoksa fairbase de varsa
    private fun getCompletedBadgesByIds(
        ids: List<String>,
        allBadges: List<Badges>
    ): List<Badges> {
        return ids.mapNotNull { id ->
            allBadges.find { it.id == id && !it.isCompleted }?.copy(isCompleted = true, isShown = true)
        }
    }

    // Rozet gösterildi durumunu güncelle
    suspend fun updateBadgeShownStatus(badge: Badges) {
        try {
            // Local veritabanını güncelle
            badgesDao.insertBadges(listOf(badge))
            Log.d("Badges", "Rozet gösterildi durumu güncellendi: ${badge.id}")
        } catch (e: Exception) {
            Log.e("Badges", "Rozet gösterildi durumu güncellenirken hata: ${e.message}")
        }
    }

    // Son kullanım tarihini al
     fun getLastUsageDate(): Long {
        return try {
            val prefs = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE)
            prefs.getLong("last_usage_date", 0L)
        } catch (e: Exception) {
            Log.e("Badges", "Son kullanım tarihi alınırken hata: ${e.message}")
            0L
        }
    }

    // Son kullanım tarihini güncelle
     fun updateLastUsageDate(date: Long) {
        try {
            val prefs = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_usage_date", date).apply()
            Log.d("Badges", "Son kullanım tarihi güncellendi: $date")
        } catch (e: Exception) {
            Log.e("Badges", "Son kullanım tarihi güncellenirken hata: ${e.message}")
        }
    }

    suspend fun getAppUsageDays(): Int {
        return try {
            val snapshot = db.collection("users")
                .document(auth.currentUser?.uid ?: return 0) // kullanıcı ID'si
                .collection("stats")
                .document("habitStats")
                .get()
                .await()

            snapshot.getLong("appUsageDays")?.toInt() ?: 0

        } catch (e: Exception) {
            Log.e("Badges", "Kullanım günü sayısı alınırken hata: ${e.message}")
            0
        }
    }




    // Toplam kullanım günü sayısını güncelle
    suspend fun incrementAppUsageDays(updatedDays : Int) {
        try {
            val userId = auth.currentUser?.uid ?: return

            val docRef = db.collection("users")
                .document(userId)
                .collection("stats")
                .document("habitStats")


            // Firebase'de güncelle
            docRef.set(mapOf("appUsageDays" to updatedDays), SetOptions.merge()).await()

            Log.d("Badges", "Uygulama kullanım günü güncellendi: $updatedDays")

        } catch (e: Exception) {
            Log.e("Badges", "Kullanım günü sayısı artırılırken hata: ${e.message}")
        }
    }

    suspend fun getKickedMemberCount() : Int {
        return try {
            val snapshot = db.collection("users")
                .document(auth.currentUser?.uid ?: return 0) // kullanıcı ID'si
                .collection("stats")
                .document("habitStats")
                .get()
                .await()

            val kickedCount = snapshot.getLong("kickedMemberCount")?.toInt() ?: 0
            Log.d("Badges", "Atılan kullanıcı sayısı başarıyla alındı: $kickedCount") // Başarıyla veri alındıysa
            kickedCount
        } catch (e: Exception) {
            Log.e("Badges", "Atılan kullanıcı sayısı alınırken hata: ${e.message}", e) // Hata durumunda
            0
        }
    }


    // Yönetici olarak gruptan atılan kullanıcı sayısını güncelleyen fonksiyon
    suspend fun updateRemovedUserCount(newKickCount: Int) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return

            val docRef = db.collection("users")
                .document(currentUserId) // Direkt mevcut kullanıcı ID'sini kullan
                .collection("stats")
                .document("habitStats")

            docRef.set(mapOf("kickedMemberCount" to newKickCount), SetOptions.merge())
                .await()

            Log.d("UpdateRemovedUserCount", "Atılan kullanıcı sayısı güncellendi: $newKickCount")
        } catch (e: Exception) {
            Log.e("UpdateRemovedUserCount", "Güncelleme hatası: ${e.message}")
        }
    }
}