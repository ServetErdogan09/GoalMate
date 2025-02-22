package com.example.goalmate.data.localdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.goalmate.data.localdata.HabitHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitHistoryDao {

    @Insert
    suspend fun historyInsert(habitHistory: HabitHistory): Long

    // Normal alışkanlık türü için en eski kaydı sil (10'dan fazla varsa)
    @Query("""
        DELETE FROM habit_history
        WHERE habitType = 'normal'
        AND id = (
            SELECT id FROM habit_history
            WHERE habitType = 'normal'
            ORDER BY startDate ASC LIMIT 1
        )
    """)
    suspend fun deleteOldestNormalHabitHistory()

    // Grup alışkanlık türü için en eski kaydı sil (10'dan fazla varsa)
    @Query("""
        DELETE FROM habit_history
        WHERE habitType = 'group'
        AND id = (
            SELECT id FROM habit_history
            WHERE habitType = 'group'
            ORDER BY startDate ASC LIMIT 1
        )
    """)
    suspend fun deleteOldestGroupHabitHistory()


    @Query("SELECT COUNT(*) FROM habit_history WHERE habitType = 'group'")
    suspend fun getGroupHabitCount(): Int


    @Query("SELECT COUNT(*) FROM habit_history WHERE habitType = 'normal'")
    suspend fun getNormalHabitCount(): Int

    // 30 günden eski tüm geçmişleri sil
    @Query("DELETE FROM habit_history WHERE startDate < :thirtyDaysAgo")
    suspend fun deleteOldHabitHistory(thirtyDaysAgo: Long)

    // Normal alışkanlıklar için en son 10 kaydı al
    @Query("SELECT * FROM habit_history WHERE habitType ='normal' ORDER BY startDate DESC LIMIT 8")
    fun getTop10NormalHabits(): Flow<List<HabitHistory>>

    // Grup alışkanlıkları için en son 10 kaydı al
    @Query("SELECT * FROM habit_history WHERE habitType ='group' ORDER BY startDate DESC LIMIT 8")
    fun getTop10GroupHabits(): Flow<List<HabitHistory>>

    // Tüm alışkanlık geçmişini al
    @Query("SELECT * FROM habit_history")
    fun getAllHabitHistory(): Flow<List<HabitHistory>>


}