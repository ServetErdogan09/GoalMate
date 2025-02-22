package com.example.goalmate.data.localdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.goalmate.data.localdata.HabitsPoints
import com.example.goalmate.data.localdata.UserPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPointsCoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitPoints(habitsPoints: HabitsPoints)

    @Insert
    suspend fun insertUserPoints(userPoints: UserPoints)

    // Kullanıcı toplam puanını almak için Flow döndüren fonksiyon
    @Query("SELECT totalStarPoints FROM user_points")
    fun getTotalStarPoints(): Flow<Int?>

    // Alışkanlığın puanını almak
    @Query("SELECT starPoints FROM habits_points WHERE habitId = :habitId")
    fun getStarPoints(habitId: Int): Flow<Int?>

    // Alışkanlık puanını güncelle
    @Query("UPDATE habits_points SET starPoints = :newPoints WHERE habitId = :habitId")
    suspend fun updateHabitStarPoints(habitId: Int, newPoints: Int)


    // Kullanıcı toplam puanını güncelle
    @Query("UPDATE user_points SET totalStarPoints = :newPoints")
    suspend fun updateUserPoints(newPoints: Int)


}

