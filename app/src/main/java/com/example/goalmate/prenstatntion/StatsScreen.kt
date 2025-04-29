package com.example.goalmate.prenstatntion

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Timeline
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.GroupHabitStats
import com.example.goalmate.data.localdata.GroupHabits
import com.example.goalmate.prenstatntion.AnalysisScreen.totalHabit
import com.example.goalmate.viewmodel.HabitStatsViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(
    navController: NavController,
    habitStatsViewModel: HabitStatsViewModel
) {
    val habitStats = habitStatsViewModel.statsState.collectAsState().value
    val overallStats = habitStatsViewModel.overallStats.collectAsState().value
    var selectedHabit by remember { mutableStateOf<GroupHabits?>(null) }
    var showDetailedStats by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        habitStatsViewModel.getGroupHabitsStats()
        habitStatsViewModel.getOverallStats()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.arkaplan))
    ) {
        AnimatedVisibility(
            visible = !showDetailedStats,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            MainStatsContent(
                habitStats = habitStats,
                overallStats = overallStats,
                onHabitClick = { 
                    selectedHabit = it
                    showDetailedStats = true
                },
                navController = navController
            )
        }

        AnimatedVisibility(
            visible = showDetailedStats,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            DetailedStatsContent(
                habit = selectedHabit!!,
                onBackClick = { showDetailedStats = false },
                frequency = selectedHabit!!.frequency
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainStatsContent(
    habitStats: List<GroupHabits>,
    overallStats: GroupHabitStats,
    onHabitClick: (GroupHabits) -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Başlık ve Geri Tuşu
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = colorResource(id = R.color.yazirengi)
                    )
                }
                
                Text(
                    text = "İstatistiklerim",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorResource(id = R.color.yazirengi),
                    fontWeight = FontWeight.Bold
                )
                
                // Dengeleme için boş spacer
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Ana İstatistik Kartı
        item {
            MainStatsCard(
                dailyCount = overallStats.dailyGroupsCompleted,
                weeklyCount = overallStats.weeklyGroupsCompleted,
                monthlyCount = overallStats.monthlyGroupsCompleted,
                habitStats = habitStats
            )
        }

        // Alışkanlıklar Listesi
        item {
            Text(
                text = "Alışkanlıklarım",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = R.color.yazirengi),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(habitStats) { habit ->
            HabitCard(
                habit = habit,
                onClick = { onHabitClick(habit) },
                frequency = habit.frequency
            )
        }
    }
}

@Composable
fun MainStatsCard(
    dailyCount: Int,
    weeklyCount: Int,
    monthlyCount: Int,
    habitStats: List<GroupHabits>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.beyaz)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Alışkanlık Dağılımı",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = R.color.yazirengi),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Custom daire gösterimi
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                ) {
                    // Arka plan daireleri
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = 0.66f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = 0.33f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF9C27B0).copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )

                    // Gerçek değer daireleri
                    val total = (dailyCount + weeklyCount + monthlyCount).toFloat().coerceAtLeast(1f)
                    CircularProgressIndicator(
                        progress = dailyCount / total,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF4CAF50),
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = weeklyCount / total,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF2196F3),
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = monthlyCount / total,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF9C27B0),
                        strokeWidth = 12.dp
                    )

                    // Merkezdeki toplam sayı
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.beyaz).copy(alpha = 0.9f))
                            .padding(8.dp),
                    ) {
                        var startAnimation by remember { mutableStateOf(false) }
                        val animatedValue by animateIntAsState(
                            targetValue = if (startAnimation) (dailyCount + weeklyCount + monthlyCount) else 0,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing)
                        )

                        LaunchedEffect(Unit) {
                            startAnimation = true
                        }

                        Text(
                            text = animatedValue.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = colorResource(id = R.color.kutubordrengi),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toplam",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(id = R.color.yazirengi)
                        )
                    }
                }
            }

            // Açıklama
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Günlük", dailyCount, Color(0xFF4CAF50))
                LegendItem("Haftalık", weeklyCount, Color(0xFF2196F3))
                LegendItem("Aylık", monthlyCount, Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    value: Int,
    color: Color
) {
    var startAnimation by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (startAnimation) value else 0,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.yazirengi)
            )
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitCard(
    habit: GroupHabits,
    onClick: () -> Unit,
    frequency: String
) {
    val progress = habit.completedDays.toFloat() / totalHabit(frequency)
    val progressColor = getProgressColor(progress)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.beyaz)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.habitName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(id = R.color.yazirengi),
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${habit.completedDays} gün tamamlandı",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f)
                )
            }

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(40.dp),
                color = progressColor,
                strokeWidth = 4.dp,
                trackColor = progressColor.copy(alpha = 0.2f),
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailedStatsContent(
    habit: GroupHabits,
    onBackClick: () -> Unit,
    frequency: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Üst Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = colorResource(id = R.color.yazirengi)
                )
            }
            
            Text(
                text = habit.habitName,
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(id = R.color.yazirengi),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // İlerleme Kartı
        DetailProgressCard(habit, frequency = frequency)

        Spacer(modifier = Modifier.height(16.dp))

        // İstatistik Kartları
        StatisticsGrid(habit, frequency)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailProgressCard(habit: GroupHabits, frequency: String) {
    val progress = habit.completedDays.toFloat() / totalHabit(frequency)
    val progressColor = getProgressColor(progress)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.beyaz)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor,
                    strokeWidth = 8.dp,
                    trackColor = progressColor.copy(alpha = 0.2f),
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tamamlandı",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(id = R.color.yazirengi)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn("Tamamlanan", habit.completedDays.toString(), "Gün")
                StatColumn("Kalan", (totalHabit(habit.frequency) - habit.completedDays).toString(), "Gün")
                StatColumn("Hedef", totalHabit(habit.frequency).toString(), "Gün")
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = colorResource(id = R.color.yazirengi),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsGrid(habit: GroupHabits, frequency: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Başarı Oranı Kartı
        StatCard(
            title = "Başarı Oranı",
            value = "${((habit.completedDays.toFloat() / totalHabit(frequency)) * 100).toInt()}%",
            icon = Icons.Default.Star,
            color = getProgressColor(habit.completedDays.toFloat() / totalHabit(frequency))
        )

        // Ortalama Tamamlama Kartı
        StatCard(
            title = "Günlük Ortalama",
            value = "${habit.completedDays}/${totalHabit(frequency)}",
            icon = Icons.Default.DateRange,
            color = colorResource(id = R.color.kutubordrengi)
        )

        // Kalan Gün Kartı
        StatCard(
            title = "Kalan Gün",
            value = "${totalHabit(frequency) - habit.completedDays}",
            icon = Icons.Default.DateRange,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.beyaz)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(id = R.color.yazirengi)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getProgressColor(progress: Float): Color {
    return when {
        progress < 0.4f -> Color(0xFFE57373) // Kırmızı
        progress < 0.7f -> Color(0xFFFFB74D) // Sarı
        else -> Color(0xFF81C784) // Yeşil
    }
}