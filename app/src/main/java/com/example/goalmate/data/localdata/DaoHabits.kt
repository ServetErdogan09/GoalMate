package com.example.goalmate.data.localdata

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoHabits {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(habit: Habit): Long

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT COUNT(*) FROM habits WHERE isExpired = 0")
    suspend fun getActiveHabitCount(): Int

    @Update
    suspend fun updateHabit(habit: Habit)

    @Update
    suspend fun updateAllHabits(habits: List<Habit>)

    @Update
    suspend fun newUpdateHabit(habit: Habit)

    @Query("UPDATE habits SET isCompleted = 0 WHERE isExpired = 0")
    suspend fun resetHabitsForNewDay()

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitId(habitId: Int): Flow<Habit?>

    @Query("UPDATE habits SET lastResetDate = :date WHERE id = :habitId")
    suspend fun updateLastResetDate(habitId: Long, date: Long)
}