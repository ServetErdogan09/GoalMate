package com.example.goalmate.utils

object Constants {
    // Bütün alışkanlıklar için sabit olan değerler her hangi tabloda tutulmasına gerek yok
    const val MAX_HABIT_COUNT = 20
    const val dailyPoints: Int = 10  // Günlük puan
    const val weeklyBonusPoints: Int = 40  // Haftalık bonus puan
    const val monthlyBonusPoints: Int = 100  // Aylık bonus puan


    fun getRankFromPoints(points: Int): String {
        return when (points) {
            in 0..199 -> "Acemi"
            in 200..499 -> "Başlangıç"
            in 500..799 -> "Çaylak"
            in 800..1199 -> "Disiplinli"
            in 1200..1799 -> "Kararlı"
            in 1800..2499 -> "Usta"
            in 2500..3499 -> "Bilge"
            else -> "Efsane"
        }
    }

    }