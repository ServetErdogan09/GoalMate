package com.example.goalmate.data.localdata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.goalmate.extrensions.RequestStatus
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,  // Alışkanlık kimliği
    val name: String,  // Alışkanlık adı
    val frequency: String,  // Sıklık (Günlük, Haftalık, Aylık)
    val isPrivate: Boolean,  // Özel mi? (true = gizli, false = herkese açık)
    val time: String,  // Süre (örneğin "30 dakika")
    val isCompleted: Boolean = false,  // Tamamlandı mı?
    val startDate: Long,  // Başlangıç tarihi (milisaniye)
    val finishDate: Long,  // Bitiş tarihi (milisaniye)
    val completedDays: Int = 0,  // Tamamlanan gün sayısı
    var lastCompletedDate: Long? = null,  // Son tamamlanma tarihi
    val lastResetDate: Long? = System.currentTimeMillis(),  // Son sıfırlama tarihi
    val iconResId: Int? = null,  // Simge kaynağı
    val habitType: String,  // Alışkanlık türü (Grup/Normal)
    val colorResId: Int? = null,  // Renk kaynağı
    @ColumnInfo(defaultValue = "0")
    val isExpired: Boolean = false
)

@Entity(tableName = "habit_history")
data class HabitHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitName: String,               // Alışkanlık adı
    val startDate: Long,                 // Başlangıç tarihi
    val endDate: Long,                   // Bitiş tarihi
    val frequency: String,               // Günlük, Haftalık, Aylık
    val daysCompleted: Int,              // Tamamlanan gün sayısı
    val habitType: String               // Alışkanlık türü (Grup ya da Normal)

)

@Entity(
    tableName = "habits_points",
    foreignKeys = [ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)]
)
data class HabitsPoints(
    @PrimaryKey(autoGenerate = true) val pointsId: Int = 0,
    val habitId: Int,  // hangi habit alışkanlıktan geldği
    val starPoints: Int = 0  // Alışkanlık başına kazanılan yıldız
)




@Entity(tableName = "user_points")
data class UserPoints(
    @PrimaryKey val userId: Int = 2,  // sabit Id  ile toplam kazanılan yıldızı hesaplayacağız
    val totalStarPoints: Int = 0,  // Kullanıcının toplam yıldız puanı
)

// Tamamlanan Günler  Tablosu
@Entity(
    tableName = "completed_days",
    foreignKeys = [ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)]
)
data class CompletedDay(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int, // Hangi alışkanlığa ait olduğunu gösterir
    val date: Long, // Tamamlanan tarih (milisaniye cinsinden)
    val isCompleted: Boolean = false
)
data class RegistrationData(
    val email: String = "",
    val profileImage: String = "",
    val password: String = "",
    val name: String = "",
    val surname: String = "",
    val gender: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val maxAllowedGroups :Int = 3
)


data class GroupHabit(
    val name : String ,
    val startDate: Long,  // Başlangıç tarihi (milisaniye)
    val finishDate: Long,  // Bitiş tarihi (milisaniye)
    val completedDays: Int = 0,  // Tamamlanan gün sayısı
)


@IgnoreExtraProperties
data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val category: String = "",
    val frequency: String = "",
    val isPrivate: Boolean = false,
    val participationType: String = "",
    val participantNumber: Int = 0,
    val description: String = "",
    val createdAt: Long = 0,
    val habitDuration: String,
    val createdBy: String = "",
    val quote: String = "",
    val members: List<String> = emptyList(),
) {
    constructor() : this("", "", "", "", false, "", 0, "", 0, "", "", "", emptyList())
}


@Entity(
    tableName = "motivation_quotes",
    indices = [Index(value = ["category"])]
)
data class MotivationQuote(
    @PrimaryKey val id: String = "",
    val category: String = "",
    val quote: String = ""
)

data class GroupRequest(
    val id: String,
    val groupId: String,
    val userId: String,
    val senderName: String,
    val senderImage: String?,
    val groupName: String,
    val timestamp: Long,
    val status: RequestStatus = RequestStatus.PENDING,
    val isRead: Boolean = false
)