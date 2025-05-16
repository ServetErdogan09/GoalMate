package com.example.goalmate.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.toLowerCase
import kotlinx.coroutines.CancellationException as JobCancellationException
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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.example.goalmate.utils.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.data.localdata.GroupHabitStats
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.SupervisorJob
import com.example.goalmate.data.repository.PointsRepository

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
   private val pointsRepository: PointsRepository,
    @ApplicationContext private val context: Context
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

    private val _maxAllowedGroups  = MutableStateFlow<Int>(2)
    val maxAllowedGroups : StateFlow<Int> = _maxAllowedGroups.asStateFlow()

    private val _showPasswordDialog = MutableStateFlow(false)
    val showPasswordDialog: StateFlow<Boolean> = _showPasswordDialog.asStateFlow()

    private val _tempPassword = MutableStateFlow("")
    val tempPassword: StateFlow<String> = _tempPassword.asStateFlow()

    // Yeni StateFlow ekleyelim
    private val _joinedGroupsCount = MutableStateFlow(0)
    val joinedGroupsCount: StateFlow<Int> = _joinedGroupsCount.asStateFlow()


    val userPoints: StateFlow<Int> = pointsRepository.userPoints

    init {
        _userName.value = getLocalUserName(context)
        checkCurrentUser()
        getCurrentUser(context)

        auth.currentUser?.let {user->
            viewModelScope.launch {
                listenToUserNameChanges(user.uid, context)
                pointsRepository.listenToPointChanges(user.uid,context)
                pointsRepository.initializeUserPoints(context)
            }

        }
    }




    fun calculateExitPenalty(frequency : String){
        viewModelScope.launch {
            try {
                pointsRepository.calculateExitPenalty(frequency)
            }catch (e: Exception){

            }
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
                            sharedPreferences.edit() { putString("user_name", firebaseName) }
                        }

                        if (!firebaseProfileImage.isNullOrEmpty()) {
                            _profileImage.value = firebaseProfileImage
                            sharedPreferences.edit() {
                                putString("profileImage", firebaseProfileImage)
                            }
                            Log.d("RegisterViewModel", "Updated profile image from Firebase: $firebaseProfileImage")
                        }
                        
                        // Kullanıcının katıldığı grupları ve maksimum grup limitini al
                        val joinedGroups = document.get("joinedGroups") as? List<*> ?: emptyList<String>()
                        _joinedGroupsCount.value = joinedGroups.size
                        
                        val maxAllowed = document.getLong("maxAllowedGroups")?.toInt() ?: 2
                        _maxAllowedGroups.value = maxAllowed
                        
                        Log.d("RegisterViewModel", "User joined groups: ${joinedGroups.size}/$maxAllowed")
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
                    try {
                        // Kullanıcının grup listesini al ve detaylı kontrol et
                        val rawJoinedGroups = snapshot.get("joinedGroups")
                        Log.d("RegisterViewModel", "Ham joinedGroups verisi: $rawJoinedGroups")
                        
                        val userJoinedGroups = when (rawJoinedGroups) {
                            is List<*> -> {
                                // Listedeki her elemanı kontrol et
                                rawJoinedGroups.filterNotNull().filter { groupId ->
                                    // groupId'nin geçerli bir string olduğunu kontrol et
                                    groupId is String && groupId.isNotBlank()
                                }
                            }
                            else -> {
                                Log.e("RegisterViewModel", "joinedGroups geçerli bir liste değil")
                                emptyList()
                            }
                        }

                        // Kullanıcının kendi limitini al
                        val userMaxAllowedGroups = snapshot.getLong("maxAllowedGroups")?.toInt() ?: 2

                        Log.d("RegisterViewModel", """
                            Grup Limit Kontrolü:
                            - Mevcut Grup Sayısı: ${userJoinedGroups.size}
                            - Maximum İzin: $userMaxAllowedGroups
                            - Katılabilir mi: ${userJoinedGroups.size < userMaxAllowedGroups}
                        """.trimIndent())
                        
                        // StateFlow'ları güncelle
                        viewModelScope.launch {
                            _joinedGroupsCount.value = userJoinedGroups.size
                            _maxAllowedGroups.value = userMaxAllowedGroups
                        }

                        // Diğer kullanıcı bilgilerini güncelle
                        val name = snapshot.getString("name")
                        val profileImage = snapshot.getString("profileImage")
                        
                        if (!name.isNullOrEmpty()) {
                            _userName.value = name
                        }
                        if (!profileImage.isNullOrEmpty()) {
                            _profileImage.value = profileImage
                        }

                    } catch (e: Exception) {
                        Log.e("RegisterViewModel", "Kullanıcı verilerini işlerken hata: ", e)
                    }
                }
            }
    }

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

    fun login(email: String, password: String,context: Context) {
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
                    // Kullanıcı bilgilerini hemen güncelle
                    getCurrentUser(context)
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
        sharedPreferences.edit() { clear() }
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
            putString("maxAllowedGroups", data.maxAllowedGroups.toString())
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

    fun createGroupCode(groupId: String) {
       val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
       val newCode =  (1..8).map { chars.random() }.joinToString("")
       Log.d("groupCode", "Oluşturulan grup kodu: $newCode")
        db.collection("groups").document(groupId)
            .update("groupCode",newCode)
            .addOnSuccessListener {
                Log.d("Firebase", "Grup kodu güncellendi: $newCode")
            }
            .addOnFailureListener {e->
                Log.e("Firebase", "Grup kodu güncellenirken hata oluştu: $e")

            }
    }
    fun saveUserToFirestore(userId: String, context: Context) {
        viewModelScope.launch {
            try {
                // Önce oturum durumunu kontrol et
                if (auth.currentUser == null) {
                    Log.e("RegisterViewModel", "User not authenticated")
                    _authState.value = AuthState.Error("Lütfen tekrar giriş yapın")
                    return@launch
                }

                Log.d("RegisterViewModel", "Current user ID: ${auth.currentUser?.uid}")
                Log.d("RegisterViewModel", "Saving data for user ID: $userId")

                // SharedPreferences'dan kayıt verilerini al
                val savedData = getRegistrationDataFromPrefs(context)
                Log.d("RegisterViewModel", "Retrieved saved data: $savedData")

                // Profil resmini al
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val profileImage = sharedPreferences.getString("profileImage", "") ?: ""

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fcmToken = task.result

                        // Transaction başlat
                        db.runTransaction { transaction ->
                            val userRef = db.collection("users").document(userId)

                            // Ana kullanıcı verilerini hazırla
                            val userData = hashMapOf(
                                "email" to savedData.email,
                                "name" to savedData.name,
                                "surname" to savedData.surname,
                                "gender" to savedData.gender,
                                "birthDate" to "${savedData.birthDay}/${savedData.birthMonth}/${savedData.birthYear}",
                                "profileImage" to profileImage,
                                "isEmailVerified" to true,
                                "createdAt" to System.currentTimeMillis(),
                                "joinedGroups" to listOf<String>(),
                                "fcmToken" to fcmToken,
                                "maxAllowedGroups" to 2,
                                "totalPoints" to 0
                            )

                            // Ana kullanıcı dökümanını oluştur
                            transaction.set(userRef, userData)
                               pointsRepository.saveUserPointsToLocal(context,0)

                            // GroupHabitStats alt koleksiyonunu oluştur
                            val statsRef = userRef.collection("stats").document("habitStats")
                            transaction.set(statsRef, GroupHabitStats(
                                dailyGroupsCompleted = 0,
                                weeklyGroupsCompleted = 0,
                                monthlyGroupsCompleted = 0,
                                adminCompletedGroups = 0,
                                kickedMemberCount = 0,
                                appUsageDays = 0
                            ))

                            // BadgesId alt koleksiyonunu oluştur
                            val badgesRef = userRef.collection("badges").document("earnedBadges")
                            transaction.set(badgesRef, hashMapOf(
                                "badgeIds" to listOf<Int>()
                            ))

                        }.addOnSuccessListener {
                            Log.d("RegisterViewModel", "User data and subcollections saved successfully")

                            viewModelScope.launch {
                                try {
                                    // Auth profilini güncelle
                                    auth.currentUser?.updateProfile(userProfileChangeRequest {
                                        displayName = savedData.name
                                    })?.await()

                                    Log.d("RegisterViewModel", "Updated Auth display name")

                                    // StateFlow'u güncelle
                                    _userName.value = savedData.name

                                    // SharedPreferences'ı temizle
                                    clearRegistrationDataFromPrefs(context)

                                    // ProfileScreen'e yönlendir
                                    _authState.value = AuthState.ProfileRequired

                                } catch (e: Exception) {
                                    Log.e("RegisterViewModel", "Error updating profile", e)
                                    _authState.value = AuthState.Error("Profil güncellenemedi: ${e.message}")
                                }
                            }
                        }.addOnFailureListener { e ->
                            Log.e("RegisterViewModel", "Error saving user data", e)
                            _authState.value = AuthState.Error("Kullanıcı bilgileri kaydedilemedi: ${e.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error in saveUserToFirestore", e)
                _authState.value = AuthState.Error("Kullanıcı bilgileri kaydedilemedi: ${e.message}")
            }
        }
    }

    fun updateProfileImage(imageUri: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                Log.d("RegisterViewModel", "Profil resmi güncelleniyor: $imageUri")
                
                val finalImagePath = when {
                    // Avatar seçilmişse (resource ID)
                    imageUri.all { it.isDigit() } -> {
                        Log.d("RegisterViewModel", "Avatar seçildi, ID: $imageUri")
                        imageUri
                    }
                    
                    imageUri.startsWith("content") -> {
                        Log.d("RegisterViewModel", "Galeri resmi Cloudinary'ye yükleniyor")
                        withContext(Dispatchers.IO) {
                            uploadImageToCloudinary(context, Uri.parse(imageUri))?.also {
                                Log.d("RegisterViewModel", "Cloudinary URL: $it")
                            } ?: run {
                                Log.e("RegisterViewModel", "Resim yüklenemedi")
                                return@withContext null
                            }
                        } ?: return@launch
                    }
                    
                    else -> imageUri
                }
                
                withContext(Dispatchers.Main) {
                    // StateFlow'u güncelle
                    _profileImage.value = finalImagePath
                    
                    // SharedPreferences'a kaydet
                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit() { putString("profileImage", finalImagePath) }
                }
                
                // Firestore'a kaydet
                auth.currentUser?.let { user ->
                    try {
                        withContext(Dispatchers.IO) {
                            db.collection("users").document(user.uid)
                                .update(mapOf(
                                    "profileImage" to finalImagePath,
                                    "lastUpdated" to System.currentTimeMillis()
                                ))
                                .await()
                            
                            Log.d("RegisterViewModel", "Profil resmi başarıyla güncellendi: $finalImagePath")
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterViewModel", "Firestore güncelleme hatası", e)
                        throw e
                    }
                }
                
            } catch (e: Exception) {
                if (e is JobCancellationException) {
                    Log.w("RegisterViewModel", "Profil resmi güncelleme işlemi iptal edildi", e)
                } else {
                    Log.e("RegisterViewModel", "Profil resmi güncellenirken hata oluştu", e)
                }
            }
        }
    }

    private suspend fun uploadImageToCloudinary(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Cloudinary'i başlat
                CloudinaryConfig.initCloudinary(context)

                // Yükleme sonucunu beklemek için CompletableDeferred kullan
                val result = CompletableDeferred<String?>()

                // Yükleme işlemini başlat
                CloudinaryConfig.getCloudinaryInstance()
                    .upload(imageUri)
                    .unsigned("goalmate_preset")
                    .option("folder", "profile_images")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d("Cloudinary", "Yükleme başladı")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100) / totalBytes
                            Log.d("Cloudinary", "Yükleme ilerlemesi: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["url"] as? String
                            result.complete(url)
                            Log.d("Cloudinary", "Yükleme başarılı: $url")
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e("Cloudinary", "Yükleme hatası: ${error.description}")
                            result.complete(null)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.e("Cloudinary", "Yükleme yeniden planlandı: ${error.description}")
                        }
                    })
                    .dispatch()

                // Sonucu bekle ve döndür
                try {
                    withTimeout(30000) { // 30 saniye timeout
                        result.await()
                    }
                } catch (e: Exception) {
                    Log.e("Cloudinary", "Yükleme timeout veya hata", e)
                    null
                }

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Cloudinary yükleme hatası", e)
                return@withContext null
            }
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

    private fun deleteUserPref(context: Context){
        val sharedPreferences =  context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit() { clear() }
    }

    fun updateTempPassword(password: String) {
        _tempPassword.value = password
    }

    fun showPasswordConfirmDialog() {
        _showPasswordDialog.value = true
    }

    fun hidePasswordConfirmDialog() {
        _showPasswordDialog.value = false
        _tempPassword.value = ""
    }

    fun deleteAccount(context: Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val user = auth.currentUser
                
                if (user != null) {
                    try {
                        if (_tempPassword.value.isEmpty()) {
                            _authState.value = AuthState.Error("Lütfen şifrenizi giriniz")
                            return@launch
                        }
                        
                        // Kullanıcının girdiği şifre ile kimlik doğrulaması yap
                        val credential = EmailAuthProvider.getCredential(user.email!!, _tempPassword.value)
                        user.reauthenticate(credential).await()
                        
                        // Kimlik doğrulaması başarılı olduysa hesabı sil
                        try {
                            // Önce Firestore verilerini sil
                            db.collection("users").document(user.uid)
                                .delete()
                                .await()
                            
                            // SharedPreferences'ı temizle
                            deleteUserPref(context)
                            
                            // Authentication hesabını sil
                            user.delete().await()
                            
                            // StateFlow'ları sıfırla
                            _userName.value = "Misafir"
                            _profileImage.value = ""
                            _tempPassword.value = ""
                            _authState.value = AuthState.Idle
                            
                            // Firebase'den çıkış yap
                            auth.signOut()
                            
                            hidePasswordConfirmDialog()
                            
                        } catch (e: Exception) {
                            Log.e("RegisterViewModel", "Hesap silinirken hata", e)
                            throw e
                        }
                        
                    } catch (e: FirebaseAuthRecentLoginRequiredException) {
                        Log.e("RegisterViewModel", "Yeniden kimlik doğrulaması gerekli", e)
                        _authState.value = AuthState.Error("Güvenlik nedeniyle hesabınızı silmek için lütfen şifrenizi tekrar giriniz")
                        showPasswordConfirmDialog()
                    } catch (e: Exception) {
                        Log.e("RegisterViewModel", "Kimlik doğrulama hatası", e)
                        _authState.value = AuthState.Error("Hatalı şifre")
                    }
                } else {
                    throw Exception("Oturum açmış kullanıcı bulunamadı")
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Hesap silme işlemi başarısız", e)
                _authState.value = AuthState.Error("Hesap silinemedi: ${e.message}")
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            try {
                // Önce AuthState'i güncelle
                _authState.value = AuthState.Loading
                
                // Firebase'den çıkış yap
                auth.signOut()
                
                // Kullanıcı verilerini temizle
                _userName.value = "Misafir"
                _profileImage.value = ""
                
                // SharedPreferences'ı temizle
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit() { clear() }
                
                // Kayıt verilerini temizle
                clearRegistrationDataFromPrefs(context)
                
                // Kısa bir gecikme ekle
                delay(100)
                
                // En son AuthState'i güncelle
                _authState.value = AuthState.Idle
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Çıkış yapılırken hata oluştu", e)
                _authState.value = AuthState.Error("Çıkış yapılamadı: ${e.message}")
            }
        }
    }

    // Kullanıcının grup sayılarını günceller
    suspend fun updateUserGroupCounts(userId: String) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val joinedGroups = userDoc.get("joinedGroups") as? List<*> ?: emptyList<String>()
                val maxAllowed = userDoc.getLong("maxAllowedGroups")?.toInt() ?: 2

                _joinedGroupsCount.value = joinedGroups.size
                _maxAllowedGroups.value = maxAllowed

                Log.d("RegisterViewModel", """
                    Grup Sayıları Güncellendi:
                    - Kullanıcı ID: $userId
                    - Mevcut Grup Sayısı: ${joinedGroups.size}
                    - Maximum İzin: $maxAllowed
                """.trimIndent())
            }
        } catch (e: Exception) {
            Log.e("RegisterViewModel", "Grup sayıları güncellenirken hata oluştu", e)
        }
    }

    fun canJoinMoreGroups(): Boolean {
        val currentCount = _joinedGroupsCount.value
        val maxAllowed = _maxAllowedGroups.value
        val canJoin = currentCount < maxAllowed
        
        Log.d("RegisterViewModel", """
            Grup Limit Kontrolü:
            - Mevcut Grup Sayısı: $currentCount
            - Maximum İzin: $maxAllowed
            - Katılabilir mi: $canJoin
        """.trimIndent())
        
        return canJoin
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}