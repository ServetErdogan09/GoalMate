package com.example.goalmate.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.data.localdata.GroupRequest
import com.example.goalmate.extrensions.GroupCreationState
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.extrensions.GroupListState
import com.example.goalmate.extrensions.RequestStatus
import com.example.goalmate.extrensions.RequestsUiState
import com.example.goalmate.utils.NetworkUtils.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

@HiltViewModel
class GroupsAddViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _groupCreationState = MutableStateFlow<GroupCreationState>(GroupCreationState.Loading)
    val groupCreationState = _groupCreationState.asStateFlow()

    private val _profileImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val profileImages: StateFlow<Map<String, String>> = _profileImages.asStateFlow()

    private val PAGE_SIZE = 8
    private var lastDocument: DocumentSnapshot? = null
    private var isLoading = false
    private var _hasMoreDataFlag = true
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private val _groupListState = MutableStateFlow<GroupListState>(GroupListState.Loading)
    val groupListState = _groupListState.asStateFlow()

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames.asStateFlow()

    private val _groupDetailState = MutableStateFlow<GroupDetailState>(GroupDetailState.Loading)
    val groupDetailState = _groupDetailState.asStateFlow()

    private val _joinGroupState = MutableStateFlow<String?>(null)
    val joinGroupState = _joinGroupState.asStateFlow()

    private var currentCategory: String = "Tümü"
    private var currentPrivacy: String? = null

    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups: StateFlow<List<Group>> = _myGroups.asStateFlow()

    private val _requestsState = MutableStateFlow<RequestsUiState>(RequestsUiState.Loading)
    val requestsState = _requestsState.asStateFlow()

    private val _maxAllowedGroups = MutableStateFlow(3)
    val maxAllowedGroups: StateFlow<Int> = _maxAllowedGroups.asStateFlow()

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError: StateFlow<String?> = _joinError.asStateFlow()

    init {
        getGroupList()
    }

    suspend fun createGroup(
        groupName: String,
        category: String,
        frequency: String,
        isPrivate: Boolean,
        participationType: String,
        participantNumber: Int,
        habitDuration: String,
        description: String,
        context: Context
    ): String? {
        _groupCreationState.value = GroupCreationState.Loading

        try {
            if (!isNetworkAvailable(context)) {
                _groupCreationState.value = GroupCreationState.NoInternet
                return null
            }

            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                _groupCreationState.value = GroupCreationState.Failure("Kullanıcı oturumu bulunamadı")
                return null
            }

            // Kullanıcının mevcut grup sayısını ve limitini kontrol et
            val userDoc = db.collection("users").document(currentUserId).get().await()
            val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()
            val maxAllowedGroups = userDoc.getLong("maxAllowedGroups")?.toInt() ?: 3

            if (joinedGroups.size >= maxAllowedGroups) {
                _groupCreationState.value = GroupCreationState.Failure(
                    "Maksimum grup limitine ulaştınız ($maxAllowedGroups). " +
                    "Daha fazla grup oluşturmak için limit yükseltmeniz gerekiyor."
                )
                return null
            }

            val groupId = db.collection("groups").document().id
            val newGroup = Group(
                groupId = groupId,
                groupName = groupName,
                category = category,
                frequency = frequency,
                isPrivate = isPrivate,
                participationType = participationType,
                participantNumber = participantNumber,
                description = description,
                createdAt = System.currentTimeMillis(),
                createdBy = currentUserId,
                quote = "",
                groupCode = "",
                habitDuration = habitDuration,
                members = listOf(currentUserId)
            )

            // Grup oluşturma ve kullanıcı güncelleme işlemlerini transaction içinde yap
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(currentUserId)
                val groupRef = db.collection("groups").document(groupId)
                
                // Grup oluştur
                transaction.set(groupRef, newGroup)
                
                // Kullanıcının katıldığı gruplara ekle
                transaction.update(userRef, "joinedGroups", joinedGroups + groupId)
            }.await()

            // UI'ı güncelle
            val currentGroups = (_groupListState.value as? GroupListState.Success)?.groups ?: emptyList()
            val updatedGroups = listOf(newGroup) + currentGroups
            _groupListState.value = GroupListState.Success(updatedGroups)
            
            val currentUserGroups = _myGroups.value
            _myGroups.value = listOf(newGroup) + currentUserGroups

            _groupCreationState.value = GroupCreationState.Success(
                message = "Grup başarıyla oluşturuldu"
            )

            return groupId

        } catch (e: Exception) {
            _groupCreationState.value = GroupCreationState.Failure(
                e.message ?: "Grup oluşturulurken bir hata oluştu"
            )
            Log.e("GroupsAdd", "Error creating group", e)
            return null
        }
    }

    fun getGroupById(groupId: String) {
        _groupDetailState.value = GroupDetailState.Loading
        viewModelScope.launch {
            try {
                val groupDocument = db.collection("groups").document(groupId).get().await()
                if (groupDocument.exists()) {
                    val group = Group(
                        groupId = groupDocument.getString("groupId") ?: "",
                        groupName = groupDocument.getString("groupName") ?: "",
                        category = groupDocument.getString("category") ?: "",
                        frequency = groupDocument.getString("frequency") ?: "",
                        isPrivate = groupDocument.getBoolean("isPrivate") ?: false,
                        participationType = groupDocument.getString("participationType") ?: "",
                        participantNumber = groupDocument.getLong("participantNumber")?.toInt() ?: 0,
                        description = groupDocument.getString("description") ?: "",
                        createdAt = groupDocument.getLong("createdAt") ?: 0,
                        createdBy = groupDocument.getString("createdBy") ?: "",
                        habitDuration = groupDocument.getString("habitDuration") ?: "",
                        quote = groupDocument.getString("quote") ?: "",
                        groupCode = groupDocument.getString("groupCode") ?: "",
                        members = (groupDocument.get("members") as? List<String>) ?: emptyList()
                    )
                    _groupDetailState.value = GroupDetailState.Success(group)
                } else {
                    _groupDetailState.value = GroupDetailState.Error("Grup bulunamadı")
                }
            } catch (e: Exception) {
                _groupDetailState.value = GroupDetailState.Error("Grup detayları yüklenirken hata oluştu: ${e.message}")
                Log.e("getGroupById", "getGroupById : veriler çekilirken hata oluştu", e)
            }
        }
    }

    fun getUsersName(userId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (document != null && document.exists()) {
                    val userName = document.getString("name") ?: "Misafir"
                    _userNames.value += (userId to userName)
                }
            } catch (e: Exception) {
                Log.e("users", "Kullanıcı ismini çekerken hata oluştu")
            }
        }
    }

    private fun addUserToGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val groupRef = db.collection("groups").document(groupId)
                    val userRef = db.collection("users").document(userId)
                    
                    transaction.update(groupRef, "members", FieldValue.arrayUnion(userId))
                    transaction.update(userRef, "joinedGroups", FieldValue.arrayUnion(groupId))
                }.addOnSuccessListener {
                    viewModelScope.launch {
                        getGroupById(groupId)
                        getUsersName(userId)
                        getProfile(userId)
                        _joinGroupState.value = "Hoş geldiniz! 🎉 Gruba başarıyla katıldınız, artık bir üyesisiniz!"
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error joining group", e)
                    _joinGroupState.value = when {
                        e.message?.contains("PERMISSION_DENIED") == true -> 
                            "Gruba katılma izniniz yok."
                        else -> "Gruba katılırken bir hata oluştu: ${e.localizedMessage}"
                    }
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error in addUserToGroup", e)
                _joinGroupState.value = "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"
            }
        }
    }

    fun getProfile(userId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (document != null && document.exists()) {
                    val photoUrl = document.getString("profileImage") ?: ""
                    _profileImages.value += (userId to photoUrl)
                }
            } catch (e: Exception) {
                Log.w("UserPhoto", "Kullanıcı verisi çekilemedi", e)
            }
        }
    }

    private fun getGroupList(isInitialLoad: Boolean = true) {
        if (isLoading || (!isInitialLoad && !_hasMoreDataFlag)) return
        isLoading = true

        viewModelScope.launch {
            try {
                var query = db.collection("groups")
                    .orderBy("createdAt", Query.Direction.DESCENDING)

                if (currentCategory != "Tümü" && currentCategory != "Özel" && currentCategory != "Açık") {
                    query = query.whereEqualTo("category", currentCategory)
                }

                if (currentPrivacy != null) {
                    val isPrivate = currentPrivacy == "Özel"
                    query = query.whereEqualTo("isPrivate", isPrivate)
                }

                query = query.limit(PAGE_SIZE.toLong())

                if (!isInitialLoad && lastDocument != null) {
                    query = query.startAfter(lastDocument!!)
                }

                val snapshot = query.get().await()
                
                if (snapshot.isEmpty) {
                    _hasMoreData.value = false
                    _hasMoreDataFlag = false
                    return@launch
                }

                lastDocument = snapshot.documents.lastOrNull()
                
                val groups = snapshot.documents.mapNotNull { document ->
                    try {
                        Group(
                            groupId = document.getString("groupId") ?: "",
                            groupName = document.getString("groupName") ?: "",
                            category = document.getString("category") ?: "",
                            frequency = document.getString("frequency") ?: "",
                            isPrivate = document.getBoolean("isPrivate") ?: false,
                            participationType = document.getString("participationType") ?: "",
                            participantNumber = document.getLong("participantNumber")?.toInt() ?: 0,
                            description = document.getString("description") ?: "",
                            createdAt = document.getLong("createdAt") ?: 0,
                            createdBy = document.getString("createdBy") ?: "",
                            habitDuration = document.getString("habitDuration") ?: "",
                            quote = document.getString("quote") ?: "",
                            groupCode = document.getString("groupCode") ?: "",
                            members = (document.get("members") as? List<String>) ?: emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("GroupsAdd", "Error parsing group document", e)
                        null
                    }
                }

                val currentGroups = if (isInitialLoad) {
                    groups
                } else {
                    val currentState = _groupListState.value as? GroupListState.Success
                    (currentState?.groups ?: emptyList()) + groups
                }

                _groupListState.value = GroupListState.Success(currentGroups)
                isLoading = false
            } catch (e: Exception) {
                _groupListState.value = GroupListState.Error("Beklenmeyen bir hata oluştu: ${e.message}")
                Log.e("GroupsAdd", "Error fetching groups", e)
                isLoading = false
            }
        }
    }

    private fun resetGroupList() {
        lastDocument = null
        _hasMoreDataFlag = true
        _hasMoreData.value = true
        isLoading = false
        _groupListState.value = GroupListState.Loading
        getGroupList(true)
    }

    fun resetJoinGroupState() {
        _joinGroupState.value = null
    }

    fun loadMoreGroups() {
        getGroupList(false)
    }

    fun setFilters(category: String) {
        when (category) {
            "Özel" -> {
                currentPrivacy = "Özel"
                currentCategory = "Tümü"
            }
            "Açık" -> {
                currentPrivacy = "Açık"
                currentCategory = "Tümü"
            }
            else -> {
                if (category == "Tümü") {
                    currentPrivacy = null
                }
                currentCategory = category
            }
        }
        resetGroupList()
    }

    suspend fun requestJoinGroup(groupId: String, userId: String, joinCode: String?) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()
            val maxAllowedGroups = userDoc.getLong("maxAllowedGroups")?.toInt() ?: 3

            // Sadece katıldığı grupları kontrol et
            if (joinedGroups.size >= maxAllowedGroups) {
                _joinGroupState.value = "Maksimum grup limitine ulaştınız (${maxAllowedGroups})"
                return
            }

            val groupRef = db.collection("groups").document(groupId)
            val group = groupRef.get().await()
            
            if (!group.exists()) {
                _joinGroupState.value = "Grup bulunamadı"
                return
            }

            // Üyelik kontrolü
            val members = group.get("members") as? List<String> ?: emptyList()
            if (members.contains(userId)) {
                _joinGroupState.value = "Bu grubun zaten üyesisiniz"
                return
            }

            // Mevcut istekleri kontrol et
            val existingRequests = db.collection("groupRequests")
                .whereEqualTo("userId", userId)
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            if (!existingRequests.isEmpty) {
                val request = existingRequests.documents.first()
                when (request.getString("status")) {
                    "pending" -> {
                        _joinGroupState.value = "Bu gruba zaten katılım isteği gönderdiniz"
                        return
                    }
                    "accepted" -> {
                        _joinGroupState.value = "Bu gruba zaten kabul edildiniz"
                        return
                    }
                    "rejected" -> {
                        _joinGroupState.value = "Bu gruba katılım isteğiniz reddedilmişti"
                        return
                    }
                }
            }

            val groupAdminId = group.getString("createdBy")
            val userName = userDoc.getString("name") ?: "Misafir"
            val groupName = group.getString("groupName") ?: "Grup"

            if (joinCode != null) {
                if (joinCode == group.getString("groupCode")) {
                    addUserToGroup(groupId, userId)
                } else {
                    _joinGroupState.value = "Geçersiz katılım kodu"
                }
            } else {
                val request = hashMapOf(
                    "adminId" to groupAdminId,
                    "userId" to userId,
                    "groupId" to groupId,
                    "status" to "pending",
                    "userName" to userName,
                    "groupName" to groupName,
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false
                )

                val requestRef = db.collection("groupRequests")
                    .add(request)
                    .await()

                if (groupAdminId != null) {
                    sendNotificationToAdmin(
                        adminId = groupAdminId,
                        userName = userName,
                        groupName = groupName,
                        requestId = requestRef.id,
                        groupId = groupId,
                        userId = userId
                    )
                }

                _joinGroupState.value = "Katılım isteği gönderildi"
            }
        } catch (e: Exception) {
            Log.e("GroupJoin", "Error in requestJoinGroup", e)
            _joinGroupState.value = "Bir hata oluştu: ${e.message}"
        }
    }

    private suspend fun sendNotificationToAdmin(
        adminId: String,
        userName: String,
        groupName: String,
        requestId: String,
        groupId: String,
        userId: String
    ) {
        try {
            // Bildirim verilerini hazırla
            val notificationData = hashMapOf(
                "title" to "Yeni Katılım İsteği",
                "body" to "$userName, $groupName grubuna katılmak istiyor",
                "type" to "JOIN_REQUEST",
                "requestId" to requestId,
                "groupId" to groupId,
                "userId" to userId
            )

            // Bildirimi gönder
            db.collection("notifications")
                .add(notificationData)
                .await()

            Log.d("FCM", "Bildirim kaydedildi")
        } catch (e: Exception) {
            Log.e("FCM", "Bildirim gönderilirken hata", e)
            // Hatayı yukarı fırlatma, sadece loglama yap
            // throw e
        }
    }

    fun getUserGroups() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userDoc = db.collection("users").document(userId).get().await()
                    val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()
                    val maxAllowed = userDoc.getLong("maxAllowedGroups")?.toInt() ?: 3

                    val groups = joinedGroups.mapNotNull { groupId ->
                        val groupDoc = db.collection("groups").document(groupId).get().await()
                        if (groupDoc.exists()) {
                            Group(
                                groupId = groupDoc.getString("groupId") ?: "",
                                groupName = groupDoc.getString("groupName") ?: "",
                                category = groupDoc.getString("category") ?: "",
                                frequency = groupDoc.getString("frequency") ?: "",
                                isPrivate = groupDoc.getBoolean("isPrivate") ?: false,
                                participationType = groupDoc.getString("participationType") ?: "",
                                participantNumber = groupDoc.getLong("participantNumber")?.toInt() ?: 0,
                                description = groupDoc.getString("description") ?: "",
                                createdAt = groupDoc.getLong("createdAt") ?: 0,
                                createdBy = groupDoc.getString("createdBy") ?: "",
                                habitDuration = groupDoc.getString("habitDuration") ?: "",
                                quote = groupDoc.getString("quote") ?: "",
                                members = (groupDoc.get("members") as? List<String>) ?: emptyList()
                            )
                        } else null
                    }

                    _maxAllowedGroups.value = maxAllowed
                    _myGroups.value = groups
                }
            } catch (e: Exception) {
                Log.e("GroupsAddViewModel", "Error fetching user groups", e)
            }
        }
    }
    
}


