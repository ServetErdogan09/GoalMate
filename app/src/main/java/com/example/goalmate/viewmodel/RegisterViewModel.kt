package com.example.goalmate.viewmodel

import android.content.Context
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.storage.StorageException

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

    // Kullanıcı adını takip etmek için yeni bir StateFlow ekleyelim
    private val _userName = MutableStateFlow<String>("Misafir")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _profileImage = MutableStateFlow<String>("")
    val profileImage: StateFlow<String> = _profileImage.asStateFlow()

    // Firebase Storage referansını ekleyelim
    private val storage = FirebaseStorage.getInstance()

    init {
        _userName.value = getLocalUserName(context)
        checkCurrentUser()
        getCurrentUser(context)

        auth.currentUser?.let {user->
            listenToUserNameChanges(user.uid, context)
        }
    }

    fun getCurrentUser(context: Context) {
        viewModelScope.launch {
            try {
                // Önce SharedPreferences'dan yükle
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val localProfileImage = sharedPreferences.getString("profileImage", "")
                val localUserName = sharedPreferences.getString("user_name", "Misafir")

                // Local verileri ayarla
                _userName.value = localUserName ?: "Misafir"
                if (!localProfileImage.isNullOrEmpty()) {
                    _profileImage.value = localProfileImage
                    Log.d("RegisterViewModel", "Loaded profile image from local: $localProfileImage")
                }

                // Firebase'den güncel verileri al
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val document = db.collection("users").document(currentUser.uid).get().await()
                    if (document.exists()) {
                        val firebaseName = document.getString("name")
                        val firebaseProfileImage = document.getString("profileImage")

                        // Firebase'den gelen verileri kontrol et ve güncelle
                        if (!firebaseName.isNullOrEmpty()) {
                            _userName.value = firebaseName
                            sharedPreferences.edit().putString("user_name", firebaseName).apply()
                        }

                        if (!firebaseProfileImage.isNullOrEmpty()) {
                            _profileImage.value = firebaseProfileImage
                            sharedPreferences.edit().putString("profileImage", firebaseProfileImage).apply()
                            Log.d("RegisterViewModel", "Updated profile image from Firebase: $firebaseProfileImage")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error in getCurrentUser", e)
            }
        }
    }

    private fun listenToUserNameChanges(userId: String, context: Context) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RegisterViewModel", "Kullanıcı bilgileri dinlenirken hata: ", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("name")
                    val profileImage = snapshot.getString("profileImage")
                    if (!name.isNullOrEmpty()) {
                        _userName.value = name
                    }
                    if (!profileImage.isNullOrEmpty()) {
                        _profileImage.value = profileImage
                        saveProfileImageToPreferences(context, profileImage)
                    }
                }
            }
    }

    // Local'den kullanıcı adını al
    private fun getLocalUserName(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_name", "Misafir") ?: "Misafir"
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
        Log.d("RegisterViewModel", "Updating email to: $email")
        _registrationData.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _registrationData.update { it.copy(password = password) }
    }

    fun updatePersonalInfo(name: String, surname: String, gender: String) {
        Log.d("RegisterViewModel", "Updating personal info: name=$name, surname=$surname, gender=$gender")
        _registrationData.update { currentData ->
            currentData.copy(
                name = name,
                surname = surname,
                gender = gender
            ).also {
                Log.d("RegisterViewModel", "Updated registration data: $it")
            }
        }
    }

    fun updateBirthDate(day: String, month: String, year: String) {
        Log.d("RegisterViewModel", "Updating birth date: $day/$month/$year")
        _registrationData.update { currentData ->
            currentData.copy(
                birthDay = day,
                birthMonth = month,
                birthYear = year
            ).also {
                Log.d("RegisterViewModel", "Updated registration data: $it")
            }
        }
    }

    fun updateName(name: String) {
        Log.d("RegisterViewModel", "Updating name to: $name")
        _registrationData.update { currentData ->
            currentData.copy(name = name).also {
                Log.d("RegisterViewModel", "Updated registration data: $it")
            }
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

    private fun clearRegistrationDataFromPrefs(context: Context) {
        val sharedPreferences = context.getSharedPreferences("registration_data", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    private fun saveRegistrationDataToPrefs(context: Context, data: RegistrationData) {
        val sharedPreferences = context.getSharedPreferences("registration_data", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("email", data.email)
            putString("name", data.name)
            putString("surname", data.surname)
            putString("gender", data.gender)
            putString("birthDay", data.birthDay)
            putString("birthMonth", data.birthMonth)
            putString("birthYear", data.birthYear)
            apply()
        }
    }

    private fun getRegistrationDataFromPrefs(context: Context): RegistrationData {
        val sharedPreferences = context.getSharedPreferences("registration_data", Context.MODE_PRIVATE)
        return RegistrationData(
            email = sharedPreferences.getString("email", "") ?: "",
            name = sharedPreferences.getString("name", "") ?: "",
            surname = sharedPreferences.getString("surname", "") ?: "",
            gender = sharedPreferences.getString("gender", "") ?: "",
            birthDay = sharedPreferences.getString("birthDay", "") ?: "",
            birthMonth = sharedPreferences.getString("birthMonth", "") ?: "",
            birthYear = sharedPreferences.getString("birthYear", "") ?: ""
        )
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
                
                // Kayıt verilerini SharedPreferences'a kaydet
                saveRegistrationDataToPrefs(context, registrationData.value)
                
                // Firebase Auth hesabı oluştur
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Email doğrulaması gönder
                authResult.user?.let { user ->
                    user.sendEmailVerification().await()
                    _authState.value = AuthState.VerificationRequired
                }
                
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
                    else -> "Kayıt işlemi başarısız oldu: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun saveUserToFirestore(userId: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "Starting to save user data for ID: $userId")
                
                // SharedPreferences'dan kayıt verilerini al
                val savedData = getRegistrationDataFromPrefs(context)
                Log.d("RegisterViewModel", "Retrieved saved data: $savedData")

                // Profil resmini al
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val profileImage = sharedPreferences.getString("profileImage", "") ?: ""

                // Kullanıcı verilerini hazırla
                val userData = hashMapOf(
                    "email" to savedData.email,
                    "name" to savedData.name,
                    "surname" to savedData.surname,
                    "gender" to savedData.gender,
                    "birthDate" to "${savedData.birthDay}/${savedData.birthMonth}/${savedData.birthYear}",
                    "profileImage" to profileImage,
                    "isEmailVerified" to true,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )

                Log.d("RegisterViewModel", "Prepared user data: $userData")

                // Firestore'a kaydet
                db.collection("users")
                    .document(userId)
                    .set(userData)
                    .await()

                Log.d("RegisterViewModel", "Successfully saved user data to Firestore")

                // Firebase Auth'da displayName'i güncelle
                auth.currentUser?.updateProfile(userProfileChangeRequest {
                    displayName = savedData.name
                })?.await()
                
                Log.d("RegisterViewModel", "Updated Auth display name")
                
                // StateFlow'u güncelle
                _userName.value = savedData.name
                
                // SharedPreferences'ı temizle
                clearRegistrationDataFromPrefs(context = context)
                
                // ProfileScreen'e yönlendir
                _authState.value = AuthState.ProfileRequired
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error saving user data", e)
                _authState.value = AuthState.Error("Kullanıcı bilgileri kaydedilemedi: ${e.message}")
            }
        }
    }

    // Email doğrulaması tamamlandığında çağrılacak fonksiyon
    fun updateEmailVerificationStatus(userId: String) {
        viewModelScope.launch {
            try {
                // Firestore'daki kullanıcı verisini güncelle
                db.collection("users").document(userId)
                    .update(
                        mapOf(
                            "isEmailVerified" to true,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // Kullanıcı bilgilerini al ve StateFlow'ları güncelle
                val userDoc = db.collection("users").document(userId).get().await()
                userDoc.data?.let { userData ->
                    _userName.value = userData["name"] as? String ?: "Misafir"
                }

                // ProfileScreen'e yönlendir
                _authState.value = AuthState.ProfileRequired
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error updating email verification status", e)
                _authState.value = AuthState.Error("Email doğrulama durumu güncellenemedi")
            }
        }
    }

    fun updateProfileImage(imageUri: String, context: Context) {
        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "Updating profile image: $imageUri")
                
                val finalImagePath = when {
                    // Resource ID ise (uygulama içi avatar)
                    imageUri.all { it.isDigit() } -> {
                        // Avatar seçilmişse direkt resource ID'yi kullan
                        Log.d("RegisterViewModel", "Avatar selected with resource ID: $imageUri")
                        imageUri
                    }
                    
                    // URI ise (galeriden seçilen)
                    imageUri.startsWith("content") -> {
                        Log.d("RegisterViewModel", "Uploading gallery image to Firebase Storage")
                        // Sadece galeri resimlerini Firebase Storage'a yükle
                        uploadImageToFirebaseStorage(Uri.parse(imageUri)) ?: run {
                            Log.e("RegisterViewModel", "Failed to upload image")
                            return@launch
                        }
                    }
                    
                    // Zaten bir URL ise (önceden yüklenmiş)
                    else -> imageUri
                }
                
                // StateFlow'u güncelle
                _profileImage.value = finalImagePath
                
                // SharedPreferences'a kaydet
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("profileImage", finalImagePath).apply()
                
                // Firestore'a kaydet
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userRef = db.collection("users").document(currentUser.uid)
                    userRef.update(mapOf(
                        "profileImage" to finalImagePath,
                        "lastUpdated" to System.currentTimeMillis()
                    )).await()
                    
                    Log.d("RegisterViewModel", "Profile image updated successfully: $finalImagePath")
                }
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error updating profile image", e)
            }
        }
    }

    // Firebase Storage'a resim yükleme fonksiyonu
    private suspend fun uploadImageToFirebaseStorage(uri: Uri): String? {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            val timestamp = System.currentTimeMillis()
            
            // Dosya yolunu kullanıcı ID'sine göre düzenle
            val fileName = "profile_images/${currentUser.uid}/profile_${timestamp}.jpg"
            val storageRef = storage.reference.child(fileName)
            
            Log.d("RegisterViewModel", "Starting image upload to: $fileName")
            
            // Resmi yükle
            val uploadTask = storageRef.putFile(uri).await()
            Log.d("RegisterViewModel", "Image upload completed")
            
            // Download URL'ini al
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("RegisterViewModel", "Download URL obtained: ${downloadUrl}")
            
            downloadUrl.toString()
            
        } catch (e: Exception) {
            Log.e("RegisterViewModel", "Error uploading image to Firebase Storage", e)
            when (e) {
                is StorageException -> {
                    Log.e("RegisterViewModel", "Storage error code: ${e.errorCode}")
                    Log.e("RegisterViewModel", "Storage error message: ${e.message}")
                }
                else -> Log.e("RegisterViewModel", "Unexpected error: ${e.message}")
            }
            null
        }
    }

    private fun saveProfileImageToPreferences(context: Context, imageUri: String) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("profile_image", imageUri)
            putString("profileImage", imageUri)  // Her iki key'e de kaydedelim
            apply()
        }
    }

    fun checkEmailVerification(onVerificationComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.reload()?.await()
                val isVerified = auth.currentUser?.isEmailVerified == true
                onVerificationComplete(isVerified)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Email verification check failed", e)
                onVerificationComplete(false)
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