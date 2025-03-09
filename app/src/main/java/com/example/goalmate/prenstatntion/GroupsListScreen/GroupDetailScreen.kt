package com.example.goalmate.prenstatntion.GroupsListScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.viewmodel.GroupsAddViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel
) {
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value

    LaunchedEffect(groupId) {
        groupsAddViewModel.getGroupById(groupId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.arkaplan))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "back",
                    tint = colorResource(R.color.yazirengi)
                )
            }
            
            Text(
                text = "Grup Detayları",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(R.color.yazirengi)
            )
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(colorResource(R.color.beyaz))
                .padding(20.dp)
        ) {
            when (groupDetailState) {
                is GroupDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.kutubordrengi),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                is GroupDetailState.Success -> {
                    val group = groupDetailState.group
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GroupHeaderSection(group)
                        }
                        
                        item {
                            GroupInfoCard(
                                title = "Grup Açıklaması",
                                content = group.description,
                                icon = R.drawable.open
                            )
                        }
                        
                        item {
                            GroupInfoCard(
                                title = "Motivasyon",
                                content = "Teknoloji, insanın sınırlarını zorlayan bir güç, ama unutmamalıyız ki, onu nasıl kullanacağımız, bizim sınırlarımızı belirler.",
                                icon = R.drawable.close
                            )
                        }
                        
                        item {
                            GroupScheduleCard(
                                frequency = group.frequency,
                                duration = group.habitDuration
                            )
                        }
                        
                        item {
                            ParticipantsSection(group.members,groupsAddViewModel)
                        }
                    }
                }
                
                is GroupDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hata: ${groupDetailState.message}",
                            color = colorResource(R.color.pastelkirmizi),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeaderSection(group: Group) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = group.groupName,
            style = MaterialTheme.typography.headlineMedium,
            color = colorResource(R.color.yazirengi),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "${group.participationType} - ${group.category}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = colorResource(R.color.kutubordrengi),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun GroupInfoCard(title: String, content: String, icon: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gri)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = colorResource(R.color.kutubordrengi),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.yazirengi)
                )
            }
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi)
            )
        }
    }
}

@Composable
fun GroupScheduleCard(frequency: String, duration: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gri)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.zaman),
                    contentDescription = null,
                    tint = colorResource(R.color.kutubordrengi),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Alışkanlık Programı",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.yazirengi)
                )
            }
            
            Text(
                text = "Sıklık: $frequency",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Süre: $duration",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi)
            )
        }
    }
}

@Composable
fun ParticipantsSection(members: List<String> , groupsAddViewModel: GroupsAddViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gri)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_personal_info),
                    contentDescription = null,
                    tint = colorResource(R.color.kutubordrengi),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Katılımcılar (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.yazirengi)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ParticipantList(members, groupsAddViewModel =  groupsAddViewModel)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            JoinGroupButton()
        }
    }
}

@Composable
fun ParticipantList(members: List<String> , groupsAddViewModel: GroupsAddViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        members.forEach { memberId ->
            ParticipantItem(memberId,groupsAddViewModel)
        }
    }
}

@Composable
fun ParticipantItem(memberId: String, groupsAddViewModel: GroupsAddViewModel) {
    val userName = groupsAddViewModel.getUserName.collectAsState().value
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    val profileImage = profileImages[memberId] ?: ""

    LaunchedEffect(memberId) {
        groupsAddViewModel.getUsersName(memberId)
        groupsAddViewModel.getProfile(memberId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.beyaz),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
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
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, colorResource(R.color.kutubordrengi), CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = userName,
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(R.color.yazirengi)
        )
    }
}

@Composable
fun JoinGroupButton() {
    Button(
        onClick = { /* Gruba katılma işlemi */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.kutubordrengi)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_personal_info),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Gruba Katıl",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
