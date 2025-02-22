package com.example.yeniproje.data.localdata

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.goalmate.data.localdata.DaoHabits
import com.example.goalmate.data.localdata.Habit

@Database(
    entities = [Habit::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun daoHabits(): DaoHabits
} 