package com.example.goalmate.groupandprivatecreate

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yeniproje.R
import com.example.goalmate.data.localdata.HabitHistory
import com.example.goalmate.viewmodel.HabitViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

    @OptIn(ExperimentalMaterialApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun GroupAndPrivate(
        navController: NavController,
        habitViewModel: HabitViewModel = viewModel()
    ) {
        var isGroup by remember { mutableStateOf(true) }
        var showGroupHabits by remember { mutableStateOf(false) } // Grup alışkanlıklarını gösterme durumu
        var showNormalHabits by remember { mutableStateOf(true) } // Normal alışkanlıkları gösterme durumu

        val habitHistory by habitViewModel.habitHistory.collectAsState()
        val groupHabitHistory by habitViewModel.groupHabitHistory.collectAsState()

        LaunchedEffect(Unit) {
            habitViewModel.deleteHistoryHabits()
            habitViewModel.getGroupHabitHistory()
            habitViewModel.getNormalHabitHistory()
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.arkaplan))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 25.dp)
                    .background(color = colorResource(R.color.arkaplan))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(color = colorResource(R.color.arkaplan)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.arkaplan)),
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = colorResource(id = R.color.yazirengi),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isGroup = true
                                navController.navigate("AddExerciseAddScreen?isGroup=$isGroup")
                                Log.e("Navigation", "Navigating to AddExerciseAddScreen with isGroup=$isGroup")},
                        elevation = CardDefaults.cardElevation(4.dp)

                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.group),
                                contentDescription = "Grup",
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Grup Ekle",
                                color = colorResource(R.color.yazirengi),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.arkaplan)),
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = colorResource(id = R.color.yazirengi),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isGroup = false
                                navController.navigate("AddExerciseAddScreen?isGroup=$isGroup"
                                )                        },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.avatar),
                                contentDescription = "Özel",
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Alışkanlık Ekle",
                                color = colorResource(R.color.yazirengi),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (habitHistory.isEmpty() && groupHabitHistory.isEmpty()){

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Geçmiş Alışkanlıkların Burada Bulacaksın",
                            fontSize = 16.sp,
                        )
                        Image(
                            painter = painterResource(id = R.drawable.empty), // Buraya bir boş durum resmi ekleyin
                            contentDescription = "Boş Durum",
                            modifier = Modifier
                                .padding(start = 60.dp)
                                .fillMaxWidth()
                                .height(300.dp) // Yüksekliği istediğiniz gibi ayarlayın
                        )
                    }


                }else{
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                    )
                    {
                        Chip(
                            border = BorderStroke(width = 2.dp , color = colorResource(R.color.yazirengi)),
                            shape = ShapeDefaults.Medium,
                            onClick = {
                                showGroupHabits = true
                                habitViewModel.getGroupHabitHistory()
                                showNormalHabits = false
                            },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (showGroupHabits) colorResource(R.color.kutubordrengi) else colorResource(R.color.arkaplan)
                            )                    ) {
                            Text(
                                text = "Grup Geçmişi",
                                color = if (showGroupHabits) colorResource(R.color.beyaz) else colorResource(R.color.yazirengi)
                            )
                        }

                        Chip(
                            border = BorderStroke(width = 2.dp , color = colorResource(R.color.yazirengi)),
                            shape = ShapeDefaults.Medium,
                            onClick = {
                                showNormalHabits = true
                                habitViewModel.getNormalHabitHistory()
                                showGroupHabits = false
                            },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (showNormalHabits) colorResource(R.color.kutubordrengi) else colorResource(R.color.arkaplan)
                            )
                        ) {
                            Text(
                                text = "Kişisel Geçmiş",
                                color = if (showNormalHabits) colorResource(R.color.beyaz) else colorResource(R.color.yazirengi)
                            )
                        }
                    }

                    if (showGroupHabits) {
                        if (groupHabitHistory.isEmpty()) {
                            Text(
                                text = "Geçmiş grup alışkanlıklarınız yok!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily(Font(R.font.noto_regular)),
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(R.color.yazirengi),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                items(groupHabitHistory) { history ->
                                    HabitHistoryItem(habitHistory = history)
                                }
                            }
                        }
                    }

                    if (showNormalHabits) {
                        if (habitHistory.isEmpty()) {
                            Text(
                                text = "Geçmiş normal alışkanlıklarınız yok!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily(Font(R.font.noto_regular)),
                                color = colorResource(R.color.yazirengi),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(habitHistory) { history ->
                                    HabitHistoryItem(habitHistory = history)
                                }
                            }
                        }
                    }
                }

            }
        }
    }


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitHistoryItem(habitHistory: HabitHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(color = colorResource(R.color.arkaplan))
            .border(
                width = 1.dp,
                color = colorResource(R.color.yazirengi),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habitHistory.habitName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Tamamlanan: ${habitHistory.daysCompleted} / ${getFrequency(habitHistory.frequency)}",
                fontSize = 12.sp
            )
        }

        Column (
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = "Başlangıç: ${convertDate(habitHistory.startDate)}",
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )


        }

    }
}


@Composable
fun getFrequency(frequency: String) : Int {
    val isFrequency = when(frequency){
        "Günlük" -> 1
        "Haftalık" -> 7
        "Aylık" -> 30
        else -> 1
    }
    return isFrequency
}


// Tarih formatlama fonksiyonu
@RequiresApi(Build.VERSION_CODES.O)
fun convertDate(epochMillis: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(epochMillis)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // YYYY-MM-DD formatında
    } catch (e: Exception) {
        "Geçersiz tarih"
    }
}





