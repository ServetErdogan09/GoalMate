package com.example.goalmate.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.goalmate.data.localdata.DaoHabits
import com.example.goalmate.data.localdata.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId

class HabitRepository @Inject constructor(private val daoHabits: DaoHabits) {

    //alışkanlık ekleme
    suspend fun addExercise(habit: Habit): Long {
        val activeCount = daoHabits.getActiveHabitCount()
        Log.d("Repository", "Aktif alışkanlık sayısı: $activeCount")
        
        if (activeCount >= 20) {
            throw Exception("En fazla 5 aktif alışkanlık olabilir!")
        }

        val newHabit = habit.copy(
            lastCompletedDate = null,
            isCompleted = false
        )
        
        return daoHabits.insert(newHabit)
    }

    // tüm alışkanlıkları çek
    fun getAllExercises(): Flow<List<Habit>> {
        return daoHabits.getAllHabits()
    }

    // alışkanlık silmek için bir fonksiyon örneği
    suspend fun deleteExercise(habit: Habit) {
        try {
            daoHabits.deleteHabit(habit)
        } catch (e: Exception) {
            Log.e("Hatta", e.localizedMessage)
        }
    }

    suspend fun getActiveHabitCount(): Int {
        return try {
            val count = daoHabits.getActiveHabitCount()
            Log.d("Repository", "Aktif alışkanlık sayısı: $count")
            count
        } catch (e: Exception) {
            Log.e("Repository", "Alışkanlık sayısı alınırken hata: ${e.message}")
            0
        }
    }

    suspend fun updateHabit(habit: Habit) {
        try {
            daoHabits.updateHabit(habit)
            Log.e("habit", " güncel veriler ${habit}")
        } catch (e: Exception) {
            Log.e("Hatta", "günceleme hatası : ${e.localizedMessage}")
        }
    }



    suspend fun newUpdateHabit(habit: Habit) {
        try {
            daoHabits.newUpdateHabit(habit)
            Log.e("habit", " güncel veriler ${habit}")
        } catch (e: Exception) {
            Log.e("Hatta", "günceleme hatası : ${e.localizedMessage}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun resetHabit(currentTime: Long) {
        try {
            val habits = getAllExercises().first()
            if (habits.isEmpty()) return

            val istanbulZone = ZoneId.of("Europe/Istanbul")
            val today = Instant.ofEpochMilli(currentTime)
                .atZone(istanbulZone)
                .toLocalDate()

            // Her alışkanlığı kontrol et ve gerekiyorsa sıfırla
            val updatedHabits = habits.map { habit ->
                val lastCompletedDate = habit.lastCompletedDate?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(istanbulZone)
                        .toLocalDate()
                }

                // Eğer son tamamlanma tarihi bugünden farklıysa veya null ise sıfırla
                if (lastCompletedDate == null || !lastCompletedDate.isEqual(today)) {
                    Log.d("resetHabits", """
                        Alışkanlık sıfırlanıyor:
                        Habit: ${habit.name}
                        Last Completed: $lastCompletedDate
                        Today: $today
                    """.trimIndent())

                    habit.copy(
                        isCompleted = false,
                        lastResetDate = currentTime
                    )
                } else {
                    habit
                }
            }

            // Tüm güncellemeleri tek seferde yap
            daoHabits.updateAllHabits(updatedHabits)
            Log.d("resetHabits", "Alışkanlıklar yeni gün için güncellendi")

        } catch (e: Exception) {
            Log.e("resetHabits", "Sıfırlama hatası: ${e.message}")
        }
    }

    fun getHabitId(habitId: Int): Flow<Habit?> {
        return daoHabits.getHabitId(habitId)
    }
}


