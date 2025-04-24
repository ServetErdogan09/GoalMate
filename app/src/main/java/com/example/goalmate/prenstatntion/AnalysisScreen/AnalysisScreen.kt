package com.example.goalmate.prenstatntion.AnalysisScreen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Duration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import com.example.goalmate.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.goalmate.data.localdata.Habit
import com.example.goalmate.viewmodel.CompleteDayViewModel
import com.example.goalmate.viewmodel.HabitViewModel
import com.example.goalmate.viewmodel.StarCoinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalysisScreen(
    habitViewModel: HabitViewModel,
    navController: NavController,
    habitId: Int,
    completedDayViewModel: CompleteDayViewModel,
    starCoinViewModel : StarCoinViewModel,

    ) {
    val habit by habitViewModel.habit.collectAsState()
    val totalStar by starCoinViewModel.starPoints.collectAsState()






    LaunchedEffect(Unit) {
        completedDayViewModel.getCompleteDays(habitId)
        starCoinViewModel.getStarPoints(habitId)
        habitViewModel.getHabitById(habitId)
    }



    val targetDay = habitViewModel.habitRemainingDaysMap.collectAsState().value





    // İlerleme yüzdesi
    val progress = habit?.let {
        (it.completedDays.toFloat() / totalHabit(it.frequency)).coerceIn(0f, 1f)
    } ?: 0f


    // Emoji gösterimi durumu
    var showEmoji by remember { mutableStateOf(false) }




    // Kalan süre hesaplama
    val remainingMinutes = max(0, Duration.between(LocalTime.now(), LocalTime.of(23, 59)).toMinutes())
    val formattedTime = String.format("%02d:%02d", remainingMinutes / 60, remainingMinutes % 60)



    val animatedProgress = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
            .background(color = colorResource(R.color.arkaplan))
    ) {
        // Header
        HeaderComponent(
            onBackClick = { navController.popBackStack() },
            onEditClick = { navController.navigate("")  },
            onDeleteClick = {habitViewModel.deleteHabit(habit!!)},
            habit = habit,
            navController,
            completedDayViewModel,
            targetDay = targetDay[habitId]?:0
        )



        // Detay Ekranı
        DetailScreenDesign(
            habit = habit,
            progress = progress,
            showEmoji = showEmoji,
            onEmojiClick = { showEmoji = !showEmoji },
            formattedTime = if ((targetDay[habitId] ?: 0) <= 0) "--" else formattedTime,
            totalStar = totalStar,
            animatedProgress
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailScreenDesign(
    habit: Habit?,
    progress: Float,
    showEmoji: Boolean,
    onEmojiClick: () -> Unit,
    formattedTime: String,
    totalStar: Int,
    animatedProgress: Animatable<Float, *>
) {
    habit?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Alışkanlık Başlığı ve Bilgileri
            HabitInfoComponent(
                habitName = it.name,
                startDate = it.startDate,
                finishDate = it.finishDate,
                habit = it
            )

            // Ana İstatistik Kartı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, colorResource(R.color.kutubordrengi), RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // İlerleme Göstergesi
                    ProgressWaveComponent(
                        progress = progress,
                        showEmoji = showEmoji,
                        onEmojiClick = onEmojiClick,
                        animatedProgress = animatedProgress
                    )

                    // Motivasyon Mesajı
                    Text(
                        text = when {
                            progress > 0.75 -> "Harika gidiyorsun! 🌟"
                            progress > 0.50 -> "Yarıyı geçtin, devam et! 💪"
                            progress > 0.25 -> "İyi başlangıç! 🌱"
                            else -> "Haydi başlayalım! 🎯"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.yazirengi),
                        fontFamily = FontFamily(Font(R.font.kalin_bold))
                    )
                }
            }

            // İstatistik Kartları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    modifier = Modifier.weight(1f),
                    title = "Tamamlanan",
                    value = "${habit.completedDays}/${totalHabit(habit.frequency)}",
                    icon = R.drawable.taget2,
                    backgroundColor = colorResource(R.color.suicmerengi)
                )
                
                StatisticCard(
                    modifier = Modifier.weight(1f),
                    title = "Kalan Süre",
                    value = formattedTime,
                    icon = R.drawable.baseline_access_time_24,
                    backgroundColor = colorResource(R.color.calismarengi)
                )
            }

            // Detay Kartı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colorResource(R.color.kutubordrengi), RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailRow(
                        title = "Sıklık",
                        value = habit.frequency,
                        icon = R.drawable.baseline_access_time_24
                    )
                    DetailRow(
                        title = "Tür",
                        value = habit.habitType,
                        icon = R.drawable.taget2
                    )
                    DetailRow(
                        title = "Günlük Süre",
                        value = habit.time,
                        icon = R.drawable.baseline_access_time_24
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressWaveComponent(
    progress: Float,
    showEmoji: Boolean,
    onEmojiClick: () -> Unit,
    animatedProgress: Animatable<Float, *>
) {
    var emoji by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
        emoji = showEmoji(progress)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colorResource(R.color.arkaplan))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İlerleme Yüzdesi
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${(animatedProgress.value * 100).toInt()}%",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.yazirengi)
                )
                Text(
                    text = "tamamlandı",
                    fontSize = 14.sp,
                    color = colorResource(R.color.yazirengi).copy(alpha = 0.7f)
                )
            }

            // Emoji
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.kutubordrengi).copy(alpha = 0.1f))
                    .clickable { onEmojiClick() },
                contentAlignment = Alignment.Center
            ) {
                if (showEmoji) {
                    Image(
                        painter = painterResource(emoji ?: R.drawable.sad),
                        contentDescription = "Mood Emoji",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.taget2),
                        contentDescription = "",
                        tint = colorResource(R.color.kutubordrengi),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // İlerleme Çubuğu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorResource(R.color.yazirengi).copy(alpha = 0.1f))
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(4.dp)
                    .background(colorResource(R.color.yeşil))
            )
        }
    }
}

@Composable
fun StatisticCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: Int,
    backgroundColor: Color
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, backgroundColor, RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = backgroundColor
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = colorResource(R.color.yazirengi)
                )
            }
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.yazirengi)
            )
        }
    }
}



@Composable
fun DetailRow(
    title: String,
    value: String,
    icon: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "",
                tint = colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = colorResource(R.color.yazirengi).copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.yazirengi)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitInfoComponent(
    habitName: String,
    startDate: Long,
    finishDate: Long,
    habit: Habit?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = habitName,
                fontSize = 24.sp,
                color = colorResource(R.color.yazirengi),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.kalin_bold))
            )

            // Zorluk seviyesi rozeti
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getDifficultyColor(habit?.frequency))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(getDifficultyIcon(habit?.frequency)),
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = getDifficultyText(habit?.frequency),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_access_time_24),
                contentDescription = "",
                tint = colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${convertMillisToLocalDate(startDate)} - ${convertMillisToLocalDate(finishDate)}",
                fontSize = 12.sp,
                color = colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                fontFamily = FontFamily(Font(R.font.noto_regular))
            )
        }
    }
}

// Zorluk seviyesi yardımcı fonksiyonları
@Composable
private fun getDifficultyColor(frequency: String?): Color {
    return when (frequency) {
        "Günlük" -> colorResource(R.color.yesil2)
        "Haftalık" -> colorResource(R.color.suicmerengi)
        "Aylık" -> colorResource(R.color.pastelkirmizi)
        else -> colorResource(R.color.kutubordrengi)
    }
}

private fun getDifficultyText(frequency: String?): String {
    return when (frequency) {
        "Günlük" -> "Kolay"
        "Haftalık" -> "Orta"
        "Aylık" -> "Zor"
        else -> "Normal"
    }
}

private fun getDifficultyIcon(frequency: String?): Int {
    return when (frequency) {
        "Günlük" -> R.drawable.taget2  // Zor seviye ikonu
        "Haftalık" -> R.drawable.taget2 // Orta seviye ikonu
        "Aylık" -> R.drawable.taget2    // Kolay seviye ikonu
        else -> R.drawable.taget2       // Varsayılan ikon
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderComponent(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    habit: Habit?,
    navController: NavController,
    completedDayViewModel: CompleteDayViewModel,
    targetDay: Long
) {
    val showDialog = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geri Butonu
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "Geri Tuşu",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onBackClick() }

            )

            // Düzenle ve Sil Butonları
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Düzenle",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            habit?.let {
                                navController.navigate("AddHabitScreen?isGroup=${it.habitType == "group"}&habitId=${it.id}")
                            }
                        }

                )
                Image(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Sil",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            showDialog.value = true
                        }

                )
            }
        }
    }

    if (showDialog.value) {
        val kalanGun = remainingDays(habit!!.completedDays, totalHabit(habit.frequency))
        CustomAlertDialog(
            onDismiss = { showDialog.value = false },
            onConfirm = {
                showDialog.value = false
                onDeleteClick()
            },
            kalanGun = kalanGun,
            onBackClick,
            completedDayViewModel = completedDayViewModel,
            habit = habit,
            targetDay = targetDay
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun totalHabit(isFrequency: String): Int {
    return when (isFrequency) {
        "Günlük" -> 1
        "Haftalık" -> 7
        "Aylık" -> 30
        else -> 1
    }
}





// milisaniyeden taihe çevirme
@RequiresApi(Build.VERSION_CODES.O)
fun convertMillisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault()) // Cihazın zaman dilimini kullan
        .toLocalDate()
}



fun remainingDays(completedDay : Int ,  isFrequency: Int): Int {
    return isFrequency - completedDay
}




@Composable
fun CustomAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    kalanGun: Int,
    onBackClick: () -> Unit,
    completedDayViewModel: CompleteDayViewModel,
    habit: Habit?,
    targetDay: Long
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, colorResource(R.color.kutubordrengi), RoundedCornerShape(24.dp))
            .background(Color.White),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Sil",
                        modifier = Modifier.size(30.dp),
                        tint = colorResource(R.color.pastelkirmizi)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Alışkanlığı Sil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.yazirengi)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (targetDay <= 0) {
                        "Süreniz dolmuş, tamamladınız. Silmek istediğine emin misin?"
                    } else {
                        if (kalanGun > 0) {
                            "Alışkanlığın bitmesine sadece $kalanGun gün kaldı. Silmek istediğine emin misin?"
                        } else {
                            "Harika! Bu alışkanlığı tamamladın. Silmek istediğine emin misin?"
                        }
                    },
                    fontSize = 16.sp,
                    color = colorResource(R.color.yazirengi).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Text(
                    text = "Bu işlem geri alınamaz",
                    fontSize = 14.sp,
                    color = colorResource(R.color.pastelkirmizi),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Vazgeç",
                        color = colorResource(R.color.kutubordrengi),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = {
                        onConfirm()
                        completedDayViewModel.deleteHabit(habit!!.id)
                        onBackClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pastelkirmizi)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Sil",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {}
    )
}

fun showEmoji(progress: Float): Int = when {
    progress > 0.75 -> R.drawable.veryhappy
    progress > 0.50 -> R.drawable.happy
    progress > 0.25 -> R.drawable.sad2
    else -> R.drawable.sad
}

