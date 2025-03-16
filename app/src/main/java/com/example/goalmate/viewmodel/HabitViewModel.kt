package com.example.goalmate.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goalmate.data.localdata.Habit
import com.example.goalmate.data.localdata.HabitHistory
import com.example.goalmate.data.repository.HabitRepository
import com.example.goalmate.data.repository.HistoryHabitsRepository
import com.example.goalmate.data.repository.StarCoinRepository
import com.example.goalmate.extrensions.ExerciseUiState
import com.example.goalmate.utils.Constants.MAX_HABIT_COUNT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.goalmate.data.localdata.GroupRequest
import com.example.goalmate.extrensions.RequestStatus
import com.example.goalmate.extrensions.RequestsUiState
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val historyHabitsRepository: HistoryHabitsRepository,
    @ApplicationContext private val context: Context,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val _countActiveHabits = MutableStateFlow(0)
    val countActiveHabits: StateFlow<Int> = _countActiveHabits

    private val _habit = MutableStateFlow<Habit?>(null)
    val  habit : StateFlow<Habit?> = _habit

    private val _starAnimations = mutableStateListOf<Offset>()
    val starAnimations: List<Offset> get() = _starAnimations

    private val _currentTime = MutableStateFlow<Long>(0L)
    val currentTime: StateFlow<Long> get() = _currentTime

    private val _habitHistory = MutableStateFlow<List<HabitHistory>>(emptyList())
    val habitHistory: StateFlow<List<HabitHistory>> = _habitHistory

    private val _groupHabitHistory = MutableStateFlow<List<HabitHistory>>(emptyList())
    val groupHabitHistory: StateFlow<List<HabitHistory>> = _groupHabitHistory

    private val _isChecked = MutableStateFlow(false)
    val isChecked : StateFlow<Boolean> get() = _isChecked

    private val _habitRemainingDaysMap = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val habitRemainingDaysMap: StateFlow<Map<Int, Long>> = _habitRemainingDaysMap

    private val _requestsState = MutableStateFlow<RequestsUiState>(RequestsUiState.Loading)
    val requestsState: StateFlow<RequestsUiState> = _requestsState.asStateFlow()

    private val _allRequestsState = MutableStateFlow<RequestsUiState>(RequestsUiState.Loading)
    val allRequestsState: StateFlow<RequestsUiState> = _allRequestsState.asStateFlow()



    init {
        getServerTime()
        getCountActiveHabit()
        Log.d("Constants", "MAX_HABIT_COUNT değeri: ${MAX_HABIT_COUNT}")
        loadGroupRequests()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getServerTime() {
        viewModelScope.launch {
            try {
                val istanbulZone = ZoneId.of("Europe/Istanbul")

                val currentDateMillis = LocalDate.now(istanbulZone)
                    .atStartOfDay(istanbulZone)
                    .toInstant()
                    .toEpochMilli()

                _currentTime.value = currentDateMillis
                Log.e("currentTime", "Istanbul Date in Long: ${_currentTime.value}")
            } catch (e: Exception) {
                Log.e("currentTime", "Hata: ${e.message}")
            }
        }
    }


    fun checkDailyProgress() {
        viewModelScope.launch {
            try {
                val istanbulZone = ZoneId.of("Europe/Istanbul")
                val currentTime = Instant.now()
                    .atZone(istanbulZone)
                    .toLocalTime()

                // Eğer saat akşam 8'i geçtiyse ve tamamlanmamış alışkanlıklar varsa bildirim gönder
                if (currentTime.hour >= 20) {
                    when (val state = _uiState.value) {
                        is ExerciseUiState.Success -> {
                            val incompletedHabits = state.habits.count {
                                !it.isCompleted && !it.isExpired && getRemainingDays(it.finishDate, System.currentTimeMillis(), it) > 0
                            }

                            if (incompletedHabits > 0) {
                                //notificationHelper.showDailyReminder(incompletedHabits)
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error checking daily progress: ${e.message}")
            }
        }
    }


    // Alışkanlık tamamlandığında motivasyon mesajı göster
    fun showCompletionNotification(habitName: String) {
        val motivationalMessages = listOf(
            "Harika iş! $habitName alışkanlığını tamamladın!",
            "Muhteşem! Her gün daha iyiye gidiyorsun!",
            "Başardın! Bu şekilde devam et!",
            "Harika bir adım daha! Kendininle gurur duymalısın!"
        )

      //  notificationHelper.showHabitReminder("Tebrikler!", motivationalMessages.random())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateRemainingDays(habit: Habit, currentTime: Long) {
        viewModelScope.launch {
            try {
                val istanbulZone = ZoneId.of("Europe/Istanbul")
                val currentTimeIst = Instant.now()
                    .atZone(istanbulZone)
                    .toInstant()
                    .toEpochMilli()
                
                val remainingDays = getRemainingDays(habit.finishDate, currentTimeIst,habit)
                _habitRemainingDaysMap.value += (habit.id to remainingDays)
                
                Log.d("calculateRemainingDays", """
                    Habit: ${habit.name}
                    Habit ID: ${habit.id}
                    Finish Date: ${habit.finishDate}
                    Current Time: $currentTimeIst
                    Remaining Days: $remainingDays
                """.trimIndent())
            } catch (e: Exception) {
                Log.e("calculateRemainingDays", "Error: ${e.message}")
                _habitRemainingDaysMap.value += (habit.id to 0L)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRemainingDays(loadFinishDate: Long, currentTime: Long,habit: Habit): Long {
        try {
            val istanbulZone = ZoneId.of("Europe/Istanbul")
            
            val finishDate = Instant.ofEpochMilli(loadFinishDate)
                .atZone(istanbulZone)
                .toLocalDate()

            val currentDate = Instant.ofEpochMilli(currentTime)
                .atZone(istanbulZone)
                .toLocalDate()

            val remainingDays = ChronoUnit.DAYS.between(currentDate, finishDate)

            if (remainingDays <= 0 && !habit.isExpired) {
                markHabitAsExpired(habit)
            }
            
            Log.d("DateInfo", """
                Finish Date: $finishDate
                Current Date: $currentDate
                Remaining Days: $remainingDays
            """.trimIndent())

            return remainingDays
        } catch (e: Exception) {
            Log.e("DateInfo", "Error calculating remaining days: ${e.message}")
            return 0
        }
    }


    fun getExercises() {
        viewModelScope.launch {
            repository.getAllExercises()
                .catch { e ->
                    _uiState.value = ExerciseUiState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
                    Log.e("getExercises", "Hata: ${e.message}")
                }
                .collect { exerciseList ->
                    _uiState.value = ExerciseUiState.Success(exerciseList)
                    Log.d("GetExercises", "Alışkanlıklar güncellendi. Sayı: ${exerciseList.size}")
                }
        }
    }

    fun addExercise(habit: Habit) {
        viewModelScope.launch {
            try {
                val activeCount = repository.getActiveHabitCount()
                Log.d("HabitAdd", "Aktif alışkanlık sayısı: $activeCount, Maksimum limit: $MAX_HABIT_COUNT")
                if (activeCount >= MAX_HABIT_COUNT) {
                    Log.e("HabitAdd", "Maksimum limit aşıldı: $activeCount >= $MAX_HABIT_COUNT")
                    _uiState.value = ExerciseUiState.Error("En fazla $MAX_HABIT_COUNT aktif alışkanlık olabilir!")
                    return@launch
                }

                val habitId = repository.addExercise(habit)
                habit.id = habitId.toInt()
                Log.d("HabitAdd", "Yeni alışkanlık başarıyla eklendi. ID: $habitId")
                getExercises()
                getCountActiveHabit()

                Log.d("AddHabit", """
                    Yeni alışkanlık eklendi: 
                    İsim: ${habit.name}
                    ID: ${habitId}
                    Aktif Alışkanlık Sayısı: ${_countActiveHabits.value}
                    isExpired : ${habit.isExpired}
                """.trimIndent())
            } catch (e: Exception) {
                Log.e("HabitAdd", "Hata mesajı: ${e.message}")
                _uiState.value = ExerciseUiState.Error(e.message ?: "Alışkanlık eklenirken hata oluştu")
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(habit)
                getExercises()
                getCountActiveHabit()
                Log.d("HabitCount", "Habit deleted, updating count")
            } catch (e: Exception) {
                Log.e("HabitCount", "Error deleting habit: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            try {
                val istanbulZone = ZoneId.of("Europe/Istanbul")
                val currentTime = Instant.now()
                    .atZone(istanbulZone)
                    .toInstant()
                    .toEpochMilli()

                val lastCompletionDate = habit.lastCompletedDate?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(istanbulZone)
                        .toLocalDate()
                }
                
                val currentDate = Instant.ofEpochMilli(currentTime)
                    .atZone(istanbulZone)
                    .toLocalDate()


                if (lastCompletionDate?.isEqual(currentDate) == true) {
                    Log.d("HabitCompletion", "Bu alışkanlık bugün zaten tamamlanmış")
                    return@launch
                }

                val updatedHabit = habit.copy(
                    isCompleted = true,
                    completedDays = habit.completedDays + 1,
                    lastCompletedDate = currentTime
                )

                repository.updateHabit(updatedHabit)
                
                (_uiState.value as? ExerciseUiState.Success)?.let { currentState ->
                    val updatedList = currentState.habits.map { 
                        if (it.id == habit.id) updatedHabit else it 
                    }
                    _uiState.value = ExerciseUiState.Success(updatedList)
                }

            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Alışkanlık güncellenirken hata: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun resetHabitsForNewDay() {
        viewModelScope.launch {
            try {
                val istanbulZone = ZoneId.of("Europe/Istanbul")
                val currentTime = Instant.now()
                    .atZone(istanbulZone)
                    .toInstant()
                    .toEpochMilli()

                repository.resetHabit(currentTime)
                getExercises() // Güncel listeyi al
            } catch (e: Exception) {
                Log.e("resetHabits", "Hata: ${e.message}")
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {

                repository.updateHabit(habit)

                repository.getAllExercises().collect { updatedHabits ->
                    _uiState.value = ExerciseUiState.Success(updatedHabits)

                    calculateRemainingDays(habit, currentTime.value)

                    getCountActiveHabit()
                }

                Log.d("HabitUpdate", """
                Alışkanlık güncellendi: 
                İsim: ${habit.name}
                ID: ${habit.id}
                Tamamlandı mı: ${habit.isCompleted}
                Tamamlanan Gün: ${habit.completedDays}
                Başlangıç Tarihi: ${habit.startDate}
                Bitiş Tarihi: ${habit.finishDate}
            """.trimIndent())

            } catch (e: Exception) {
                Log.e("HabitUpdate", "Güncelleme hatası: ${e.message}")
                _uiState.value = ExerciseUiState.Error("Alışkanlık güncellenirken hata: ${e.message}")
            }
        }
    }

    fun getCountActiveHabit() {
        viewModelScope.launch {
            try {
                val countHabit = repository.getActiveHabitCount()
                Log.d("HabitCount", """
                    Önceki sayı: ${_countActiveHabits.value}
                    Yeni sayı: $countHabit
                    ----------------------
                """.trimIndent())
                
                _countActiveHabits.value = countHabit
            } catch (e: Exception) {
                Log.e("HabitCount", "Aktif alışkanlık sayısını alırken hata: ${e.message}")
                _countActiveHabits.value = 0
            }
        }
    }

    fun insertHabitHistory(habitHistory: HabitHistory) {
        viewModelScope.launch {
            try {
                Log.d("HabitAdd", "Alışkanlık başarıyla eklendi.")
                val habitHistoryId = historyHabitsRepository.addGroupsNormal(habitHistory)
                historyHabitsRepository.getTop10NormalHabits().collect{normalHabits->
                    if (normalHabits.size >= 10) {
                        historyHabitsRepository.deleteOldestNormalHabitHistory()
                    }
                }

                historyHabitsRepository.getTop10GroupHabits().collect{groupHabits->

                    if (groupHabits.size >= 10){
                        historyHabitsRepository.deleteOldestGroupHabitHistory()
                    }
                }

                Log.d(
                    "geçmişeklenen",
                    "silinen alışkanlık geçmişe eklendi ID: $habitHistoryId, alıskanlık adı : ${habitHistory.habitName} ,   start : ${habitHistory.startDate}  finish : ${habitHistory.endDate} , dayscompleted : ${habitHistory.daysCompleted}  , isGroup : ${habitHistory.habitType} "
                )
            }catch (e : Exception){
                _uiState.value =
                    ExerciseUiState.Error("Alışkanlık eklenirken hata: ${e.message}")
            }
        }
    }

    fun getNormalHabitHistory() {
        viewModelScope.launch {
            try {
                historyHabitsRepository.getTop10NormalHabits().collect { history ->
                    _habitHistory.value = history
                    if (history.isNotEmpty()) {
                        Log.d("habitHistory", "Normal Habit History: $history")
                    } else {
                        Log.d("habitHistory", "Normal alışkanlık geçmişi bulunamadı.")
                    }
                }
            } catch (e: Exception) {
                Log.e("historyError", "Normal alışkanlıkları çağırırken hata verdi: ${e.message}")
            }
        }
    }

    fun getGroupHabitHistory() {
        viewModelScope.launch {
            try {
                historyHabitsRepository.getTop10GroupHabits().collect { history ->
                    _groupHabitHistory.value = history
                    if (history.isNotEmpty()) {
                        history.forEach {
                            Log.d("group", "Habit Name: ${it.habitName}, Completed Days: ${it.daysCompleted}, id: ${it.id}, isGroup: ${it.frequency}")
                        }
                    } else {
                        Log.d("group", "Grup alışkanlıkları bulunamadı.")
                    }
                }
            } catch (e: Exception) {
                Log.e("historyError", "Group alışkanlıkları çağırırken hata verdi: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteHistoryHabits(){
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = Instant.now().minus(30,ChronoUnit.DAYS).toEpochMilli()
                historyHabitsRepository.deleteOldHabitHistory(thirtyDaysAgo)
            }catch (e:Exception){
                _uiState.value =
                    ExerciseUiState.Error("Alışkanlıklar Geçmişi silerken hata oluştu: ${e.message}")
                Log.e("sil","silinmedi")
            }
        }
    }

    fun addStarAnimation(position: Offset) {
        _starAnimations.add(position)
    }


    fun  getHabitById(habitId: Int){
        viewModelScope.launch {
            try {
                repository.getHabitId(habitId).collect{habit->
                    _habit.value= habit
                    Log.e("HabitViewModel","habit : $habit")
                }

            }catch (e:Exception){
                Log.e("HabitViewModel", "Veri çekerken hata verdi: ${e.message}")
            }
        }
    }

    fun onCheckboxClickedTrue(){
        _isChecked.value = true
        Log.e("isChecked","isChecked : ${_isChecked.value}")
    }

    fun onCheckboxClickedFalse(){
        _isChecked.value = false
        Log.e("isChecked","isChecked : ${_isChecked.value}")
    }

    private fun markHabitAsExpired(habit: Habit) {
        viewModelScope.launch {
            try {
                val updatedHabit = habit.copy(isExpired = true)
                repository.updateHabit(updatedHabit)

                repository.getAllExercises().collect{ newHabit->
                    _uiState.value = ExerciseUiState.Success(newHabit)
                }

                Log.d("HabitExpired", """
                Alışkanlık süresi doldu:
                İsim: ${habit.name}
                ID: ${habit.id}
                isExpired: ${updatedHabit.isExpired}
            """.trimIndent())
                getCountActiveHabit()

            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Alışkanlık güncellenirken hata: ${e.message}")
            }
        }
    }

    private fun loadGroupRequests() {
        viewModelScope.launch {
            try {
                _allRequestsState.value = RequestsUiState.Loading

                val currentUserId = auth.currentUser?.uid ?: return@launch

                // Kullanıcının yönettiği grupları dinle
                db.collection("groups")
                    .whereEqualTo("createdBy", currentUserId)
                    .addSnapshotListener { groupSnapshot, groupError ->
                        if (groupError != null) {
                            _requestsState.value = RequestsUiState.Error(
                                "Gruplar yüklenirken hata oluştu: ${groupError.message}"
                            )
                            return@addSnapshotListener
                        }

                        val groupIds = groupSnapshot?.documents?.mapNotNull { it.getString("groupId") }
                        if (groupIds.isNullOrEmpty()) {
                            _requestsState.value = RequestsUiState.Success(emptyList(), 0)
                            return@addSnapshotListener
                        }

                        // Grup isteklerini dinle
                        db.collection("groupRequests")
                            .whereIn("groupId", groupIds)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener { requestSnapshot, requestError ->
                                if (requestError != null) {
                                    _requestsState.value = RequestsUiState.Error(
                                        "İstekler yüklenirken hata oluştu: ${requestError.message}"
                                    )
                                    return@addSnapshotListener
                                }

                                viewModelScope.launch {
                                    try {
                                        val groupRequests = mutableListOf<GroupRequest>()
                                        var unreadCount = 0
                                        var hasNewRequest = false

                                        requestSnapshot?.documentChanges?.forEach { change ->
                                            val doc = change.document
                                            val userId = doc.getString("userId") ?: return@forEach
                                            val groupId = doc.getString("groupId") ?: return@forEach
                                            val status = doc.getString("status")?.let {
                                                RequestStatus.valueOf(it.uppercase())
                                            } ?: RequestStatus.PENDING
                                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                            val isRead = doc.getBoolean("isRead") ?: false

                                            // Yeni istek geldiğinde flag'i güncelle
                                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED && 
                                                !isRead && 
                                                status == RequestStatus.PENDING) {
                                                hasNewRequest = true
                                                unreadCount++
                                            }

                                            // Kullanıcı bilgilerini al
                                            val userDoc = db.collection("users").document(userId).get().await()
                                            val senderName = userDoc.getString("name") ?: "İsimsiz Kullanıcı"
                                            val senderImage = userDoc.getString("profileImage")

                                            // Grup bilgilerini al
                                            val groupDoc = db.collection("groups").document(groupId).get().await()
                                            val groupName = groupDoc.getString("groupName") ?: "İsimsiz Grup"

                                            groupRequests.add(
                                                GroupRequest(
                                                    id = doc.id,
                                                    groupId = groupId,
                                                    userId = userId,
                                                    senderName = senderName,
                                                    senderImage = senderImage,
                                                    groupName = groupName,
                                                    timestamp = timestamp,
                                                    status = status,
                                                    isRead = isRead
                                                )
                                            )
                                        }

                                        _requestsState.value = RequestsUiState.Success(
                                            requests = groupRequests,
                                            unreadCount = unreadCount,
                                            hasNewRequest = hasNewRequest
                                        )

                                    } catch (e: Exception) {
                                        _requestsState.value = RequestsUiState.Error(
                                            "İstekler işlenirken bir hata oluştu: ${e.message}"
                                        )
                                    }
                                }
                            }
                    }
            } catch (e: Exception) {
                Log.e("LoadAllRequests", "Genel hata: ${e.message}", e)
                _requestsState.value = RequestsUiState.Error(
                    "İstekler yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun markRequestAsRead(requestId: String) {
        viewModelScope.launch {
            try {
                db.collection("groupRequests")
                    .document(requestId)
                    .update("isRead", true)
                    .await()
                
                // İstekleri yeniden yükle
                loadAllRequests()
                loadGroupRequests()
                
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error marking request as read", e)
            }
        }
    }

    fun updateRequestStatus(requestId: String, newStatus: RequestStatus) {
        viewModelScope.launch {
            try {
                // Önce isteği getir
                val requestDoc = db.collection("groupRequests")
                    .document(requestId)
                    .get()
                    .await()

                val groupId = requestDoc.getString("groupId") ?: throw Exception("Grup ID bulunamadı")
                
                // Grup belgesini getir
                val groupDoc = db.collection("groups")
                    .document(groupId)
                    .get()
                    .await()

                // Kullanıcının grup yöneticisi olduğunu kontrol et
                if (groupDoc.getString("createdBy") != auth.currentUser?.uid) {
                    throw Exception("Bu işlem için yetkiniz yok")
                }


                db.collection("groupRequests")
                    .document(requestId)
                    .update("status", newStatus.name)
                    .await()

                delay(500)

                when (newStatus) {
                    RequestStatus.ACCEPTED -> {
                        try {
                            val userId = requestDoc.getString("userId")
                            
                            if (userId != null) {
                                // Kullanıcıyı gruba ekle
                                db.collection("groups")
                                    .document(groupId)
                                    .update(
                                        "members", com.google.firebase.firestore.FieldValue.arrayUnion(userId)
                                    )
                                    .await()

                                // Kullanıcının joinedGroups listesini güncelle
                                db.collection("users")
                                    .document(userId)
                                    .update(
                                        "joinedGroups", com.google.firebase.firestore.FieldValue.arrayUnion(groupId)
                                    )
                                    .await()
                            }

                            // İsteği sil
                            db.collection("groupRequests")
                                .document(requestId)
                                .delete()
                                .await()

                        } catch (e: Exception) {
                            Log.e("HabitViewModel", "Error processing accepted request", e)
                            throw e
                        }
                    }
                    RequestStatus.REJECTED -> {
                        try {
                            // İsteği sil
                            db.collection("groupRequests")
                                .document(requestId)
                                .delete()
                                .await()
                        } catch (e: Exception) {
                            Log.e("HabitViewModel", "Error deleting rejected request", e)
                            throw e
                        }
                    }
                    else -> {
                        // Başka bir şey yapmaya gerek yok
                    }
                }
                
                // İstekleri yeniden yükle
                loadAllRequests()
                loadGroupRequests()
                
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Error updating request status: ${e.message}", e)
                _allRequestsState.value = RequestsUiState.Error(
                    "İstek durumu güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }



    fun loadAllRequests() {
        viewModelScope.launch {
            try {
                _allRequestsState.value = RequestsUiState.Loading
                
                val currentUserId = auth.currentUser?.uid ?: run {
                    _allRequestsState.value = RequestsUiState.Error("Kullanıcı oturumu bulunamadı")
                    return@launch
                }

                // Kullanıcının yönettiği grupları al
                val userGroups = db.collection("groups")
                    .whereEqualTo("createdBy", currentUserId)
                    .get()
                    .await()

                val groupIds = userGroups.documents.mapNotNull { it.getString("groupId") }
                
                if (groupIds.isEmpty()) {
                    _allRequestsState.value = RequestsUiState.Success(emptyList(), 0)
                    return@launch
                }


                val requests = db.collection("groupRequests")
                    .whereIn("groupId", groupIds)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val groupRequests = mutableListOf<GroupRequest>()
                var unreadCount = 0

                for (doc in requests.documents) {
                    try {
                        val userId = doc.getString("userId") ?: continue
                        val groupId = doc.getString("groupId") ?: continue
                        val status = doc.getString("status")?.let {
                            RequestStatus.valueOf(it.uppercase())
                        } ?: RequestStatus.PENDING
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val isRead = doc.getBoolean("isRead") ?: false

                        if (!isRead && status == RequestStatus.PENDING) {
                            unreadCount++
                        }

                        // Kullanıcı bilgilerini al
                        val userDoc = db.collection("users").document(userId).get().await()
                        val senderName = userDoc.getString("name") ?: "İsimsiz Kullanıcı"
                        val senderImage = userDoc.getString("profileImage")

                        // Grup bilgilerini al
                        val groupDoc = db.collection("groups").document(groupId).get().await()
                        val groupName = groupDoc.getString("groupName") ?: "İsimsiz Grup"

                        groupRequests.add(
                            GroupRequest(
                                id = doc.id,
                                groupId = groupId,
                                userId = userId,
                                senderName = senderName,
                                senderImage = senderImage,
                                groupName = groupName,
                                timestamp = timestamp,
                                status = status,
                                isRead = isRead
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("LoadAllRequests", "İstek işlenirken hata: ${e.message}")
                        continue
                    }
                }


                _allRequestsState.value = RequestsUiState.Success(
                    requests = groupRequests,
                    unreadCount = unreadCount
                )

            } catch (e: Exception) {
                Log.e("LoadAllRequests", "Genel hata: ${e.message}", e)
                _allRequestsState.value = RequestsUiState.Error("İstekler yüklenirken hata oluştu: ${e.message}")
            }
        }
    }
}



