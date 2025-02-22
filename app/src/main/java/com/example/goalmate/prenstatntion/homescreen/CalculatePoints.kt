package com.example.goalmate.prenstatntion.homescreen

import android.util.Log
import com.example.goalmate.utils.Constants.dailyPoints
import com.example.goalmate.utils.Constants.monthlyBonusPoints
import com.example.goalmate.utils.Constants.weeklyBonusPoints
import com.example.goalmate.data.localdata.Habit


fun calculatePoints(habit: Habit, completedDays: Int): Int {
    val totalDays = ((habit.finishDate - habit.startDate) / (1000 * 60 * 60 * 24)).toInt()
    val currentDate = System.currentTimeMillis()

    val daysPassed = ((currentDate - habit.startDate) / (1000 * 60 * 60 * 24)).toInt()


    var totalPoints = 0

    when (habit.frequency) {
        "Günlük" -> {
            totalPoints = dailyPoints
        }

        "Haftalık" -> {
            val weeksPassed = ((currentDate - habit.startDate) / (1000 * 60 * 60 * 24 * 7)).toInt()
            val fullWeeksCompleted = completedDays / 7
            Log.e("Debug", "habit.startDate: ${habit.startDate}")
            Log.e("Debug", "daysPassed: $daysPassed")
            Log.e("Debug", "weeksPassed: $weeksPassed")
            totalPoints = dailyPoints

            if (fullWeeksCompleted >= weeksPassed) {
                val remainingDays = completedDays % 7
                val bonus = when (remainingDays) {
                    0 -> weeklyBonusPoints
                    6 -> weeklyBonusPoints - 20
                    5 -> weeklyBonusPoints - 10
                    else -> 0
                }
                totalPoints += bonus
            }
        }

        "Aylık" -> {
            val monthsPassed = ((currentDate - habit.startDate) / (1000 * 60 * 60 * 24 * 30)).toInt()
            val fullMonthsCompleted = completedDays / 30

            Log.e("Debug", "monthsPassed: $monthsPassed")

            totalPoints =  dailyPoints

            if (fullMonthsCompleted >= monthsPassed) {
                val remainingDays = completedDays % 30
                val bonus = when (remainingDays) {
                    in 26..30 -> monthlyBonusPoints
                    in 21..25 -> monthlyBonusPoints - 20
                    in 16..20 -> monthlyBonusPoints - 40
                    else -> 0
                }
                totalPoints += bonus
            }
        }
    }

    Log.e("Debug", "completedDays: $completedDays")
    Log.e("Debug", "totalDays: $totalDays")
    Log.e("Debug", "totalPoints (Hesaplanmış): $totalPoints")


    return totalPoints
}