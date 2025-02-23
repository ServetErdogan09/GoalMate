package com.example.goalmate.viewmodel

import android.content.Context
import android.util.Log
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
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext context: Context
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
        getCurrentUser(context)
    }

    private fun getCurrentUser(context: Context) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("RegisterViewModel", "Current user UID: ${currentUser.uid}")

            // Firebase'den kullanıcı bilgilerini al
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    Log.d("RegisterViewModel", "Firestore document: ${document.data}")
                    
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        Log.d("RegisterViewModel", "Retrieved name: $name")
                        
                        if (!name.isNullOrEmpty()) {
                            _registrationData.value = _registrationData.value.copy(name = name)
                            saveUserNameToPreferences(context, name)
                            Log.d("RegisterViewModel", "Name updated in RegistrationData: $name")
                        } else {
                            Log.d("RegisterViewModel", "Name is null or empty in Firestore")
                        }
                    } else {
                        Log.d("RegisterViewModel", "No document found in Firestore!")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterViewModel", "Error fetching data from Firestore!", e)
                }
        } else {
            Log.d("RegisterViewModel", "No user logged in!")
        }
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

    fun updateName(name: String) {
        _registrationData.update { currentData ->
            currentData.copy(name = name)
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
                if (password.length < 8) {
                    _authState.value = AuthState.Error("Şifre en az 8 karakter olmalıdır")
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

    fun createUserWithEmailOnly(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (!validateEmailPassword()) {
                    return@launch
                }
                
                val email = registrationData.value.email
                val password = registrationData.value.password
                
                // Kullanıcıyı oluştur
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Kullanıcı oluşturulduktan hemen sonra verileri kaydet
                result.user?.let { user ->
                    val userData = hashMapOf(
                        "email" to registrationData.value.email,
                        "name" to registrationData.value.name,
                        "surname" to registrationData.value.surname,
                        "gender" to registrationData.value.gender,
                        "birthDate" to "${registrationData.value.birthDay}/${registrationData.value.birthMonth}/${registrationData.value.birthYear}"
                    )
                    
                    try {
                        // Firestore'a verileri kaydet
                        db.collection("users").document(user.uid).set(userData).await()
                        Log.d("RegisterViewModel", "User data saved to Firestore: $userData")
                        
                        // Local'e de kaydet
                        saveUserNameToPreferences(context, registrationData.value.name)
                    } catch (e: Exception) {
                        Log.e("RegisterViewModel", "Error saving user data to Firestore", e)
                    }
                }
                
                // Email doğrulaması gönder
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

    // Kullanıcı kayıt olduktan sonra verileri kaydet
    fun saveUserDataAfterVerification(context: Context) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // Verilerin zaten kaydedilmiş olması gerekiyor, sadece kontrol edelim
                    db.collection("users").document(userId).get().await().let { document ->
                        if (!document.exists()) {
                            // Eğer veriler kayıtlı değilse tekrar kaydet
                            val userData = hashMapOf(
                                "email" to registrationData.value.email,
                                "name" to registrationData.value.name,
                                "surname" to registrationData.value.surname,
                                "gender" to registrationData.value.gender,
                                "birthDate" to "${registrationData.value.birthDay}/${registrationData.value.birthMonth}/${registrationData.value.birthYear}"
                            )
                            db.collection("users").document(userId).set(userData).await()
                        }
                    }
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Kullanıcı bilgileri kaydedilemedi")
            }
        }
    }


    fun saveUserNameToPreferences(context: Context, userName: String) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_name", userName)
            apply()
        }
    }


    fun getUserNameFromPreferences(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_name", "Misafir") ?: "Misafir"
    }


    fun clearUserPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}