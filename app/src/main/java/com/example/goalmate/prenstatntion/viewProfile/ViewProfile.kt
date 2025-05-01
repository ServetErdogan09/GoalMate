package com.example.goalmate.prenstatntion.viewProfile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.R
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.data.localdata.HabitFirebase
import com.example.goalmate.prenstatntion.homescreen.PointColor
import com.example.goalmate.prenstatntion.homescreen.RankBadge
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.example.goalmate.utils.Constants
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.example.goalmate.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ViewProfile(
    userId: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel,
    registerViewModel: RegisterViewModel
) {
    // User data states
    val userNames = groupsAddViewModel.userNames.collectAsState().value
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    val userName = userNames[userId] ?: "Yükleniyor..."
    val profileImage = profileImages[userId] ?: ""

    var userPoint by remember { mutableStateOf(0) }

    // State for user's groups
    var userGroups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var userHabits by remember { mutableStateOf<List<HabitFirebase>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var joinDate by remember { mutableStateOf("01.01.2023") }
    var selectedTab by remember { mutableStateOf(0) }

    // firestoreden verileri çek
    LaunchedEffect(userId) {
        groupsAddViewModel.getUsersName(userId)
        groupsAddViewModel.getProfile(userId)
        
        // Kullanıcının puanını çek
        userPoint = groupsAddViewModel.getUserPoints(userId)

        val db = FirebaseFirestore.getInstance()

        //kullanıcının olduğu gurupları çek
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val joinedGroupIds = userDoc.get("joinedGroups") as? List<String> ?: emptyList()
            val createdAt = userDoc.getLong("createdAt")

            // Format join date
            if (createdAt != null) {
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                joinDate = formatter.format(Date(createdAt))
            }

            // Get group details
            val groupsList = mutableListOf<Group>()
            for (groupId in joinedGroupIds) {
                val groupDoc = db.collection("groups").document(groupId).get().await()
                if (groupDoc.exists()) {
                    groupsList.add(
                        Group(
                            groupId = groupId,
                            groupName = groupDoc.getString("groupName") ?: "",
                            category = groupDoc.getString("category") ?: "",
                            isPrivate = groupDoc.getBoolean("private") ?: false,
                            participationType = groupDoc.getString("participationType") ?: "",
                            frequency = groupDoc.getString("frequency") ?: "",
                            description = groupDoc.getString("description") ?: "",
                            habitDuration = groupDoc.getString("habitDuration") ?: "",
                            members = (groupDoc.get("members") as? List<String>) ?: emptyList()
                        )
                    )
                    Log.e("isPrivate","isPrivate : ${groupDoc.getBoolean("private")}")
                }
            }
            userGroups = groupsList

            // Get user's habits
            val habitsList = mutableListOf<HabitFirebase>()
            val habitsCollection = db.collection("users").document(userId).collection("habits").get().await()
            for (doc in habitsCollection.documents) {
                val habitName = doc.getString("name") ?: continue
                val frequency = doc.getString("frequency") ?: "Günlük"
                val iconResId = doc.getLong("iconResId")?.toInt()
                val colorResId = doc.getLong("colorResId")?.toInt()
                val habitId = doc.getLong("habitId")

                habitsList.add(
                    HabitFirebase(
                        name = habitName,
                        frequency = frequency,
                        iconResId = iconResId,
                        colorResId = colorResId,
                        habitId = habitId!!.toInt()
                    )
                )
            }
            userHabits = habitsList

            isLoading = false

        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
                    .padding(top = 22.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 4.dp , top = 30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Geri",
                            tint = colorResource(id = R.color.yazirengi),

                        )
                    }



                    Text(
                       text = userName,
                        fontSize = 22.sp, // Kullanıcının ismi boyutu
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.noto_regular)),
                        color = colorResource(id = R.color.yazirengi),
                        modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                    )
                    RankBadge(rank = Constants.getRankFromPoints(userPoint) , modifier = Modifier.padding(start = 10.dp , top = 30.dp))


                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.arkaplan))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.kutubordrengi)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        // Modern Profile Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Profile Info Row

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 15.dp, bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Image with animated border
                                Box(
                                    modifier = Modifier
                                        .size(85.dp)
                                        .clip(CircleShape)
                                        .padding(3.dp)
                                ) {
                                    Image(
                                        painter = when {
                                            profileImage.isNotEmpty() -> {
                                                when {
                                                    profileImage.startsWith("http") || profileImage.startsWith("content") -> {
                                                        rememberAsyncImagePainter(model = profileImage)
                                                    }
                                                    else -> painterResource(getProfilePainter(profileImage, R.drawable.personel))
                                                }
                                            }
                                            else -> painterResource(R.drawable.personel)
                                        },
                                        contentDescription = "Profile Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // User Info and Stats
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatItem(
                                            value = userHabits.size.toString(),
                                            label = "Alışkanlık"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .width(1.dp)
                                                .background(colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f))
                                        )
                                        StatItem(
                                            value = userGroups.size.toString(),
                                            label = "Grup"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .width(1.dp)
                                                .background(colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f))
                                        )
                                        PointColor(modifier = Modifier ,userPoint )
                                    }
                                }
                            }

                            // Join Date with Icon in a Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.zaman),
                                        contentDescription = null,
                                        tint = colorResource(id = R.color.kutubordrengi),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Katılım tarihi: $joinDate",
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.yazirengi)
                                    )
                                }
                            }

                            // Custom Tab Bar
                            TabRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when (selectedTab) {
                        0 -> {
                            if (userHabits.isEmpty()) {
                                item { EmptyStateMessage(message = "Henüz alışkanlık bulunmuyor") }
                            } else {
                                items(userHabits) { habit ->
                                    HabitCard(habit = habit)
                                }
                            }
                        }
                        1 -> {
                            if (userGroups.isEmpty()) {
                                item { EmptyStateMessage(message = "Henüz grup bulunmuyor") }
                            } else {
                                items(userGroups) { group ->
                                    GroupCard(group = group, navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun TabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f))
            .padding(4.dp)
    ) {
        TabItem(
            text = "Alışkanlıklar",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabItem(
            text = "Gruplar",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) colorResource(id = R.color.kutubordrengi)
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else colorResource(id = R.color.yazirengi),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.talk),
            contentDescription = null,
            tint = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.yazirengi).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HabitCard(habit: HabitFirebase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.arkaplan)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = if (habit.colorResId == null) colorResource(R.color.dogarengi) else colorResource(
                            habit.colorResId
                        ),
                        shape = CircleShape
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (habit.iconResId == null) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.habits),
                        contentDescription = "icon",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    androidx.compose.material3.Icon(
                        painter = painterResource(habit.iconResId),
                        contentDescription = "icon",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = habit.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.yazirengi)
                )

                Text(
                    text = "Sıklık: ${habit.frequency}",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.yazirengi).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun GroupCard(group: Group,navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("GroupDetailScreen/${group.groupId}/${group.groupName}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.arkaplan)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = group.groupName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.yazirengi)
                )

                // Privacy badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (group.isPrivate)
                                colorResource(id = R.color.pastelkirmizi).copy(alpha = 0.2f)
                            else
                                colorResource(id = R.color.yesil).copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (group.isPrivate) "Özel" else "Açık",
                        fontSize = 12.sp,
                        color = if (group.isPrivate)
                            colorResource(id = R.color.pastelkirmizi)
                        else
                            colorResource(id = R.color.yesil)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.talk),
                    contentDescription = null,
                    tint = colorResource(id = R.color.kutubordrengi),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Kategori: ${group.category}",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.yazirengi)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.zaman),
                    contentDescription = null,
                    tint = colorResource(id = R.color.kutubordrengi),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Sıklık: ${group.frequency}",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.yazirengi)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_personal_info),
                    contentDescription = null,
                    tint = colorResource(id = R.color.kutubordrengi),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Katılımcılar: ${group.members.size}",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.yazirengi)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = colorResource(id = R.color.yazirengi),
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = colorResource(id = R.color.yazirengiacik),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


