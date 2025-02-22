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


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val historyHabitsRepository: HistoryHabitsRepository,
    private val starCoinRepository: StarCoinRepository,
    @ApplicationContext private val context: Context
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


    init {
        getServerTime()
        getCountActiveHabit()
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



    // Egzersizleri getir
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


    // Alışkanlık ekle
    fun addExercise(habit: Habit) {
        viewModelScope.launch {
            try {
                val activeCount = repository.getActiveHabitCount()
                Log.e("habit", "habit eklendi : $habit")
                if (activeCount >= MAX_HABIT_COUNT) {
                    _uiState.value = ExerciseUiState.Error("En fazla $MAX_HABIT_COUNT aktif alışkanlık olabilir!")
                    Log.e("habit", "max 5 geçti")
                    return@launch
                }

                val habitId = repository.addExercise(habit)
                habit.id = habitId.toInt()

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


    // -------------geçmiş hesaplamalar---------------------
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

    fun removeStarAnimation(position: Offset) {
        _starAnimations.remove(position)
    }


    //------------------- Analiz Hesaplama alanı----------------------------------

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

    // Alışkanlık süresi bittiğinde çağrılacak fonksiyon
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

}



