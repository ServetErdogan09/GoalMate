package com.example.goalmate.data.localdata


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Habit::class , HabitHistory::class , UserPoints ::class , HabitsPoints :: class , CompletedDay::class , Badges::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): DaoHabits
    abstract fun habitHistoryDao(): HabitHistoryDao
    abstract fun userPointsCoinDao(): UserPointsCoinDao
    abstract fun completedDayDao(): CompletedDayDao
    abstract fun badgesDao(): BadgesDao

}