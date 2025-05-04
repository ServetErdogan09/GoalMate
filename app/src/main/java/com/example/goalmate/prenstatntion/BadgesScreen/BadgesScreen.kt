package com.example.goalmate.prenstatntion.BadgesScreen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.Badges
import com.example.goalmate.viewmodel.BadgesViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    navController: NavController,
    badgesViewModel: BadgesViewModel,
    context: Context
) {
    val badges by badgesViewModel.badges.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tümü", "Grup", "Limit", "Yönetici", "Kullanım")

    // İlk kez JSON'dan rozet yükleme
    LaunchedEffect(Unit) {
        if (badges.isEmpty()) {
            try {
                Log.d("BadgesScreen", "JSON'dan rozetler yükleniyor")
                val inputStream = context.resources.openRawResource(R.raw.badges)
                val reader = InputStreamReader(inputStream)
                val badgeType = object : TypeToken<List<Badges>>() {}.type
                val jsonBadges = Gson().fromJson<List<Badges>>(reader, badgeType)
                
                if (jsonBadges != null) {
                    badgesViewModel.addListBadges(jsonBadges)
                }
            } catch (e: Exception) {
                Log.e("BadgesScreen", "JSON okuma hatası", e)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.beyaz))
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Rozetlerim",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.yazirengi)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rozetleri kazanmak için görevleri tamamla",
                    fontSize = 14.sp,
                    color = colorResource(R.color.yazirengi).copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = colorResource(R.color.kutubordrengi),
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = colorResource(R.color.kutubordrengi)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (badges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorResource(R.color.beyaz)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val filteredBadges = when (selectedTab) {
                    0 -> badges // Tümü
                    1 -> badges.filter { it.category == "GROUP_COMPLETION" } // Grup
                    2 -> badges.filter { it.category == "LIMIT_INCREASE" } // Limit
                    3 -> badges.filter { it.category == "ADMIN" } // Yönetici
                    4 -> badges.filter { it.category == "APP_USAGE" } // Kullanım
                    else -> badges
                }



                items(filteredBadges.chunked(2)) { badgePair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        badgePair.forEach { badge ->
                            BadgeCard(badge)
                        }
                        if (badgePair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: Badges) {
    val imageRes = when (badge.iconId) {
        "rozet1" -> R.drawable.rozet1
        "rozet2" -> R.drawable.rozet2
        "rozet3" -> R.drawable.rozet3
        "rozet4" -> R.drawable.rozet4
        "rozet5" -> R.drawable.rozet5
        "rozet6" -> R.drawable.rozet6
        "rozet7" -> R.drawable.rozet7
        "rozet8" -> R.drawable.rozet8
        "rozet9" -> R.drawable.rozet9
        "rozet10" -> R.drawable.rozet10
        "rozet11" -> R.drawable.rozet11
        "rozet12" -> R.drawable.rozet12
        "rozet13" -> R.drawable.rozet13
        "rozet14" -> R.drawable.rozet14
        "rozet15" -> R.drawable.rozet15
        "rozet16" -> R.drawable.rozet16
        "rozet17" -> R.drawable.rozet17
        else -> R.drawable.rozet1
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = badge.description,
                    colorFilter = if (!badge.isCompleted) 
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    else 
                        null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = badge.ad,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isCompleted)
                    colorResource(R.color.kutubordrengi)
                else
                    colorResource(R.color.yazirengi).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = badge.description,
                fontSize = 13.sp,
                color = colorResource(R.color.yazirengi).copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.height(40.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (badge.isCompleted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.tamamlandi),
                        contentDescription = "Tamamlandı",
                        tint = colorResource(R.color.kutubordrengi),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tamamlandı",
                        fontSize = 13.sp,
                        color = colorResource(R.color.kutubordrengi)
                    )
                }
            }
        }
    }
} 