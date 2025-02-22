package com.example.goalmate.data.localdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedDayDao {

    @Insert
   suspend  fun insert(completedDay: CompletedDay)

    @Query("SELECT * FROM completed_days WHERE habitId=:habitId")
    fun getCompletedDays(habitId: Int): Flow<List<CompletedDay>>


    @Query("DELETE  FROM completed_days WHERE habitId =:habitId")
    suspend fun deleteHabit(habitId: Int)

    @Query("SELECT * FROM completed_days WHERE habitId = :habitId AND date = :date")
   suspend  fun getCompletedDayByDate(habitId: Int, date: Long): CompletedDay?

    @Query("UPDATE completed_days SET isCompleted = :completed WHERE habitId = :habitId AND date = :date")
   suspend  fun updateCompletedDay(habitId: Int, date: Long, completed: Boolean)

}