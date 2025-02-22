package com.example.goalmate.data.repository

import android.util.Log
import com.example.goalmate.data.localdata.HabitsPoints
import com.example.goalmate.data.localdata.UserPoints
import com.example.goalmate.data.localdata.UserPointsCoinDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StarCoinRepository @Inject constructor(
    private val userPointsCoinDao: UserPointsCoinDao
)
{

    suspend fun addHabitPoints(habitsPoints : HabitsPoints) {

        val currentPoints = userPointsCoinDao.getStarPoints(habitsPoints.habitId).first()

        if(currentPoints == null) {
            // Yeni kayıt oluştur
            userPointsCoinDao.insertHabitPoints(habitsPoints)
        } else {
            // Mevcut kaydı güncelle
            val newTotalPoints = currentPoints + habitsPoints.starPoints
            Log.e("addHabitPoints", "newTotalPoints: $newTotalPoints")
            userPointsCoinDao.updateHabitStarPoints(habitsPoints.habitId, newTotalPoints)
        }
    }


     fun getStarPoints(habitId: Int): Flow<Int?> {
        return userPointsCoinDao.getStarPoints(habitId)
    }

   suspend  fun updateHabitStarPoints(habitId: Int, newPoints: Int) {
        userPointsCoinDao.updateHabitStarPoints(habitId, newPoints)
    }

    fun getTotalStarPoints(): Flow<Int?> {
        return userPointsCoinDao.getTotalStarPoints()
    }


    private suspend fun updateUserPoints(userPoints: Int) {
        Log.e("updateUserPoints", "userPoints: $userPoints")
        return  userPointsCoinDao.updateUserPoints(userPoints)
    }


    suspend fun insertUserPoints(userPoints: UserPoints) {
        val currentPoints = userPointsCoinDao.getTotalStarPoints().first()
        Log.e("updateUserPoints", "currentPoints: $currentPoints")

        if(currentPoints == null) {
            // Yeni kayıt oluştur
            userPointsCoinDao.insertUserPoints(userPoints)
            Log.e("updateUserPoints", "Yeni kayıt oluşturuldu : $userPoints")
        }else{
            // Mevcut kaydı güncelle
            val newTotalPoints = currentPoints + userPoints.totalStarPoints
            Log.e("updateUserPoints", "newTotalPoints: $newTotalPoints")
            updateUserPoints(newTotalPoints)
        }
    }

}