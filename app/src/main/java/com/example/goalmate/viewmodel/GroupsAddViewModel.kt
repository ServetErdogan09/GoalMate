package com.example.goalmate.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.ChatMessage
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.extrensions.GroupCreationState
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.extrensions.GroupListState
import com.example.goalmate.extrensions.MessagesState
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
import com.example.goalmate.utils.NetworkUtils
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.goalmate.data.localdata.GroupCloseVoteState
import com.example.goalmate.data.localdata.GroupHabitStats
import com.example.goalmate.data.localdata.GroupHabits
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class GroupsAddViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _groupCreationState =
        MutableStateFlow<GroupCreationState>(GroupCreationState.Loading)
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

    private val _chatMessage = MutableStateFlow<MessagesState>(MessagesState.Loading)
    val chatMessage: StateFlow<MessagesState> = _chatMessage.asStateFlow()

    // Messages list to store fetched messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _habitCompletedToday = MutableStateFlow<Map<String,Boolean>>(emptyMap())
    val habitCompletedToday: StateFlow<Map<String,Boolean>> = _habitCompletedToday.asStateFlow()

    private val _voteToCloseGroup = MutableStateFlow<(Map<String , Boolean>)>(emptyMap())
    val voteToCloseGroup : StateFlow<Map<String,Boolean>> = _voteToCloseGroup.asStateFlow()

    private val _groupCloseVoteState = MutableStateFlow<Map<String, GroupCloseVoteState>>(emptyMap())
    val groupCloseVoteState: StateFlow<Map<String, GroupCloseVoteState>> = _groupCloseVoteState.asStateFlow()



    // Flag to track if cleanup is already in progress
    private var isCleanupRunning = false

    init {
        getGroupList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createGroup(
        groupName: String,
        category: String,
        frequency: String,
        isPrivate: Boolean,
        participationType: String,
        maxParticipantNumber: Int,
        startDelay: Int,
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
                _groupCreationState.value =
                    GroupCreationState.Failure("Kullanıcı oturumu bulunamadı")
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
            val currentTime = NetworkUtils.getTime(context)
            // startDeadline = currentTime + (2 * 60 * 1000)
             val startDeadline = currentTime + (startDelay * 24 * 60 * 60 * 1000L) // günü milisaniyeye çevir

            val minParticipationCount = when{
                maxParticipantNumber <= 3 -> 2
                else -> maxParticipantNumber / 2
            }

            // Firestore'a kaydedilecek grup verisi
            val groupData = hashMapOf(
                "groupId" to groupId,
                "groupName" to groupName,
                "category" to category,
                "frequency" to frequency,
                "private" to isPrivate,
                "participationType" to participationType,
                "muxParticipationCount" to maxParticipantNumber,
                "minParticipationCount" to minParticipationCount, // Minimum katılımcı sayısı
                "groupStartTime" to startDelay.toString(),
                "description" to description,
                "createdAt" to currentTime,
                "createdBy" to currentUserId,
                "quote" to "",
                "groupCode" to "",
                "habitDuration" to habitDuration,
                "members" to listOf(currentUserId),
                "groupStatus" to "WAITING",
                "startDeadline" to startDeadline,
                "actualStartDate" to null
            )

            // Grup oluşturma ve kullanıcı güncelleme işlemlerini transaction içinde yap
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(currentUserId)
                val groupRef = db.collection("groups").document(groupId)



                // Grup oluştur
                transaction.set(groupRef, groupData)

                // Kullanıcının katıldığı gruplara ekle
                transaction.update(userRef, "joinedGroups", joinedGroups + groupId)

                // GroupHabits alt koleksiyonunu oluştur
                val groupHabitsRef = userRef.collection("groupHabits").document(groupId)
                transaction.set(groupHabitsRef, GroupHabits(
                    habitName = groupName,
                    completedDays = 0,
                    uncompletedDays = 0,
                    completedTime = currentTime
                )
                )
            }.await()


            // UI'ı güncelle
            val newGroup = Group(
                groupId = groupId,
                groupName = groupName,
                category = category,
                frequency = frequency,
                isPrivate = isPrivate,
                participationType = participationType,
                muxParticipationCount = maxParticipantNumber,
                minParticipationCount = maxParticipantNumber / 2,
                groupStartTime = startDelay.toString(),
                description = description,
                createdAt = currentTime,
                createdBy = currentUserId,
                quote = "",
                groupCode = "",
                habitDuration = habitDuration,
                members = listOf(currentUserId),
                groupStatus = "WAITING",
                startDeadline = startDeadline,
                actualStartDate = null
            )

            val currentGroups =
                (_groupListState.value as? GroupListState.Success)?.groups ?: emptyList()
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





 // kullanıcı tamamladığını firestore kaydediyoruz duurmu
 @RequiresApi(Build.VERSION_CODES.O)
 fun markHabitAsCompleted(groupId: String, isCompleted: Boolean = true, context: Context){
        viewModelScope.launch {
            try {
               val currentUserId = auth.currentUser?.uid ?: return@launch
               // val currentTime = System.currentTimeMillis()
                val currentTime = NetworkUtils.getTime(context)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

               val completedDaysRef = db.collection("users")
                   .document(currentUserId)
                   .collection("groupHabits")
                   .document(groupId)

                completedDaysRef.get().addOnSuccessListener {completed->
                        if (completed.exists()){
                            val completedDays = completed.getLong("completedDays")?.toInt() ?:0
                            val uncompletedDays = completed.getLong("uncompletedDays")?.toInt() ?:0
                            val lastCompletionTime = completed.getLong("completedTime") ?: 0
                            val lastCompletionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date(lastCompletionTime))
                            val wasCompletedToday = completed.getBoolean("wasCompletedToday") ?: false

                            val updates = mutableMapOf<String, Any>()
                            
                            // Eğer yeni bir günse
                            if (lastCompletionDate != today) {
                                if (isCompleted) {
                                    updates["completedDays"] = completedDays + 1
                                } else {
                                    updates["uncompletedDays"] = uncompletedDays + 1
                                }
                            } else {
                                // Aynı gün içinde durum değişikliği
                                if (isCompleted && !wasCompletedToday) {
                                    // Tamamlanmamıştan tamamlandıya
                                    updates["completedDays"] = completedDays + 1
                                    if (uncompletedDays > 0) {
                                        updates["uncompletedDays"] = uncompletedDays - 1
                                    }
                                } else if (!isCompleted && wasCompletedToday) {
                                    // Tamamlanmıştan tamamlanmamışa
                                    if (completedDays > 0) {
                                        updates["completedDays"] = completedDays - 1
                                    }
                                    updates["uncompletedDays"] = uncompletedDays + 1
                                }
                            }
                            
                            updates["completedTime"] = currentTime
                            updates["wasCompletedToday"] = isCompleted

                            completedDaysRef.update(updates).addOnSuccessListener {
                                Log.d("Firestore", "Alışkanlık durumu başarıyla güncellendi")
                                _habitCompletedToday.value += (groupId to isCompleted)
                            }
                            .addOnFailureListener {e->
                                Log.e("Firestore", "Güncelleme başarısız oldu", e)
                            }
                        }else{
                            // Eğer döküman yoksa yeni oluştur
                            val initialData = hashMapOf(
                                "completedDays" to if (isCompleted) 1 else 0,
                                "uncompletedDays" to if (isCompleted) 0 else 1,
                                "completedTime" to currentTime,
                                "wasCompletedToday" to isCompleted
                            )
                            completedDaysRef.set(initialData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Yeni alışkanlık kaydı oluşturuldu")
                                    _habitCompletedToday.value += (groupId to isCompleted)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Yeni kayıt oluşturma başarısız oldu", e)
                                }
                        }
                }

            }catch (e:Exception){
                Log.e("GroupsAddViewModel", "Alışkanlık tamamlandı olarak işaretlenirken bir hata oluştu", e)            
            }
        }
    }


    // tamamlanıp tammalanmadığını kontrol et
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkHabitCompletion(groupId: String , context: Context){
        viewModelScope.launch {
            val currentServerTime = NetworkUtils.getTime(context = context )
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // GroupHabits koleksiyonundan kontrol et
                val habitRef = db.collection("users")
                    .document(currentUserId)
                    .collection("groupHabits")
                    .document(groupId)
                    .get()
                    .await()

                if (habitRef.exists()) {
                    val lastCompletionTime = habitRef.getLong("completedTime") ?: 0
                    val lastCompletionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(lastCompletionTime))
                    val wasCompletedToday = habitRef.getBoolean("wasCompletedToday") ?: false

                    // Eğer son tamamlama tarihi bugün değilse veya hiç tamamlanmamışsa
                    if (lastCompletionDate != today) {
                        _habitCompletedToday.value += (groupId to false)
                    } else {
                        _habitCompletedToday.value += (groupId to wasCompletedToday)
                    }
                } else {
                    _habitCompletedToday.value += (groupId to false)
                }

            }catch (e:Exception){
                Log.e("GroupsAddViewModel", "Alışkanlık durumu kontrol edilirken hata oluştu", e)
                _habitCompletedToday.value += (groupId to false)
            }
        }
    }


    fun closeGroup(groupId: String) { // yöetici tarafından kapatılabilir grup
        viewModelScope.launch {
            db.collection("users").get()
                .addOnSuccessListener { documents ->
                    val batch = db.batch() // toplu işlem başlat

                    for (document in documents) {
                        val userRef = db.collection("users").document(document.id)
                        // members listesinden bu gurubu çıkar
                        batch.update(userRef, "members", FieldValue.arrayRemove(groupId))
                    }
                    batch.commit().addOnSuccessListener {
                        db.collection("groups").document(groupId)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Grup başarıyla silindi: $groupId.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Grubu silerken hata oluştu", e)
                            }
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Kullanıcıları güncellerken hata oluştu", e)

                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Kullanıcıları çekerken hata oluştu", e)

                }
        }
    }





    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val userRef = db.collection("users").document(currentUser.uid)
                val groupRef = db.collection("groups").document(groupId)

                db.runTransaction { transaction ->
                    // Grup bilgilerini al
                    val groupDoc = transaction.get(groupRef)
                    val group = groupDoc.toObject<Group>()

                    // Kullanıcı bilgilerini al
                    val userDoc = transaction.get(userRef)
                    val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()

                    if (group != null) {
                        // Gruptan kullanıcıyı çıkar
                        val updatedMembers = group.members.filter { it != currentUser.uid }
                        transaction.update(groupRef, "members", updatedMembers)

                        // Kullanıcının joinedGroups listesinden grubu çıkar
                        val updatedJoinedGroups = joinedGroups.filter { it != groupId }
                        transaction.update(userRef, "joinedGroups", updatedJoinedGroups)

                        Log.d("leaveGroup", "Grup güncelleniyor: $groupId")
                        Log.d("leaveGroup", "Eski üye listesi: ${group.members}")
                        Log.d("leaveGroup", "Yeni üye listesi: $updatedMembers")
                        Log.d("leaveGroup", "Kullanıcı güncelleniyor: ${currentUser.uid}")
                        Log.d("leaveGroup", "Eski katıldığı gruplar: $joinedGroups")
                        Log.d("leaveGroup", "Yeni katıldığı gruplar: $updatedJoinedGroups")

                        // Eğer son üye ayrılıyorsa grubu kapat
                        if (updatedMembers.isEmpty()) {
                            closeGroup(groupId)
                        }
                    }
                }.addOnSuccessListener {
                    Log.d("leaveGroup", "Kullanıcı başarıyla gruptan ayrıldı: ${currentUser.uid}")
                    viewModelScope.launch {
                        // Grup listelerini güncelle
                        getUserGroups()
                        resetGroupList()
                        // Grup detaylarını güncelle
                        getGroupById(groupId)
                    }
                }.addOnFailureListener { e ->
                    Log.e("leaveGroup", "Gruptan ayrılma hatası", e)
                }
            } catch (e: Exception) {
                Log.e("leaveGroup", "Gruptan ayrılma işlemi sırasında hata", e)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createMessagesFirebase(
        groupId: String,
        senderId: String,
        senderName: String,
        message: String,
        isCurrentUser: Boolean,
        context: Context
    ) {
        viewModelScope.launch {
            // Set loading state
            _chatMessage.value = MessagesState.Loading

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _chatMessage.value = MessagesState.Error("Kullanıcı oturumu bulunamadı")
                    return@launch
                }

                // Clean up old messages (older than 24 hours)
                cleanupOldMessages(groupId ,context)

                // Generate a unique ID for the message
                val messageId =
                    db.collection("groups").document(groupId).collection("messages").document().id

                // Get server time or use device time if server time is unavailable
                val timestamp = NetworkUtils.getTime(context = context )

                // Create message data map
                val messageMap: HashMap<String, Any> = hashMapOf(
                    "messageId" to messageId,
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "message" to message,
                    "timestamp" to timestamp,
                    "isCurrentUser" to isCurrentUser.toString() // Store as string to match expected format
                )

                // Update the state with the new message
                _chatMessage.value = MessagesState.Success(messageMap)

                // Save to Firebase
                db.collection("groups")
                    .document(groupId)
                    .collection("messages")
                    .document(messageId) // Use the generated ID for consistent references
                    .set(messageMap)
                    .addOnSuccessListener {
                        Log.d("GroupsAddViewModel", "Mesaj başarıyla eklendi: $messageId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("GroupsAddViewModel", "Mesaj gönderme hatası", e)
                        viewModelScope.launch {
                            _chatMessage.value =
                                MessagesState.Error("Mesajınız gönderilemedi. Lütfen internet bağlantınızı kontrol edip tekrar deneyin: ${e.localizedMessage}")
                        }
                    }
            } catch (e: Exception) {
                Log.e("GroupsAddViewModel", "Mesaj oluşturma hatası", e)
                _chatMessage.value =
                    MessagesState.Error("Beklenmeyen bir hata oluştu: ${e.localizedMessage}")
            }
        }
    }

    // Function to cleanup messages older than 24 hours
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun cleanupOldMessages(groupId: String , context: Context) {
        try {
            // Get current server time to ensure we don't depend on device time
            val currentServerTime = NetworkUtils.getTime(context = context)

            // Calculate the timestamp for 24 hours ago using server time
            val twentyFourHoursAgo = currentServerTime - (24 * 60 * 60 * 1000)

            Log.d(
                "MesajTemizleme",
                "Şu anki sunucu zamanı: " + formatTimestampForLog(currentServerTime)
            )
            Log.d(
                "MesajTemizleme",
                "Şundan eski mesajlar kontrol ediliyor: " + formatTimestampForLog(twentyFourHoursAgo)
            )
            Log.d(
                "MesajTemizleme",
                "Temizleme eşiği (24 saat önce): " + formatTimestampForLog(twentyFourHoursAgo)
            )

            // Query messages older than 24 hours
            val oldMessagesQuery = db.collection("groups")
                .document(groupId)
                .collection("messages")
                .whereLessThan("timestamp", twentyFourHoursAgo)
                .limit(100) // Process in batches to avoid overloading

            val oldMessages = oldMessagesQuery.get().await()

            if (!oldMessages.isEmpty) {
                Log.d("MesajTemizleme", "${oldMessages.size()} adet silinecek mesaj bulundu")

                // Delete each old message
                for (doc in oldMessages.documents) {
                    try {
                        val messageTimestamp = doc.getLong("timestamp") ?: 0
                        val messageText = doc.getString("message") ?: ""
                        val senderName = doc.getString("senderName") ?: ""
                        val messageId = doc.id

                        Log.d(
                            "MesajTemizleme",
                            "Mesaj siliniyor: \"$messageText\" gönderen: $senderName"
                        )
                        Log.d(
                            "MesajTemizleme",
                            "Mesaj zamanı: " + formatTimestampForLog(messageTimestamp)
                        )
                        Log.d(
                            "MesajTemizleme",
                            "Mesaj yaşı: ${(currentServerTime - messageTimestamp) / (1000 * 60 * 60)} saat"
                        )

                        // Ensure we're using await() to complete the delete operation before continuing
                        db.collection("groups")
                            .document(groupId)
                            .collection("messages")
                            .document(messageId)
                            .delete()
                            .await()

                        Log.d("MesajTemizleme", "BAŞARILI: Mesaj silindi - ID: $messageId")
                    } catch (e: Exception) {
                        Log.e("MesajTemizleme", "Mesaj silinirken hata oluştu: ${e.message}", e)
                    }
                }

                // Double check if deletion worked by trying to get the messages again
                val checkAfterDelete = db.collection("groups")
                    .document(groupId)
                    .collection("messages")
                    .whereLessThan("timestamp", twentyFourHoursAgo)
                    .get()
                    .await()

                if (checkAfterDelete.isEmpty) {
                    Log.d("MesajTemizleme", "Doğrulama: Eski mesajlar başarıyla temizlendi!")
                } else {
                    Log.d(
                        "MesajTemizleme",
                        "Doğrulama: Hala ${checkAfterDelete.size()} adet eski mesaj var, tekrar deneniyor..."
                    )
                    // If we reached the limit, there might be more messages to delete
                    if (oldMessages.size() >= 100) {
                        Log.d("MesajTemizleme", "Limit aşıldı, temizlemeye devam ediliyor...")
                        cleanupOldMessages(groupId,context) // Recursively delete more messages
                    }
                }
            } else {
                Log.d("MesajTemizleme", "Grup için silinecek mesaj bulunamadı: $groupId")
            }
        } catch (e: Exception) {
            Log.e("MesajTemizleme", "Eski mesajları temizlerken hata: ${e.message}", e)
        }
    }

    // Helper function to format timestamp for logging
    private fun formatTimestampForLog(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    fun getGroupById(groupId: String) {
        viewModelScope.launch {
            try {
                val groupDoc = db.collection("groups").document(groupId).get().await()
                if (groupDoc.exists()) {
                    val group = groupDoc.toObject<Group>()?.copy(
                        groupId = groupDoc.id,
                        groupName = groupDoc.getString("groupName") ?: "",
                        category = groupDoc.getString("category") ?: "",
                        frequency = groupDoc.getString("frequency") ?: "",
                        isPrivate = groupDoc.getBoolean("private") ?: false,
                        participationType = groupDoc.getString("participationType") ?: "",
                        muxParticipationCount = groupDoc.getLong("muxParticipationCount")
                            ?.toInt() ?: 15,
                        minParticipationCount = groupDoc.getLong("minParticipationCount")
                            ?.toInt() ?: 7,
                        groupStartTime = groupDoc.getString("groupStartTime") ?: "1",
                        description = groupDoc.getString("description") ?: "",
                        createdAt = groupDoc.getLong("createdAt") ?: 0,
                        habitDuration = groupDoc.getString("habitDuration") ?: "",
                        createdBy = groupDoc.getString("createdBy") ?: "",
                        quote = groupDoc.getString("quote") ?: "",
                        groupCode = groupDoc.getString("groupCode") ?: "",
                        members = groupDoc.get("members") as? List<String> ?: emptyList(),
                        groupStatus = groupDoc.getString("groupStatus") ?: "WAITING",
                        startDeadline = groupDoc.getLong("startDeadline") ?: 0,
                        actualStartDate = groupDoc.getLong("actualStartDate")
                    )
                    _groupDetailState.value = GroupDetailState.Success(group!!)
                } else {
                    _groupDetailState.value = GroupDetailState.Error("Grup bulunamadı")
                }
            } catch (e: Exception) {
                _groupDetailState.value =
                    GroupDetailState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
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
                        _joinGroupState.value =
                            "Tebrikler! 🎉 Grubumuza katıldınız, şimdi hep birlikte daha güçlüyüz!"
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
                    query = query.whereEqualTo("private", isPrivate)
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
                        document.toObject<Group>()?.copy(
                            groupId = document.id,
                            groupName = document.getString("groupName") ?: "",
                            category = document.getString("category") ?: "",
                            frequency = document.getString("frequency") ?: "",
                            isPrivate = document.getBoolean("private") ?: false,
                            participationType = document.getString("participationType") ?: "",
                            muxParticipationCount = document.getLong("muxParticipationCount")
                                ?.toInt() ?: 15,
                            minParticipationCount = document.getLong("minParticipationCount")
                                ?.toInt() ?: 7,
                            groupStartTime = document.getString("groupStartTime") ?: "1",
                            description = document.getString("description") ?: "",
                            createdAt = document.getLong("createdAt") ?: 0,
                            habitDuration = document.getString("habitDuration") ?: "",
                            createdBy = document.getString("createdBy") ?: "",
                            quote = document.getString("quote") ?: "",
                            groupCode = document.getString("groupCode") ?: "",
                            members = document.get("members") as? List<String> ?: emptyList(),
                            groupStatus = document.getString("groupStatus") ?: "WAITING",
                            startDeadline = document.getLong("startDeadline") ?: 0,
                            actualStartDate = document.getLong("actualStartDate")
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
                _groupListState.value =
                    GroupListState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
                Log.e("GroupsAdd", "Error fetching groups", e)
                isLoading = false
            }
        }
    }

    fun resetGroupList() {
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

    // Guruba katılma isteğin kontrol edildiği ve gurup kodun kontrol edildiği fonksiyon
    suspend fun requestJoinGroup(
        groupId: String,
        userId: String,
        joinCode: String?,
        participantNumber: Int,
        members: List<String>
    ) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()
            val maxAllowedGroups = userDoc.getLong("maxAllowedGroups")?.toInt() ?: 3

            if (members.size >= participantNumber) {
                _joinGroupState.value = "Üzgünüz, bu grup dolmuş. Yeni katılım yapılamaz."
                return
            }

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

            // Grup aktif mi kontrol et
            val groupStatus = group.getString("groupStatus")
            if (groupStatus == "ACTIVE") {
                _joinGroupState.value = "Bu grup aktif durumda. Yeni katılımlar kabul edilmiyor."
                return
            }

            // Üyelik kontrolü
            val currentMembers = group.get("members") as? List<String> ?: emptyList()
            if (currentMembers.contains(userId)) {
                _joinGroupState.value = "Bu grubun zaten üyesisiniz"
                return
            }

            val isPrivate = group.getBoolean("private") ?: false

            if (!isPrivate) {
                // Açık grup - direkt katılım
                addUserToGroup(groupId, userId)
                return
            }

            // Özel grup işlemleri
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
                return
            }

            // Katılım isteği gönderme
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
        } catch (e: Exception) {
            Log.e("GroupJoin", "Error in requestJoinGroup", e)
            _joinGroupState.value = "Bir hata oluştu: ${e.message}"
        }
    }


    // eğer alışkanlıklar tamamlandıysa bunlarda ekleme yapılacak
    suspend fun updateGroupStats(frequency: String){
        try {
            val currentUserId = auth.currentUser?.uid ?:return
            val userRef = db.collection("users").document(currentUserId)
            val statsRef = userRef.collection("stats").document("habitStats")


            db.runTransaction {transaction->
                val currentStates = transaction.get(statsRef).toObject<GroupHabitStats>() ?:GroupHabitStats()
                val updatedStats = when(frequency){
                    "Günlük" -> currentStates.copy(dailyGroupsCompleted = currentStates.dailyGroupsCompleted + 1)
                    "Haftalık"-> currentStates.copy(weeklyGroupsCompleted = currentStates.weeklyGroupsCompleted + 1 )
                    "Aylık"-> currentStates.copy(monthlyGroupsCompleted = currentStates.monthlyGroupsCompleted + 1)
                    else -> currentStates
                }

                transaction.set(statsRef, updatedStats, SetOptions.merge())

            }.await()

        }catch (e:Exception){
            Log.e("GroupStats", "Error updating group stats", e)
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
                            groupDoc.toObject<Group>()?.copy(
                                groupId = groupDoc.id,
                                groupName = groupDoc.getString("groupName") ?: "",
                                category = groupDoc.getString("category") ?: "",
                                frequency = groupDoc.getString("frequency") ?: "",
                                isPrivate = groupDoc.getBoolean("private") ?: false,
                                participationType = groupDoc.getString("participationType") ?: "",
                                muxParticipationCount = groupDoc.getLong("muxParticipationCount")
                                    ?.toInt() ?: 15,
                                minParticipationCount = groupDoc.getLong("minParticipationCount")
                                    ?.toInt() ?: 7,
                                groupStartTime = groupDoc.getString("groupStartTime") ?: "1",
                                description = groupDoc.getString("description") ?: "",
                                createdAt = groupDoc.getLong("createdAt") ?: 0,
                                habitDuration = groupDoc.getString("habitDuration") ?: "",
                                createdBy = groupDoc.getString("createdBy") ?: "",
                                quote = groupDoc.getString("quote") ?: "",
                                groupCode = groupDoc.getString("groupCode") ?: "",
                                members = groupDoc.get("members") as? List<String> ?: emptyList(),
                                groupStatus = groupDoc.getString("groupStatus") ?: "WAITING",
                                startDeadline = groupDoc.getLong("startDeadline") ?: 0,
                                actualStartDate = groupDoc.getLong("actualStartDate")
                            )
                        } else null
                    }

                    _myGroups.value = groups
                }
            } catch (e: Exception) {
                Log.e("GroupsAddViewModel", "Error fetching user groups", e)
            }
        }
    }

    // Helper function to get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }



    // Helper function to get current user name - cached or fetch from Firestore
    fun getCurrentUserName(): String? {
        val userId = getCurrentUserId() ?: return null
        // Check if we already have this user's name cached
        val cachedName = _userNames.value[userId]
        if (cachedName != null) {
            return cachedName
        }

        // If not cached, try to fetch (will be async, but at least future calls will have it)
        viewModelScope.launch {
            try {
                val document = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (document.exists()) {
                    val userName = document.getString("name") ?: "Misafir"
                    _userNames.value += (userId to userName)
                }
            } catch (e: Exception) {
                Log.e("GroupsAddViewModel", "Error fetching user name", e)
            }
        }

        return "Misafir" // Default fallback name if not immediately available
    }

    // Listen for group messages in real-time
    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupMessages(groupId: String, context: Context) {
        viewModelScope.launch {
            try {
                // Set initial loading state
                _chatMessage.value = MessagesState.Loading

                // Clean up old messages first
                cleanupOldMessages(groupId,context)

                // Listen for vote state changes
                listenToVoteState(groupId)

                // Get current user ID to determine which messages are from the current user
                val currentUserId = getCurrentUserId()

                // Create a listener for real-time updates
                val messagesRef = db.collection("groups")
                    .document(groupId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)

                // Use addSnapshotListener for real-time updates
                messagesRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("GroupsAddViewModel", "Error listening for messages", error)
                        _chatMessage.value = MessagesState.Error("Mesajlar yüklenirken hata oluştu")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val messagesList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val messageId = doc.getString("messageId") ?: ""
                                val senderId = doc.getString("senderId") ?: ""
                                val senderName = doc.getString("senderName") ?: ""
                                val messageText = doc.getString("message") ?: ""
                                val timestamp = doc.getLong("timestamp") ?: 0L
                                // Determine if the message is from current user based on sender ID
                                val isCurrentUser = (senderId == currentUserId)

                                // Ensure we load profile image for this user
                                if (!_profileImages.value.containsKey(senderId)) {
                                    getProfile(senderId)
                                }

                                // Ensure we have the user name
                                if (!_userNames.value.containsKey(senderId)) {
                                    getUsersName(senderId)
                                }

                                ChatMessage(
                                    messageId = messageId,
                                    senderId = senderId,
                                    senderName = senderName,
                                    message = messageText,
                                    timestamp = timestamp,
                                    isCurrentUser = isCurrentUser
                                )
                            } catch (e: Exception) {
                                Log.e("GroupsAddViewModel", "Error parsing message", e)
                                null
                            }
                        }

                        _messages.value = messagesList

                        // If we have messages, update the success state with the last one
                        if (messagesList.isNotEmpty()) {
                            val lastMessage = messagesList.last()
                            val messageMap = hashMapOf<String, Any>(
                                "messageId" to lastMessage.messageId,
                                "senderId" to lastMessage.senderId,
                                "senderName" to lastMessage.senderName,
                                "message" to lastMessage.message,
                                "timestamp" to lastMessage.timestamp,
                                "isCurrentUser" to lastMessage.isCurrentUser.toString()
                            )
                            _chatMessage.value = MessagesState.Success(messageMap)
                            Log.e("GroupsAddViewModel", "GroupsAddViewModel:$messageMap")
                        } else {
                            // Empty message list is still a success state
                            _chatMessage.value = MessagesState.Success(hashMapOf("empty" to true))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupsAddViewModel", "Error in getGroupMessages", e)
                _chatMessage.value =
                    MessagesState.Error("Mesajlar yüklenirken beklenmeyen bir hata oluştu")
            }
        }
    }

    // Schedule automatic message cleanup for all user's groups
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleMessageCleanup(context: Context) {
        // Return early if already cleaning up
        if (isCleanupRunning) {
            Log.d("MesajTemizleme", "Temizleme işlemi zaten devam ediyor, yeni işlem atlanıyor")
            return
        }

        isCleanupRunning = true

        viewModelScope.launch {
            try {
                Log.d("MesajTemizleme", "Zamanlanmış mesaj temizleme başlatılıyor...")
                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    Log.d("MesajTemizleme", "Giriş yapmış kullanıcı yok, temizleme atlanıyor")
                    isCleanupRunning = false
                    return@launch
                }

                Log.d("MesajTemizleme", "Kullanıcının katıldığı gruplar alınıyor: $currentUserId")
                // Get user's joined groups
                val userDoc = db.collection("users").document(currentUserId).get().await()
                val joinedGroups = userDoc.get("joinedGroups") as? List<String> ?: emptyList()

                Log.d(
                    "MesajTemizleme",
                    "Kullanıcı ${joinedGroups.size} gruba üye, mesajlar temizleniyor..."
                )

                // Clean up messages in each group
                for (groupId in joinedGroups) {
                    try {
                        Log.d("MesajTemizleme", "$groupId kodlu grup için mesajlar temizleniyor")
                        cleanupOldMessages(groupId,context)
                        Log.d("MesajTemizleme", "$groupId kodlu grup için temizlik tamamlandı")
                    } catch (e: Exception) {
                        Log.e(
                            "MesajTemizleme",
                            "$groupId kodlu grup için mesaj temizleme sırasında hata: ${e.message}",
                            e
                        )
                    }
                }

                Log.d(
                    "MesajTemizleme",
                    "Zamanlanmış mesaj temizleme işlemi tüm gruplar için tamamlandı"
                )
            } catch (e: Exception) {
                Log.e(
                    "MesajTemizleme",
                    "Zamanlanmış mesaj temizleme sırasında hata: ${e.message}",
                    e
                )
            } finally {
                isCleanupRunning = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initiateGroupCloseVote(groupId: String , context: Context) {
        viewModelScope.launch {
            try {
                Log.d("OylamaBaslatma", "Grup kapatma oylaması başlatılıyor: $groupId")
                
                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    Log.e("OylamaBaslatma", "Kullanıcı oturumu bulunamadı")
                    return@launch
                }
                
                // Grup bilgilerini al
                val groupRef = db.collection("groups").document(groupId)
                val groupDoc = groupRef.get().await()
                val group = groupDoc.toObject<Group>()
                
                if (group?.createdBy != currentUserId) {
                    Log.e("OylamaBaslatma", "Sadece grup yöneticisi oylama başlatabilir")
                    return@launch
                }
                
                Log.d("OylamaBaslatma", "Grup üye sayısı: ${group.members.size}")
                
                val currentServerTime = NetworkUtils.getTime(context = context)
                val votingEndTime = currentServerTime + (24 * 60 * 60 * 1000)
                
                Log.d("OylamaBaslatma", "Oylama bitiş zamanı: ${formatTimestampForLog(votingEndTime)}")
                
                // Oylama verilerini hazırla
                val voteData = hashMapOf(
                    "votingEndTime" to votingEndTime,
                    "yesVotes" to 0,
                    "noVotes" to 0,
                    "totalMembers" to group.members.size,
                    "votedMembers" to listOf<String>(),
                    "initiatedBy" to currentUserId,
                    "initiatedAt" to currentServerTime
                )
                
                // Önce mevcut oylamayı kontrol et ve temizle
                val existingVoteRef = groupRef.collection("closeVote").document("status")
                val existingVote = existingVoteRef.get().await()
                if (existingVote.exists()) {
                    Log.d("OylamaBaslatma", "Mevcut oylama siliniyor")
                    existingVoteRef.delete().await()
                }
                
                // Yeni oylamayı oluştur
                groupRef.collection("closeVote").document("status")
                    .set(voteData)
                    .addOnSuccessListener {
                        Log.d("OylamaBaslatma", "Oylama başarıyla oluşturuldu")
                        
                        // State'i güncelle
                        _groupCloseVoteState.value += (groupId to GroupCloseVoteState(
                            votingEndTime = votingEndTime,
                            yesVotes = 0,
                            noVotes = 0,
                            totalMembers = group.members.size,
                            hasUserVoted = false,
                            canAdminInitiateVote = false
                        ))
                    }
                    .addOnFailureListener { e ->
                        Log.e("OylamaBaslatma", "Oylama oluşturulurken hata: ${e.localizedMessage}")
                    }
                
                // 24 saat sonra oylama sonucunu kontrol et
                scheduleVoteCheck(groupId, votingEndTime , context)
                
            } catch (e: Exception) {
                Log.e("OylamaBaslatma", "Beklenmeyen hata: ${e.localizedMessage}")
            }
        }
    }

    fun submitVote(groupId: String, isYesVote: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("OyVerme", "Oy verme işlemi başlatılıyor...")
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val groupRef = db.collection("groups").document(groupId)
                val voteRef = groupRef.collection("closeVote").document("status")

                // Önce dokümanın varlığını kontrol et
                val voteDoc = voteRef.get().await()
                if (!voteDoc.exists()) {
                    Log.e("OyVerme", "Oylama dokümanı bulunamadı")
                    return@launch
                }

                Log.d("OyVerme", "Oylama dokümanı bulundu, transaction başlatılıyor")
                
                db.runTransaction { transaction ->
                    val currentVoteDoc = transaction.get(voteRef)
                    val votedMembers = currentVoteDoc.get("votedMembers") as? List<String> ?: listOf()
                    
                    if (currentUserId !in votedMembers) {
                        val yesVotes = currentVoteDoc.getLong("yesVotes")?.toInt() ?: 0
                        val noVotes = currentVoteDoc.getLong("noVotes")?.toInt() ?: 0
                        
                        Log.d("OyVerme", "Mevcut oylar - Evet: $yesVotes, Hayır: $noVotes")
                        Log.d("OyVerme", "Kullanıcı ${if (isYesVote) "EVET" else "HAYIR"} oyu kullanıyor")
                        
                        val updates = mutableMapOf<String, Any>()
                        if (isYesVote) {
                            updates["yesVotes"] = yesVotes + 1
                        } else {
                            updates["noVotes"] = noVotes + 1
                        }
                        updates["votedMembers"] = votedMembers + currentUserId
                        
                        transaction.update(voteRef, updates)
                        Log.d("OyVerme", "Oy başarıyla kaydedildi")
                    } else {
                        Log.d("OyVerme", "Kullanıcı zaten oy kullanmış")
                    }
                }.addOnSuccessListener {
                    Log.d("OyVerme", "Transaction başarıyla tamamlandı")
                    viewModelScope.launch {
                        updateVoteState(groupId)
                    }
                }.addOnFailureListener { e ->
                    Log.e("OyVerme", "Transaction sırasında hata: ${e.localizedMessage}")
                }
                
            } catch (e: Exception) {
                Log.e("OyVerme", "Oy verme işlemi sırasında hata: ${e.localizedMessage}")
            }
        }
    }

    private fun updateVoteState(groupId: String) {
        viewModelScope.launch {
            try {
                val voteRef = db.collection("groups").document(groupId)
                    .collection("closeVote").document("status")
                val voteDoc = voteRef.get().await()
                
                if (voteDoc.exists()) {
                    val currentState = GroupCloseVoteState(
                        votingEndTime = voteDoc.getLong("votingEndTime") ?: 0,
                        yesVotes = voteDoc.getLong("yesVotes")?.toInt() ?: 0,
                        noVotes = voteDoc.getLong("noVotes")?.toInt() ?: 0,
                        totalMembers = voteDoc.getLong("totalMembers")?.toInt() ?: 0,
                        hasUserVoted = auth.currentUser?.uid in (voteDoc.get("votedMembers") as? List<String> ?: emptyList()),
                        canAdminInitiateVote = false
                    )
                    _groupCloseVoteState.value += (groupId to currentState)
                }
            } catch (e: Exception) {
                Log.e("GroupClose", "Error updating vote state", e)
            }
        }
    }

    // oy kontrolu zamanla
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleVoteCheck(groupId: String, votingEndTime: Long , context: Context) {
        viewModelScope.launch {
            val currentServerTime = NetworkUtils.getTime(context = context)
            try {
                val delayMillis = votingEndTime - currentServerTime
                if (delayMillis > 0) {
                    kotlinx.coroutines.delay(delayMillis)
                    checkVoteResult(groupId)
                }
            } catch (e: Exception) {
                Log.e("GroupClose", "Error scheduling vote check", e)
            }
        }
    }

    private suspend fun checkVoteResult(groupId: String) {
        try {
            val voteRef = db.collection("groups").document(groupId)
                .collection("closeVote").document("status")
            val voteDoc = voteRef.get().await()
            
            if (voteDoc.exists()) {
                val yesVotes = voteDoc.getLong("yesVotes")?.toInt() ?: 0
                val noVotes = voteDoc.getLong("noVotes")?.toInt() ?: 0
                val totalMembers = voteDoc.getLong("totalMembers")?.toInt() ?: 0
                
                // Eğer evet oyları çoğunluktaysa grubu kapat
                if (yesVotes > noVotes && yesVotes > totalMembers / 2) {
                    closeGroup(groupId)
                }
                
                // Oylama sonuçlarını arşivle ve state'i temizle
                voteRef.delete()
                _groupCloseVoteState.value -= groupId
            }
        } catch (e: Exception) {
            Log.e("GroupClose", "Error checking vote result", e)
        }
    }

    private fun listenToVoteState(groupId: String) {
        viewModelScope.launch {
            try {
                val voteRef = db.collection("groups").document(groupId)
                    .collection("closeVote").document("status")
                
                voteRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("VoteState", "Error listening to vote state", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        val votingEndTime = snapshot.getLong("votingEndTime") ?: 0
                        val yesVotes = snapshot.getLong("yesVotes")?.toInt() ?: 0
                        val noVotes = snapshot.getLong("noVotes")?.toInt() ?: 0
                        val totalMembers = snapshot.getLong("totalMembers")?.toInt() ?: 0
                        val votedMembers = snapshot.get("votedMembers") as? List<String> ?: emptyList()
                        val currentUserId = getCurrentUserId()
                        
                        val voteState = GroupCloseVoteState(
                            votingEndTime = votingEndTime,
                            yesVotes = yesVotes,
                            noVotes = noVotes,
                            totalMembers = totalMembers,
                            hasUserVoted = currentUserId in votedMembers,
                            canAdminInitiateVote = false
                        )
                        
                        _groupCloseVoteState.value += (groupId to voteState)
                    } else {
                        // Oylama yoksa state'ten kaldır
                        _groupCloseVoteState.value -= groupId
                    }
                }
            } catch (e: Exception) {
                Log.e("VoteState", "Error setting up vote state listener", e)
            }
        }
    }

}