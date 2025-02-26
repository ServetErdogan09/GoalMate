package com.example.goalmate.data

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object VerificationRequired : AuthState()
    object ProfileRequired : AuthState()
    data class Error(val message: String) : AuthState()
} 