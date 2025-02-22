package com.example.goalmate.data.repository

import com.example.goalmate.data.localdata.HabitHistory
import com.example.goalmate.data.localdata.HabitHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryHabitsRepository @Inject constructor(private val habitHistoryDao: HabitHistoryDao) {


    suspend fun deleteOldestNormalHabitHistory() {
        habitHistoryDao.deleteOldestNormalHabitHistory()
    }

    suspend fun deleteOldestGroupHabitHistory() {

        habitHistoryDao.deleteOldestNormalHabitHistory()
    }


    suspend fun deleteOldHabitHistory(thirtyDaysAgo: Long) {
        habitHistoryDao.
        deleteOldHabitHistory(thirtyDaysAgo)
    }


   suspend fun getTop10NormalHabits() : Flow<List<HabitHistory>> {

        return habitHistoryDao.getTop10NormalHabits()
    }


    fun getTop10GroupHabits() : Flow<List<HabitHistory>> {

        return habitHistoryDao.getTop10GroupHabits()
    }

    //alışkanlık ekleme
    suspend fun addGroupsNormal(habitHistory: HabitHistory): Long {
        return habitHistoryDao.historyInsert(habitHistory)
    }

     fun verilericek() : Flow<List<HabitHistory>> {
        return habitHistoryDao.getAllHabitHistory()
    }

}