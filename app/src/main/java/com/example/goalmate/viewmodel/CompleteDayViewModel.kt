package com.example.goalmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.CompletedDay
import com.example.goalmate.data.repository.CompleteDayDaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteDayViewModel @Inject constructor(
    private val completeDayDaoRepository: CompleteDayDaoRepository
) : ViewModel() {

    private val _completedDays = MutableStateFlow<List<CompletedDay>>(emptyList())
    val completedDays: StateFlow<List<CompletedDay>> = _completedDays

    fun getCompleteDays(habitId: Int) {
        viewModelScope.launch {
            try {
                completeDayDaoRepository.getCompletedDays(habitId).collect { completeDays ->
                    _completedDays.value = completeDays
                    Log.d("completeDays", "Veri çekildi: $completeDays")
                }
            } catch (e: Exception) {
                _completedDays.value = emptyList()  // Hata durumunda listeyi temizle
                Log.e("completeDays", "Veri çekilirken hata: ${e.localizedMessage}")
            }
        }
    }


    fun deleteHabit(habitId: Int){
        viewModelScope.launch {
            try {
                completeDayDaoRepository.deleteHabit(habitId)
                Log.e("completeDays", "Veri silindi: $habitId")
            }catch (e:Exception){
                Log.e("completeDays", "Veri silinirken hata: ${e.localizedMessage}")
            }
        }
    }

    fun getCompletedDayByDate(habitId: Int, date: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val existingCompletedDay =
                    completeDayDaoRepository.getCompletedDayByDate(habitId, date)
                if (existingCompletedDay == null) {
                    val completedDay = CompletedDay(
                        habitId = habitId,
                        date = date,
                        isCompleted = false
                    )
                    completeDayDaoRepository.insert(completedDay)
                    Log.d("completeDays", "Yeni kayıt eklendi: $completedDay")
                } else {
                    Log.d("completeDays", "Kayıt zaten var: $existingCompletedDay")
                }
            } catch (e: Exception) {
                Log.e("completeDays", "Kayıt eklenirken hata: ${e.localizedMessage}")
            }
        }
    }


    fun updateCompletedDays(habitId: Int, date: Long, completed: Boolean) {
        viewModelScope.launch {
            try {
                val existingCompletedDay =
                    completeDayDaoRepository.getCompletedDayByDate(habitId, date)
                if (existingCompletedDay != null) {
                    completeDayDaoRepository.updateCompletedDays(habitId, date, completed)
                    Log.d(
                        "completeDays",
                        "Veri güncellendi: $habitId - boolean: $completed - date: $date"
                    )
                    getCompleteDays(habitId)
                } else {
                    Log.d(
                        "completeDays",
                        "Güncellenmeye çalışılan kayıt bulunamadı: $habitId - date: $date"
                    )
                }
            } catch (e: Exception) {
                Log.e("completeDays", "Veri güncellenirken hata: ${e.localizedMessage}")
            }
        }
    }
}