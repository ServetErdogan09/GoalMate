package com.example.goalmate.data.repository

import com.example.goalmate.data.localdata.CompletedDay
import com.example.goalmate.data.localdata.CompletedDayDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CompleteDayDaoRepository @Inject constructor(private val completedDayDao: CompletedDayDao){

  suspend   fun insert(completedDay : CompletedDay){
        completedDayDao.insert(completedDay)
    }



   suspend  fun deleteHabit(habitId: Int){
        completedDayDao.deleteHabit(habitId)
    }


     fun getCompletedDays(habitId:Int ) :Flow<List<CompletedDay>>{
        return completedDayDao.getCompletedDays(habitId)
    }

    suspend fun updateCompletedDays(habitId: Int, date: Long, completed: Boolean ){
        completedDayDao.updateCompletedDay(habitId, date, completed)
    }

   suspend  fun getCompletedDayByDate(habitId: Int, date: Long): CompletedDay? {
        return completedDayDao.getCompletedDayByDate(habitId, date)
    }

}