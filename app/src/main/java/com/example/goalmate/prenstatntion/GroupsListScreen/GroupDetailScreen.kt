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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.data.localdata.Group
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.viewmodel.GroupsAddViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel
) {
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val joinGroupState = groupsAddViewModel.joinGroupState.collectAsState().value
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(joinGroupState) {
        joinGroupState?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                groupsAddViewModel.resetJoinGroupState()
            }
        }
    }

    LaunchedEffect(groupId) {
        groupsAddViewModel.getGroupById(groupId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.arkaplan))
                .padding(paddingValues)
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
                        val isUserMember = group.members.contains(currentUserId)

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                GroupHeaderSection(group)
                            }

                            if (!group.isPrivate || isUserMember) {
                                // Açık grup veya üye
                                item {
                                    GroupInfoCard(
                                        title = "Grup Açıklaması",
                                        content = group.description,
                                        icon = R.drawable.talk
                                    )
                                }
                                
                                item {
                                    GroupInfoCard(
                                        title = "Motivasyon",
                                        content = "Teknoloji, insanın sınırlarını zorlayan bir güç, ama unutmamalıyız ki, onu nasıl kullanacağımız, bizim sınırlarımızı belirler.",
                                        icon = R.drawable.book
                                    )
                                }
                                
                                item {
                                    GroupScheduleCard(
                                        frequency = group.frequency,
                                        duration = group.habitDuration
                                    )
                                }
                                
                                item {
                                    ParticipantsSection(
                                        members = group.members,
                                        groupsAddViewModel = groupsAddViewModel,
                                        showOnlyLeader = false,
                                        groupLeaderId = group.createdBy,
                                        showJoinButton = true
                                    )
                                }
                            } else {
                                // Özel grup ve üye değil
                                item {
                                    PrivateGroupInfo()
                                }

                                item {
                                    GroupInfoCard(
                                        title = "Motivasyon",
                                        content = "Eğer bu gruba katılmak istiyorsan, onay göndermek için adım atabilirsin. Haydi, harekete geç!",
                                        icon = R.drawable.book
                                    )
                                }

                                item {
                                    GroupScheduleCard(
                                        frequency = group.frequency,
                                        duration = group.habitDuration
                                    )
                                }

                                item {
                                    ParticipantsSection(
                                        members = listOf(group.createdBy),
                                        groupsAddViewModel = groupsAddViewModel,
                                        showOnlyLeader = true,
                                        groupLeaderId = group.createdBy,
                                        showJoinButton = true
                                    )
                                }
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
                text = "Süre: ${formatDuration(duration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi)
            )
        }
    }
}

private fun formatDuration(duration: String): String {
    return try {
        val seconds = duration.toIntOrNull() ?: 0
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        String.format("%02d:%02d", minutes, remainingSeconds)
    } catch (e: Exception) {
        "00:00"
    }
}

@Composable
fun ParticipantsSection(
    members: List<String>,
    groupsAddViewModel: GroupsAddViewModel,
    showOnlyLeader: Boolean,
    groupLeaderId: String,
    showJoinButton: Boolean,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val group = (groupDetailState as? GroupDetailState.Success)?.group
    val isUserMember = group?.members?.contains(currentUserId) ?: false

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
                    text = if (showOnlyLeader) "Grup Lideri" else "Katılımcılar (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.yazirengi)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            if (showOnlyLeader) {
                ParticipantItem(memberId = groupLeaderId, groupsAddViewModel = groupsAddViewModel)
            } else {
                members.forEach { memberId ->
                    ParticipantItem(memberId = memberId, groupsAddViewModel = groupsAddViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Her durumda katıl butonunu göster, ama üye ise deaktif olsun
            if (!isUserMember) {
                Spacer(modifier = Modifier.height(16.dp))
                JoinGroupButton(
                    db = db,
                    groupsAddViewModel = groupsAddViewModel
                )
            }
        }
    }
}

@Composable
fun ParticipantItem(memberId: String, groupsAddViewModel: GroupsAddViewModel) {
    val userNames = groupsAddViewModel.userNames.collectAsState().value
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    
    val userName = userNames[memberId] ?: "Yükleniyor..."
    val profileImage = profileImages[memberId] ?: ""

    LaunchedEffect(key1 = memberId) {
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
fun JoinGroupButton(
    db: FirebaseFirestore,
    groupsAddViewModel: GroupsAddViewModel
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val joinGroupState = groupsAddViewModel.joinGroupState.collectAsState().value
    val scope = rememberCoroutineScope()
    
    var showJoinDialog by remember { mutableStateOf(false) }
    
    if (currentUserId != null && groupDetailState is GroupDetailState.Success) {
        val group = groupDetailState.group
        val isUserInGroup = group.members.contains(currentUserId)
        
        Button(
            onClick = { 
                if (!isUserInGroup) {
                    showJoinDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isUserInGroup) 
                    colorResource(R.color.gri) 
                else 
                    colorResource(R.color.kutubordrengi)
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !isUserInGroup
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
                    text = if (isUserInGroup) "Zaten Katıldınız" else "Gruba Katıl",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (showJoinDialog) {
            JoinPrivateGroupDialog(
                group = group,
                onDismiss = { showJoinDialog = false },
                onJoinRequest = { code ->
                    scope.launch {
                        groupsAddViewModel.requestJoinGroup(group.groupId, currentUserId, code)
                    }
                    showJoinDialog = false
                }
            )
        }
    }
}

@Composable
fun JoinPrivateGroupDialog(
    group: Group,
    onDismiss: () -> Unit,
    onJoinRequest: (String?) -> Unit
) {
    var joinCode by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf<JoinOption?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Özel Gruba Katılım",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Bu özel bir grup. Katılmak için seçeneklerden birini kullanın:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Katılım seçenekleri
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableOption(
                        text = "Katılım İsteği Gönder",
                        selected = selectedOption == JoinOption.REQUEST,
                        onClick = { selectedOption = JoinOption.REQUEST }
                    )
                    
                    SelectableOption(
                        text = "Katılım Kodu Gir",
                        selected = selectedOption == JoinOption.CODE,
                        onClick = { selectedOption = JoinOption.CODE }
                    )
                }

                // Kod girişi alanı (sadece kod seçeneği seçiliyse göster)
                if (selectedOption == JoinOption.CODE) {
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        label = { Text("Katılım Kodu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedOption) {
                        JoinOption.REQUEST -> onJoinRequest(null)
                        JoinOption.CODE -> onJoinRequest(joinCode)
                        null -> onDismiss()
                    }
                },
                enabled = selectedOption != null && 
                         (selectedOption == JoinOption.REQUEST || 
                          (selectedOption == JoinOption.CODE && joinCode.isNotEmpty()))
            ) {
                Text("Katıl")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun SelectableOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) 
                    colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)
                else 
                    Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colorResource(R.color.kutubordrengi)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

private enum class JoinOption {
    REQUEST,
    CODE
}

@Composable
fun PrivateGroupInfo() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gri)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock), // Kilit ikonu ekleyin
                contentDescription = null,
                tint = colorResource(R.color.kutubordrengi),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Bu özel bir grup",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.yazirengi)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Grup detaylarını görmek için gruba katılmanız gerekmektedir.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi),
                textAlign = TextAlign.Center
            )
        }
    }
}
