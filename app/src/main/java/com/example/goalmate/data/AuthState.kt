package com.example.goalmate.data

import com.example.goalmate.data.localdata.GroupRequest

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data object VerificationRequired : AuthState()
    data object ProfileRequired : AuthState()
    data class Error(val message: String) : AuthState()
}


