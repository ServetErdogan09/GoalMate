package com.example.goalmate.prenstatntion.AchievementCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.utils.Constants
import com.example.goalmate.utils.Constants.getRankFromPoints
import com.example.goalmate.utils.Constants.getRankIcon
import com.example.goalmate.viewmodel.RegisterViewModel

data class RankInfo(
    val name: String,
    val minPoints: Int,
    val maxPoints: Int,
    val iconRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    registerViewModel: RegisterViewModel,
    navController: NavController
) {
    val currentPoints by registerViewModel.userPoints.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Top Bar with Back Button
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Geri",
                        tint = Color(0xFF222222)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF8F8F8)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .padding(bottom = 40.dp)
        ) {
            // Current Achievement Card
            AchievementCard(
                points = currentPoints,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Ranks Header
            Text(
                text = "Rütbeler ve Puan Gereksinimleri",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Ranks List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(Constants.getAllRanks()) { rank ->
                        RankListItem(
                            rank = rank,
                            isCurrentRank = currentPoints in rank.minPoints..rank.maxPoints
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankListItem(rank: RankInfo, isCurrentRank: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrentRank) Color(0xFFE8F5E9) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isCurrentRank) 1.dp else 0.dp,
                color = if (isCurrentRank) Color(0xFF4CAF50) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Icon with Container
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .background(Color(0xFFF5F5F5), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = rank.iconRes),
                contentDescription = rank.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Rank Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rank.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rank.minPoints} - ${if (rank.maxPoints == Int.MAX_VALUE) "∞" else rank.maxPoints}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = " puan",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Current Rank Indicator
        if (isCurrentRank) {
            Text(
                text = "Mevcut",
                fontSize = 12.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        Color(0xFFE8F5E9),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AchievementCard(points: Int, modifier: Modifier = Modifier) {
    val rank = getRankFromPoints(points)
    val iconRes = getRankIcon(rank)

    Column(
        modifier = modifier
            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
            .padding(10.dp)
            .width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // İkon + Platform
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.height(100.dp)
        ) {
            // Platform (sade dairesel zemin)
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 18.dp)
                    .background(Color(0xFFE0E0E0), CircleShape)
            )

            // İkon
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = rank,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rütbe İsmi
        Text(
            text = rank,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Puan
        Text(
            text = "$points Puan",
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}
