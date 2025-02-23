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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
            onEditClick = {  },
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
    formattedTime: String, // formattedTime parametresi eklendi
    totalStar : Int,
    animatedProgress: Animatable<Float, *>
) {
    habit?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // İlerleme Çemberi
            ProgressCircleComponent(
                progress = progress,
                showEmoji = showEmoji,
                onEmojiClick = onEmojiClick,
                animatedProgress = animatedProgress
            )

            // Alışkanlık Bilgileri
            HabitInfoComponent(
                habitName = it.name,
                startDate = it.startDate,
                finishDate = it.finishDate
            )

            // Bilgi Kutuları
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoBoxComponent(
                    iconResId = R.drawable.taget2,
                    title = "Tamamlandı",
                    value = "${habit.completedDays} / ${totalHabit(habit.frequency)}",
                    backgroundColor = colorResource(R.color.suicmerengi)
                )
                InfoBoxComponent(
                    iconResId = R.drawable.baseline_access_time_24,
                    title = "Bugünkü alışkanlığın bitmesine kalan süre",
                    value = formattedTime,
                    backgroundColor = colorResource(R.color.calismarengi)
                )

                InfoBoxComponent(
                    iconResId = R.drawable.star,
                    title = "Alışkanlıktan kazanılan Yıldız",
                    value = "$totalStar",
                    backgroundColor = colorResource(R.color.yildiz)
                )

                InfoBoxComponent(
                    iconResId = R.drawable.baseline_access_time_24,
                    title = "Günlük alışkanlık süresi",
                    value = habit.time,
                    backgroundColor = colorResource(R.color.dogarengi)
                )

            }

        }
    }
}



@Composable
fun InfoBoxComponent(
    iconResId: Int,
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(
                width = 1.dp,
                color = colorResource(R.color.yazirengi),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.noto_regular))
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = colorResource(R.color.yazirengi),
                    fontFamily = FontFamily(Font(R.font.noto_regular))
                )
            }
        }
    }
}



fun showEmoji(progress: Float): Int = when {
    progress > 0.75 -> R.drawable.veryhappy
    progress > 0.50 -> R.drawable.happy
    progress > 0.25 -> R.drawable.sad2
    else -> R.drawable.sad
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitInfoComponent(
    habitName: String,
    startDate: Long,
    finishDate: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alışkanlık : $habitName",
            fontSize = 20.sp,
            color = colorResource(R.color.yazirengi),
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily(Font(R.font.kalin_bold))
        )
        Text(
            text = "Tarih : ${convertMillisToLocalDate(startDate)} - ${convertMillisToLocalDate(finishDate)}",
            fontSize = 14.sp,
            color = colorResource(R.color.yazirengi),
            fontWeight = FontWeight.W500,
            fontFamily = FontFamily(Font(R.font.noto_regular))
        )
    }
}





@Composable
fun ProgressCircleComponent(
    progress: Float,
    showEmoji: Boolean,
    onEmojiClick: () -> Unit,
    animatedProgress: Animatable<Float, *>
) {

    var emoji  by remember { mutableStateOf<Int?>(null)}



    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )

        emoji = showEmoji(progress)
    }


    Box(
        modifier = Modifier
            .size(160.dp)
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arka Plan Dairesi
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.size(160.dp),
            color = Color.LightGray,
            strokeWidth = 12.dp
        )

        // Animasyonlu İlerleme Dairesi
        CircularProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier.size(180.dp),
            color = colorResource(R.color.yeşil),
            strokeWidth = 12.dp
        )

        // Yüzde veya Emoji Gösterimi
        if (!showEmoji) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier
                    .padding(top = 5.dp, start = 5.dp)
                    .clickable { onEmojiClick() }
            )
        } else {
            if (emoji==null){
                Image(
                    painter = painterResource(R.drawable.sad),
                    contentDescription = "happy",
                    modifier = Modifier.clickable { onEmojiClick() }
                )
            }else{
                Image(
                    painter = painterResource(emoji!!),
                    contentDescription = "happy",
                    modifier = Modifier
                        .padding(top = 5.dp, start = 5.dp)
                        .clickable { onEmojiClick() }
                )
            }

        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.edit),
                contentDescription = "Düzenle",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onEditClick() }
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

    if (showDialog.value) {
            val kalanGun = remainingDays(habit!!.completedDays, totalHabit(habit.frequency))
        Log.e("kalanGun", "kalanGun: $kalanGun")

        CustomAlertDialog(
            onDismiss = {showDialog.value = false} ,
            onConfirm = {showDialog.value = false
            onDeleteClick()} , kalanGun = kalanGun,
            onBackClick,
            completedDayViewModel = completedDayViewModel,
            habit,
            targetDay = targetDay
        )

    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun totalHabit(isFrequency: String): Int {

    val currentMonth = LocalDate.now().month  // Bu ayı alıyoruz
    val daysInMonth = currentMonth.length(LocalDate.now().isLeapYear)  // O ayın gün sayısını alıyoruz

    return when (isFrequency) {
        "Günlük" -> 1
        "Haftalık" -> 7
        "Aylık" -> daysInMonth
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
    targetDay:Long
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)) // Köşeleri yuvarlama
            .background(color = colorResource(R.color.arkaplan)), // Arka plan rengi
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){


                Icon(
                    painter = painterResource(R.drawable.baseline_info_24),
                    contentDescription = "Uyarı",
                    modifier = Modifier.size(50.dp),
                    tint = colorResource(R.color.yazirengi)
                )

                Text(
                    text = "Silme Onayı",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.kutubordrengi)
                )
            }

        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
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
                    color = colorResource(R.color.yazirengi),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    completedDayViewModel.deleteHabit(habit!!.id)
                    onBackClick()
                          },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.pastelkirmizi))
            ) {
                Text("Sil", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss()} ,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.yesil))
                ) {
                Text("İptal", color = Color.White)
            }
        }
    )
}

