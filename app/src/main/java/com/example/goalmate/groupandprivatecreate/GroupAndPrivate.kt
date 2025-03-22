package com.example.goalmate.groupandprivatecreate

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.HabitHistory
import com.example.goalmate.viewmodel.HabitViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupAndPrivate(
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel()
) {
    var isGroup by remember { mutableStateOf(true) }
    var showGroupHabits by remember { mutableStateOf(false) }
    var showNormalHabits by remember { mutableStateOf(true) }

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
                .background(color = colorResource(R.color.arkaplan))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 48.dp, bottom = 16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Group Card
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = colorResource(R.color.arkaplan)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clickable { 
                                    isGroup = true
                                    navController.navigate("GroupsAdd")
                                },
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp),
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Private Habit Card
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = colorResource(R.color.arkaplan)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clickable { 
                                    isGroup = false
                                    navController.navigate("AddHabitScreen?isGroup=$isGroup")
                                },
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp),
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // History Section
                item {
                    if (!habitHistory.isEmpty() || !groupHabitHistory.isEmpty()) {
                        // Filter Chips
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Geçmiş Alışkanlıklar",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.yazirengi),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                FilterChip(
                                    selected = showGroupHabits,
                                    onClick = {
                                        showGroupHabits = true
                                        showNormalHabits = false
                                        habitViewModel.getGroupHabitHistory()
                                    },
                                    label = { 
                                        Text(
                                            "Grup Geçmişi",
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = colorResource(R.color.arkaplan),
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        labelColor = colorResource(R.color.yazirengi),
                                        selectedLabelColor = colorResource(R.color.beyaz)
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                FilterChip(
                                    selected = showNormalHabits,
                                    onClick = {
                                        showNormalHabits = true
                                        showGroupHabits = false
                                        habitViewModel.getNormalHabitHistory()
                                    },
                                    label = { 
                                        Text(
                                            "Kişisel Geçmiş",
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = colorResource(R.color.arkaplan),
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        labelColor = colorResource(R.color.yazirengi),
                                        selectedLabelColor = colorResource(R.color.beyaz)
                                    )
                                )
                            }
                        }
                    }
                }

                // History Content
                if (showGroupHabits && groupHabitHistory.isNotEmpty()) {
                    items(groupHabitHistory) { history ->
                        HabitHistoryItem(habitHistory = history)
                    }
                }

                if (showNormalHabits && habitHistory.isNotEmpty()) {
                    items(habitHistory) { history ->
                        HabitHistoryItem(habitHistory = history)
                    }
                }

                // Empty State
                if (habitHistory.isEmpty() && groupHabitHistory.isEmpty() || 
                    (showGroupHabits && groupHabitHistory.isEmpty()) || 
                    (showNormalHabits && habitHistory.isEmpty())) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 65.dp , end = 50.dp , bottom = 50.dp , start = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (showGroupHabits) R.drawable.group else R.drawable.avatar
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 16.dp),
                                tint = colorResource(R.color.yazirengiacik)
                            )
                            Text(
                                text = if (showGroupHabits) 
                                    "Henüz grup alışkanlık geçmişiniz bulunmuyor" 
                                else 
                                    "Henüz kişisel alışkanlık geçmişiniz bulunmuyor",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.yazirengi),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorResource(R.color.arkaplan)
        ),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habitHistory.habitName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.yazirengi),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tamamlanan: ${habitHistory.daysCompleted} / ${getFrequency(habitHistory.frequency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.yazirengiacik)
                )
            }

            Text(
                text = "Başlangıç: ${convertDate(habitHistory.startDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.yazirengiacik),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun getFrequency(frequency: String): Int {
    return when(frequency) {
        "Günlük" -> 1
        "Haftalık" -> 7
        "Aylık" -> 30
        else -> 1
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertDate(epochMillis: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(epochMillis)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: Exception) {
        "Geçersiz tarih"
    }
}





