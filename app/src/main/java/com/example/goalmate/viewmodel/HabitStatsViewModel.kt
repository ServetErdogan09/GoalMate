package com.example.goalmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.GroupHabitStats
import com.example.goalmate.data.localdata.GroupHabits
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



@HiltViewModel
class HabitStatsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _statsState = MutableStateFlow<List<GroupHabits>>(emptyList())
    val statsState: StateFlow<List<GroupHabits>> = _statsState.asStateFlow()

    private val _overallStats = MutableStateFlow(GroupHabitStats())
    val overallStats: StateFlow<GroupHabitStats> = _overallStats.asStateFlow()

    fun getGroupHabitsStats() {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch

                val userDoc = db.collection("users")
                    .document(currentUserId)
                    .collection("groupHabits")
                    .get()
                    .await()

                val habitStats = userDoc.toObjects(GroupHabits::class.java)

                if (habitStats.isNotEmpty()) {
                    Log.d("HabitStatsViewModel", "Veriler alındı: ${habitStats.size} adet alışkanlık bulundu.")
                    _statsState.value = habitStats
                } else {
                    Log.d("HabitStatsViewModel", "Veri bulunamadı.")
                }

            } catch (e: Exception) {
                Log.e("HabitStatsViewModel", "Veriler çekilirken hata oluştu: ${e.message}")
            }
        }
    }

    fun getOverallStats() {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch

                val statsDoc = db.collection("users")
                    .document(currentUserId)
                    .collection("stats")
                    .document("habitStats")
                    .get()
                    .await()

                if (statsDoc.exists()) {
                    val stats = statsDoc.toObject(GroupHabitStats::class.java)
                    stats?.let {
                        _overallStats.value = it
                    }
                    Log.d("HabitStatsViewModel", "Genel istatistikler alındı")
                } else {
                    Log.d("HabitStatsViewModel", "Genel istatistik verisi bulunamadı")
                }

            } catch (e: Exception) {
                Log.e("HabitStatsViewModel", "Genel istatistikler çekilirken hata oluştu: ${e.message}")
            }
        }
    }
}