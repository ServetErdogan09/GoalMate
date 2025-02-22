package com.example.goalmate.extrensions

import com.example.goalmate.data.localdata.Habit

sealed class ExerciseUiState {
    data object Loading : ExerciseUiState()
    data class Success(val habits: List<Habit>, val message: String? = null) : ExerciseUiState()
    data class Error(val message: String) : ExerciseUiState()
}