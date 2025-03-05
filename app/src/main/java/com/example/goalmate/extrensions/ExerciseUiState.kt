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

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val category: String = "",
    val frequency: String = "",
    val isPrivate: Boolean = false,
    val participationType: String = "",
    val participantNumber: Int = 0,
    val description: String = "",
    val createdAt: Long = 0,
    val createdBy: String = "",
    val members: List<String> = emptyList()
)