package com.example.goalmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.RegistrationData
import com.example.goalmate.data.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.goalmate.data.RegistrationStep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.PhoneAuthProvider

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _registrationData = MutableStateFlow(RegistrationData())
    val registrationData: StateFlow<RegistrationData> = _registrationData.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Kayıt aşamasını takip etmek için
    private val _currentStep = MutableStateFlow(RegistrationStep.EMAIL_PASSWORD)
    val currentStep: StateFlow<RegistrationStep> = _currentStep.asStateFlow()



    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState = _verificationState.asStateFlow()



    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    if (currentUser.isEmailVerified) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.VerificationRequired
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun updateEmail(email: String) {
        _registrationData.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _registrationData.update { it.copy(password = password) }
    }

    fun updatePersonalInfo(name: String, surname: String, gender: String) {
        _registrationData.update {
            it.copy(
                name = name,
                surname = surname,
                gender = gender
            )
        }
    }

    fun updateBirthDate(day: String, month: String, year: String) {
        _registrationData.update {
            it.copy(
                birthDay = day,
                birthMonth = month,
                birthYear = year
            )
        }
    }

    private fun validateEmailPassword(): Boolean {
        val email = registrationData.value.email
        val password = registrationData.value.password

        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Lütfen email ve şifre alanlarını doldurunuz")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Lütfen geçerli bir email adresi giriniz")
            return false
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Şifre en az 6 karakter olmalıdır")
            return false
        }
        return true
    }



    fun moveToNextStep() {
        _currentStep.value = _currentStep.value.next()
    }

    fun moveToPreviousStep() {
        _currentStep.value = _currentStep.value.previous()
    }


    fun resendVerificationCode() {
        viewModelScope.launch {
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                _verificationState.value = VerificationState.Idle
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error("Kod gönderilemedi")
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Email formatı kontrolü
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Lütfen geçerli bir email adresi giriniz")
                    return@launch
                }

                // Şifre uzunluğu kontrolü
                if (password.length < 6) {
                    _authState.value = AuthState.Error("Şifre en az 6 karakter olmalıdır")
                    return@launch
                }

                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.reload()?.await()
                
                if (result.user?.isEmailVerified == true) {
                    _authState.value = AuthState.Success
                } else {
                    result.user?.sendEmailVerification()?.await()
                    _authState.value = AuthState.VerificationRequired
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("no user record") == true -> 
                        "Bu email adresi ile kayıtlı bir hesap bulunamadı"
                    e.message?.contains("password is invalid") == true -> 
                        "Girdiğiniz şifre hatalı"
                    e.message?.contains("blocked") == true -> 
                        "Çok fazla başarısız deneme. Lütfen daha sonra tekrar deneyin"
                    e.message?.contains("network") == true -> 
                        "İnternet bağlantınızı kontrol edin"
                    e.message?.contains("badly formatted") == true -> 
                        "Geçersiz email formatı"
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                        "Email veya şifre hatalı"
                    else -> "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun showError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun createUserWithEmailOnly() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (!validateEmailPassword()) {
                    return@launch
                }
                
                val email = registrationData.value.email
                val password = registrationData.value.password
                
                auth.createUserWithEmailAndPassword(email, password).await()
                auth.currentUser?.sendEmailVerification()?.await()
                
                _authState.value = AuthState.VerificationRequired
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("email address is already in use") == true -> 
                        "Bu email adresi zaten kullanımda"
                    e.message?.contains("password is invalid") == true -> 
                        "Şifre en az 8 karakter olmalıdır"
                    e.message?.contains("network") == true -> 
                        "İnternet bağlantınızı kontrol edin"
                    e.message?.contains("badly formatted") == true -> 
                        "Geçersiz email formatı"
                    else -> "Kayıt işlemi başarısız oldu. Lütfen daha sonra tekrar deneyin"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    // Email doğrulaması sonrası kullanıcı bilgilerini kaydetme
    fun saveUserDataAfterVerification() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userData = hashMapOf(
                        "email" to registrationData.value.email,
                        "name" to registrationData.value.name,
                        "surname" to registrationData.value.surname,
                        "gender" to registrationData.value.gender,
                        "birthDate" to "${registrationData.value.birthDay}/${registrationData.value.birthMonth}/${registrationData.value.birthYear}"
                    )
                    
                    db.collection("users").document(userId).set(userData).await()
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Kullanıcı bilgileri kaydedilemedi")
            }
        }
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}