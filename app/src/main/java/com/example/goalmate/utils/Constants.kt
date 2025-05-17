package com.example.goalmate.utils

import com.example.goalmate.R
import com.example.goalmate.prenstatntion.AchievementCard.RankInfo

object Constants {
    // Bütün alışkanlıklar için sabit olan değerler her hangi tabloda tutulmasına gerek yok
    const val MAX_HABIT_COUNT = 20
    const val dailyPoints: Int = 10  // Günlük puan
    const val weeklyBonusPoints: Int = 40  // Haftalık bonus puan
    const val monthlyBonusPoints: Int = 100  // Aylık bonus puan

    private val rankRanges = listOf(
        Triple("Acemi", 0, 99),
        Triple("Başlangıç", 100, 299),
        Triple("Çaylak", 300, 599),
        Triple("Disiplinli", 600, 999),
        Triple("Kararlı", 1000, 1599),
        Triple("Usta", 1600, 2299),
        Triple("Bilge", 2300, 3199),
        Triple("Efsane", 3200, Int.MAX_VALUE)
    )

    fun getRankFromPoints(points: Int): String {
        return rankRanges.first { (_, min, max) -> points in min..max }.first
    }

    fun getRankIcon(rank: String): Int {
        return when (rank) {
            "Acemi" -> R.drawable.rank_1_acemi_icon
            "Başlangıç" -> R.drawable.rank_2_baslangic_icon
            "Çaylak" -> R.drawable.rank_3_caylak_icon
            "Disiplinli" -> R.drawable.rank_4_disiplinli_icon
            "Kararlı" -> R.drawable.rank_5_kararli_icon
            "Usta" -> R.drawable.rank_6_usta_icon
            "Bilge" -> R.drawable.rank_7_bilge_icon
            "Efsane" -> R.drawable.efsane_icon
            else -> R.drawable.efsane_icon
        }
    }

    fun getAllRanks(): List<RankInfo> {
        return rankRanges.map { (name, min, max) ->
            RankInfo(
                name = name,
                minPoints = min,
                maxPoints = max,
                iconRes = getRankIcon(name)
            )
        }
    }
}