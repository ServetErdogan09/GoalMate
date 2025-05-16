package com.example.goalmate.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.Badges
import com.example.goalmate.data.repository.BadgesRepository
import com.example.goalmate.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val badgesRepository: BadgesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _badges = MutableStateFlow<List<Badges>>(emptyList())
    val badges: StateFlow<List<Badges>> = _badges.asStateFlow()

    private val _newlyEarnedBadges = MutableStateFlow<List<Badges>>(emptyList())
    val newlyEarnedBadges: StateFlow<List<Badges>> = _newlyEarnedBadges.asStateFlow()

    init {
        loadBadges()
    }

    private fun loadBadges() {
        viewModelScope.launch {
            try {
                badgesRepository.getAllBadges().collect { localBadges ->
                    if (localBadges.isEmpty()) {
                        Log.d("BadgesViewModel", "Local veritabanı boş, JSON'dan yüklenecek")
                    } else {
                        auth.currentUser?.uid?.let { userId ->
                            badgesRepository.syncFirebaseBadgesToLocal(userId)
                        }
                    }
                    _badges.value = localBadges
                    checkForUnshownBadges()
                }
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Rozetleri yüklerken hata oluştu", e)
            }
        }
    }

    fun addListBadges(badges: List<Badges>) {
        viewModelScope.launch {
            try {
                badgesRepository.addBadges(badges)
                loadBadges()
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Rozetleri eklerken hata oluştu", e)
            }
        }
    }

    fun checkForUnshownBadges() {
        viewModelScope.launch {
            try {
                // Sadece tamamlanmış ve gösterilmeye hazır (isShown = true) olan ilk rozeti al
                val unshownBadge = _badges.value.find { it.isCompleted && it.isShown }
                _newlyEarnedBadges.value = if (unshownBadge != null) listOf(unshownBadge) else emptyList()
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Gösterilmemiş rozetler kontrol edilirken hata oluştu", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun incrementAppUsageDays(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val lastUsageDate = badgesRepository.getLastUsageDate()
                //val currentDate = System.currentTimeMillis()
                val currentDate = NetworkUtils.getTime(context)

                // Yeni gün kontrolü (24 saat geçmiş mi?)
                if (lastUsageDate == 0L || (currentDate - lastUsageDate) >= 24 * 60 * 60 * 1000) {
                    val currentUsageDays = badgesRepository.getAppUsageDays()
                    val newUsageDays = currentUsageDays + 1
                    
                    badgesRepository.incrementAppUsageDays(newUsageDays)
                    badgesRepository.updateLastUsageDate(currentDate)
                    
                    // Kullanım rozetlerini kontrol et
                    badgesRepository.checkAppUsageBadges(newUsageDays)
                    
                    Log.d("BadgesViewModel", "Uygulama kullanım günü güncellendi: $newUsageDays")
                }
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Uygulama kullanım günü güncellenirken hata oluştu", e)
            }
        }
    }



    fun fetchKickedMemberCount() {
        viewModelScope.launch {
            try {

                val currentKickCount = badgesRepository.getKickedMemberCount()
                Log.d("FetchKickedMemberCount", "Mevcut atılan kullanıcı sayısı: $currentKickCount")

                val newKickCount = currentKickCount + 1
                Log.d("FetchKickedMemberCount", "Yeni atılan kullanıcı sayısı hesaplandı: $newKickCount")

                // Veritabanını güncelle
                badgesRepository.updateRemovedUserCount(newKickCount)
                Log.d("FetchKickedMemberCount", "Yeni atılan kullanıcı sayısı veritabanına kaydedildi: $newKickCount")

                // Admin rozet kontrolü
                badgesRepository.checkAdminBadges(isAdmin = true, kickedMemberCount = newKickCount, adminCompletedGroups = 0)
                Log.d("FetchKickedMemberCount", "Admin rozet kontrolü başarıyla yapıldı.")
            } catch (e: Exception) {
                // Hata logu
                Log.e("FetchKickedMemberCount", "Kullanıcı sayısı güncellenirken hata oluştu: ${e.message}")
            }
        }
    }



    fun markBadgesAsShown() {
        viewModelScope.launch {
            try {
                // Gösterilen rozeti güncelle
                _newlyEarnedBadges.value.firstOrNull()?.let { badge ->
                    badgesRepository.updateBadgeShownStatus(badge.copy(isShown = false))
                }
                // Gösterilen rozeti temizle
                _newlyEarnedBadges.value = emptyList()
                // Diğer rozetleri kontrol et
                checkForUnshownBadges()
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Rozetler gösterildi olarak işaretlenirken hata oluştu", e)
            }
        }
    }

    /*
    fun checkGroupCompletionBadgesViewModel(
        dailyGroupCount: Int,
        weeklyGroupCount: Int,
        monthlyGroupCount: Int
    ) {
        viewModelScope.launch {
            try {
                auth.currentUser?.uid?.let { userId ->
                    val badgesId = badgesRepository.checkGroupCompletionBadges(dailyGroupCount = dailyGroupCount , weeklyGroupCount = weeklyGroupCount , monthlyGroupCount = monthlyGroupCount)
                    badgesRepository.updateBadgesInBothDatabases(badgesId)
                }
            } catch (e: Exception) {
                Log.e("BadgesViewModel", "Rozet güncellerken hata oluştu", e)
            }
        }
    }

     */




}