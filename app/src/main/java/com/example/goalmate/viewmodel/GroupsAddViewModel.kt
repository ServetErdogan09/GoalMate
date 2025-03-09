package com.example.goalmate.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.extrensions.GroupCreationState
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.extrensions.GroupListState
import com.example.goalmate.utils.NetworkUtils.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class GroupsAddViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _groupCreationState = MutableStateFlow<GroupCreationState>(GroupCreationState.Loading)
    val groupCreationState = _groupCreationState.asStateFlow()

    private val _profileImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val profileImages: StateFlow<Map<String, String>> = _profileImages.asStateFlow()

    private val _groupListState = MutableStateFlow<GroupListState>(GroupListState.Loading)
    val groupListState = _groupListState.asStateFlow()

    private val _groupDetailState = MutableStateFlow<GroupDetailState>(GroupDetailState.Loading)
    val groupDetailState = _groupDetailState.asStateFlow()

    init {
        getGroupList()
    }

    fun createGroup(
        groupName: String,
        category: String,
        frequency: String,
        isPrivate: Boolean,
        participationType: String,
        participantNumber: Int,
        description: String,
        context: Context
    ) {
        viewModelScope.launch {
            _groupCreationState.value = GroupCreationState.Loading

            try {
                if (!isNetworkAvailable(context)) {
                    _groupCreationState.value = GroupCreationState.NoInternet
                    return@launch
                }

                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    _groupCreationState.value = GroupCreationState.Failure("Kullanıcı oturumu bulunamadı")
                    return@launch
                }

                val groupId = db.collection("groups").document().id
                val group = hashMapOf(
                    "groupId" to groupId,
                    "groupName" to groupName,
                    "category" to category,
                    "frequency" to frequency,
                    "isPrivate" to isPrivate,
                    "participationType" to participationType,
                    "participantNumber" to participantNumber,
                    "description" to description,
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to currentUserId,
                    "members" to listOf(currentUserId)
                )

                db.collection("groups").document(groupId)
                    .set(group)
                    .addOnSuccessListener {
                        _groupCreationState.value = GroupCreationState.Success(
                            "Grup başarıyla oluşturuldu!"
                        )
                    }
                    .addOnFailureListener { e ->
                        _groupCreationState.value = GroupCreationState.Failure(
                            "Grup oluşturulurken bir hata oluştu: ${e.message}"
                        )
                        Log.e("GroupsAdd", "Error creating group", e)
                    }
            } catch (e: Exception) {
                _groupCreationState.value = GroupCreationState.Failure(
                    "Beklenmeyen bir hata oluştu: ${e.message}"
                )
                Log.e("GroupsAdd", "Unexpected error while creating group", e)
            }
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
                        members = (groupDocument.get("members") as? List<String>) ?: emptyList()
                    )
                    _groupDetailState.value = GroupDetailState.Success(group)
                } else {
                    _groupDetailState.value = GroupDetailState.Error("Grup bulunamadı")
                }
            } catch (e: Exception) {
                _groupDetailState.value = GroupDetailState.Error("Grup detayları yüklenirken hata oluştu: ${e.message}")
                Log.e("getGroupById", "getGroupById : veriler çekilirken hata oluştu")
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

    fun getGroupList() {
        viewModelScope.launch {
            _groupListState.value = GroupListState.Loading
            
            try {
                db.collection("groups")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _groupListState.value = GroupListState.Error("Gruplar yüklenirken hata oluştu: ${error.message}")
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
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
                                        members = (document.get("members") as? List<String>) ?: emptyList()
                                    )

                                } catch (e: Exception) {
                                    Log.e("GroupsAdd", "Error parsing group document", e)
                                    null
                                }
                            }
                            _groupListState.value = GroupListState.Success(groups)
                        }
                    }
            } catch (e: Exception) {
                _groupListState.value = GroupListState.Error("Beklenmeyen bir hata oluştu: ${e.message}")
                Log.e("GroupsAdd", "Error fetching groups", e)
            }
        }
    }
}