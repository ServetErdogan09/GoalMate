package com.example.goalmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.repository.StarCoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarCoinViewModel @Inject constructor(
    private val starCoinRepository: StarCoinRepository
) : ViewModel() {

    private val _starPoints = MutableStateFlow(0)
    val starPoints: StateFlow<Int> = _starPoints

    private val _starTotalPoints = MutableStateFlow<Int?>(null)
    val starTotalPoints: StateFlow<Int?> = _starTotalPoints


    // Alışkanlık puanını alma
    fun getStarPoints(habitId: Int) {
        viewModelScope.launch {
            try {
                starCoinRepository.getStarPoints(habitId).collect { starPoint ->
                    _starPoints.value = starPoint ?: 0
                }
            } catch (e: Exception) {
                Log.e("star", "Hata: ${e.localizedMessage}")
            }
        }
    }

    // Alışkanlık puanını güncelle
    fun updateHabitStarPoints(habitId: Int, newPoints: Int) {
        viewModelScope.launch {
            try {
                starCoinRepository.updateHabitStarPoints(habitId, newPoints)
            } catch (e: Exception) {
                Log.e("star", "Hata: ${e.localizedMessage}")
            }
        }
    }


    fun getTotalStarPoints() {
        viewModelScope.launch {
            try {
                starCoinRepository.getTotalStarPoints().collect { totalStar ->
                    Log.d("StarCoinViewModel", "Alınan veri: $totalStar")
                    _starTotalPoints.value = totalStar
                }
            }catch (e:Exception){
                Log.e("star", "Hata: ${e.localizedMessage}")

            }
        }
    }

}