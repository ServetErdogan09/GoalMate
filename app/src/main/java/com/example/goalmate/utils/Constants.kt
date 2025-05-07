package com.example.goalmate.utils

import com.example.goalmate.R


object Constants {
    // Bütün alışkanlıklar için sabit olan değerler her hangi tabloda tutulmasına gerek yok
    const val MAX_HABIT_COUNT = 20
    const val dailyPoints: Int = 10  // Günlük puan
    const val weeklyBonusPoints: Int = 40  // Haftalık bonus puan
    const val monthlyBonusPoints: Int = 100  // Aylık bonus puan


    fun getRankFromPoints(points: Int): String {
        return when (points) {
            in 0..99 -> "Acemi"
            in 100..299 -> "Başlangıç"
            in 300..599 -> "Çaylak"
            in 600..999 -> "Disiplinli"
            in 1000..1599 -> "Kararlı"
            in 1600..2299 -> "Usta"
            in 2300..3199 -> "Bilge"
            else -> "Efsane"
        }
    }

    fun getRankIcon(rank: String): Int {
        return when (rank) {
            "Kararlı" -> R.drawable.acemi
           // "Başlangıç" -> R.drawable.baslangic_icon
          //  "Çaylak" -> R.drawable.caylak_icon
           // "Disiplinli" -> R.drawable.disiplinli_icon
           // "Kararlı" -> R.drawable.kararli_icon
            //"Usta" -> R.drawable.usta_icon
            //"Bilge" -> R.drawable.bilge_icon
            //"Efsane" -> R.drawable.efsane_icon
            else -> R.drawable.acemi
        }
    }
}