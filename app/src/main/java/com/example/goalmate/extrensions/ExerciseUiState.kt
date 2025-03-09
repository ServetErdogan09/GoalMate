package com.example.goalmate.extrensions

import com.example.goalmate.data.localdata.Group
import com.example.goalmate.data.localdata.Habit

sealed class ExerciseUiState {
    data object Loading : ExerciseUiState()
    data class Success(val habits: List<Habit>, val message: String? = null) : ExerciseUiState()
    data class Error(val message: String) : ExerciseUiState()
}

sealed class GroupCreationState {
    data class Success(val message: String? = null) : GroupCreationState()
    data class Failure(val message: String) : GroupCreationState()
    data object NoInternet : GroupCreationState()
    data object Loading : GroupCreationState()
}

sealed class GroupListState {
    data object Loading : GroupListState()
    data class Success(val groups: List<Group>) : GroupListState()
    data class Error(val message: String) : GroupListState()
}

sealed class GroupDetailState {
    data object Loading : GroupDetailState()
    data class Success(val group: Group) : GroupDetailState()
    data class Error(val message: String) : GroupDetailState()
}
