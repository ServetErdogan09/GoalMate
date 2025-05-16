package com.example.goalmate.prenstatntion.ScoreBoard


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.goalmate.R
import com.example.goalmate.data.localdata.GroupHabits
import com.example.goalmate.viewmodel.ScoreBoardViewModel
import com.example.goalmate.viewmodel.UserScoreData
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter

@Composable
fun ScoreBoardScreen(
    viewModel: ScoreBoardViewModel,
    groupId: String,
    navController: NavController
) {
    val scoreBoardState by viewModel.scoreBoard.collectAsState()
    
    LaunchedEffect(groupId) {
        viewModel.getUsersScoreBoard(groupId)
    }

    // Kullanıcıları tamamlanma oranına göre sırala
    val sortedUsers = remember(scoreBoardState.userScores) {
        scoreBoardState.userScores.map { user ->
            val totalDays = user.habitData.completedDays + user.habitData.uncompletedDays
            val completionRate = if (totalDays > 0) {
                (user.habitData.completedDays.toFloat() / totalDays * 100).toInt()
            } else 0
            user to completionRate
        }.sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
            ) {
                // Top bar with back button and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Geri",
                            tint = colorResource(R.color.yazirengi)
                        )
                    }

                    // Title
                    Text(
                        text = scoreBoardState.groupName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.noto_regular))
                        ),
                        color = colorResource(id = R.color.yazirengi)
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.arkaplan))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Skor Tablosu",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorResource(id = R.color.kutubordrengi),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${scoreBoardState.userScores.size} Üye",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.kutubordrengi)
                        )
                    }
                }
            }

            itemsIndexed(sortedUsers) { index, (userScore, _) ->
                ScoreBoardItem(userScore = userScore, rank = index + 1 , navController = navController , userId =  userScore.userId)
            }
        }
    }
}

@Composable
fun ScoreBoardItem(userScore: UserScoreData, rank: Int , navController: NavController , userId : String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{
                navController.navigate("ViewProfile/$userId")
            }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700) // Altın
                            2 -> Color(0xFFC0C0C0) // Gümüş
                            3 -> Color(0xFFCD7F32) // Bronz
                            else -> colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = when (rank) {
                        1, 2, 3 -> Color.White
                        else -> colorResource(id = R.color.kutubordrengi)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }


          val painter = rememberAsyncImagePainter(
              model = when{
                  userScore.profileImage.startsWith("http") || userScore.profileImage.startsWith("content")->
                      userScore.profileImage
                  else->{
                      getProfilePainter(userScore.profileImage, R.drawable.personel)
                  }
              },
              error = painterResource(R.drawable.bildl),
              placeholder = painterResource(R.drawable.bildl)
          )

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop,
            )




            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userScore.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.yazirengi)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Completed Days
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = colorResource(id = R.color.yesil2),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${userScore.habitData.completedDays}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.yesil2)
                        )
                    }

                    Text(
                        text = "•",
                        color = colorResource(id = R.color.yazirengi).copy(alpha = 0.5f)
                    )

                    // Uncompleted Days
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            tint = colorResource(id = R.color.pastelkirmizi),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${userScore.habitData.uncompletedDays}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.pastelkirmizi)
                        )
                    }
                }
            }

            // Completion Rate
            val completionRate = if (userScore.habitData.completedDays + userScore.habitData.uncompletedDays > 0) {
                (userScore.habitData.completedDays.toFloat() / (userScore.habitData.completedDays + userScore.habitData.uncompletedDays) * 100).toInt()
            } else 0

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        completionRate >= 75 -> colorResource(id = R.color.yesil2)
                        completionRate >= 50 -> colorResource(id = R.color.kutubordrengi)
                        else -> colorResource(id = R.color.pastelkirmizi)
                    }
                ),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$completionRate%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

