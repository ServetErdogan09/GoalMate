package com.example.goalmate.data.localdata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.goalmate.extrensions.RequestStatus
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

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
    val isExpired: Boolean = false,
    var firestoreId: String? = null // Firestore belge ID'si
)

@Entity(tableName = "habit_history")
data class HabitHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
     val habitName: String, // Alışkanlık adı
     val startDate: Long,  // Başlangıç tarihi
     val frequency: String, // Günlük, Haftalık, Aylık
     val daysCompleted: Int,              // Tamamlanan gün sayısı
     val habitType: String               // Alışkanlık türü (Grup ya da Normal)

)

@Entity(tableName = "badges")
data class Badges(
    @PrimaryKey val id: String,
    val iconId: String? = null,
    val ad : String,
    val description: String,
    val isCompleted: Boolean,
    val category: String // GROUP_COMPLETION, LIMIT_INCREASE, ADMIN, GENERAL_ACHIEVEMENT, APP_USAGE
)


data class ChatMessage(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long,
    val isCurrentUser: Boolean
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
    val maxAllowedGroups :Int = 3,
    val totalPoints: Int = 0
)


data class HabitFirebase(
    val name: String,
    val iconResId: Int? = null,  // Simge kaynağı,
     val frequency: String,  // Sıklık (Günlük, Haftalık, Aylık)
    val colorResId: Int? = null,  // Renk kaynağı
    val habitId : Int
)

@IgnoreExtraProperties
data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val category: String = "",
    val frequency: String = "",
    val isPrivate: Boolean = false,
    val participationType: String = "",
    val muxParticipationCount: Int = 0,
    val minParticipationCount: Int = 0, // Minimum katılımcı sayısı
    val groupStartTime: String = "",
    val description: String = "",
    val createdAt: Long = 0,
    val habitDuration: String = "",
    val createdBy: String = "",
    val quote: String = "",
    val groupCode: String = "",
    val members: List<String> = emptyList(),
    val groupStatus: String = "WAITING", // WAITING, ACTIVE, CLOSED
    val startDeadline: Long = 0, // Grubun başlaması gereken son tarih
    val actualStartDate: Long? = null ,// Grubun gerçekte başladığı tarih,
    val groupCompletedDays : Int = 0

)

// Her gurup için ayrı bir users koleksiyonun içinde tutulacak alt koleksiyon
data class GroupHabits(
    val habitName : String = "",
    val completedDays : Int = 0,
    val uncompletedDays : Int = 0,
    val completedTime : Long = 0L,
    val frequency : String = "günlük",
    val wasCompletedToday : Boolean = false
)




// Her kullanıcı için ayrı bir users koleksiyonun içinde tutulacak alt koleksiyon
data class GroupHabitStats(
  val dailyGroupsCompleted : Int = 0,
  val weeklyGroupsCompleted : Int = 0,
  val monthlyGroupsCompleted : Int = 0
)


data class GroupCloseVoteState(
    val votingEndTime: Long = 0,
    val yesVotes: Int = 0,
    val noVotes: Int = 0,
    val totalMembers: Int = 0,
    val hasUserVoted: Boolean = false,
    val canAdminInitiateVote: Boolean = true,
    val startTime: Long = 0
)

// her kullanıcı tammaladığı rozetlerin id si tutlacak
data class BadgesId(
    val badgesId: Int = 0
)

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