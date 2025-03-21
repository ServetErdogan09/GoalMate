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
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import com.example.goalmate.viewmodel.RegisterViewModel

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel,
    motivationQuoteViewModel: MotivationQuoteViewModel,
    registerViewModel: RegisterViewModel
) {
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val joinGroupState = groupsAddViewModel.joinGroupState.collectAsState().value
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Kullanıcı grup üyesi ise showGroupChatScreen sayfasına yönlendirme yap
    LaunchedEffect(groupDetailState) {
        if (groupDetailState is GroupDetailState.Success && currentUserId != null) {
            if (groupDetailState.group.members.contains(currentUserId)) {
                navController.navigate("showGroupChatScreen") {
                    navController.popBackStack()
                   // popUpTo("GroupDetailScreen/${groupId}") { inclusive = true }
                }
                return@LaunchedEffect
            }
        }
    }



    DisposableEffect(Unit) {
        onDispose {
            snackbarHostState.currentSnackbarData?.dismiss() // Sayfadan çıkarken Snackbar'ı kapat
            Log.e("girdi","girdi")
        }
    }

    // Grup detaylarını yükle
    LaunchedEffect(groupId) {
        groupsAddViewModel.getGroupById(groupId)
    }

    LaunchedEffect(joinGroupState) {
        joinGroupState?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                groupsAddViewModel.resetJoinGroupState()
            }
        }
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
                                        content = group.quote,
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
                                        showJoinButton = true,
                                        navController = navController,
                                        registerViewModel =registerViewModel
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
                                        showJoinButton = true,
                                        navController = navController,
                                        registerViewModel = registerViewModel
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
    // Debug log ekleyelim
    LaunchedEffect(Unit) {
        Log.d("GroupDetailScreen", "Group Details:")
        Log.d("GroupDetailScreen", "isPrivate: ${group.isPrivate}")
        Log.d("GroupDetailScreen", "participationType: ${group.participationType}")
        Log.d("GroupDetailScreen", "groupName: ${group.groupName}")
    }

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
                    containerColor = if (group.isPrivate) 
                        colorResource(R.color.kirmizi).copy(alpha = 0.1f)
                    else 
                        colorResource(R.color.yesil2).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (group.isPrivate) R.drawable.close
                            else R.drawable.open
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (group.isPrivate) 
                            colorResource(R.color.pastelkirmizi)
                        else 
                            colorResource(R.color.yesil2)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (group.isPrivate) {
                            Log.d("GroupDetailScreen", "Displaying as Private Group")
                            "Özel - ${group.category}"
                        } else {
                            Log.d("GroupDetailScreen", "Displaying as Open Group")
                            "Açık - ${group.category}"
                        },
                        color = if (group.isPrivate) 
                            colorResource(R.color.pastelkirmizi)
                        else 
                            colorResource(R.color.yesil2),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
    registerViewModel: RegisterViewModel,
    showOnlyLeader: Boolean,
    groupLeaderId: String,
    showJoinButton: Boolean,
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    navController: NavController
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
                ParticipantItem(memberId = groupLeaderId, groupsAddViewModel = groupsAddViewModel, navController = navController)
            } else {
                members.forEach { memberId ->
                    ParticipantItem(memberId = memberId, groupsAddViewModel = groupsAddViewModel, navController = navController )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Her durumda katıl butonunu göster, ama üye ise deaktif olsun
            if (!isUserMember) {
                Spacer(modifier = Modifier.height(16.dp))
                JoinGroupButton(
                    db = db,
                    groupsAddViewModel = groupsAddViewModel,
                    registerViewModel = registerViewModel
                )
            }
        }
    }
}

@Composable
fun ParticipantItem(memberId: String, groupsAddViewModel: GroupsAddViewModel, navController: NavController) {
    val userNames = groupsAddViewModel.userNames.collectAsState().value
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val group = (groupDetailState as? GroupDetailState.Success)?.group
    
    val userName = userNames[memberId] ?: "Yükleniyor..."
    val profileImage = profileImages[memberId] ?: ""
    val isGroupLeader = group?.createdBy == memberId

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
            .clickable {
                navController.navigate("ViewProfile/${memberId}")
                Log.e("ViewProfile","kullanıcı ıd : $memberId")
            }
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
                .clickable {
                    navController.navigate("ViewProfile/${memberId}")
                    Log.e("ViewProfile","kullanıcı ıd : $memberId")
                }
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            if (isGroupLeader){
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.clickable {
                        navController.navigate("ViewProfile/${memberId}")
                        Log.e("ViewProfile","kullanıcı ıd : $memberId")
                    }
                )

                Text(
                    text = "Yönetici",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.clickable {
                        navController.navigate("ViewProfile/${memberId}")
                        Log.e("ViewProfile","kullanıcı ıd : $memberId")
                    }
                )
            }else{
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.yazirengi),
                    modifier = Modifier.clickable {
                        navController.navigate("ViewProfile/${memberId}")
                        Log.e("ViewProfile","kullanıcı ıd : $memberId")
                    }
                )
            }
        }


    }
}

@Composable
fun JoinGroupButton(
    db: FirebaseFirestore,
    groupsAddViewModel: GroupsAddViewModel,
    registerViewModel: RegisterViewModel
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val scope = rememberCoroutineScope()
    val joinedGroupsCount = registerViewModel.joinedGroupsCount.collectAsState().value
    val maxAllowedGroups = registerViewModel.maxAllowedGroups.collectAsState().value
    
    var showJoinDialog by remember { mutableStateOf(false) }
    var showMaxGroupsDialog by remember { mutableStateOf(false) }
    
    if (currentUserId != null && groupDetailState is GroupDetailState.Success) {
        val group = groupDetailState.group
        val isUserInGroup = group.members.contains(currentUserId)
        val isGroupFull = group.members.size >= group.participantNumber
        val hasReachedGroupLimit = joinedGroupsCount >= maxAllowedGroups
        
        if (showMaxGroupsDialog) {
            AlertDialog(
                onDismissRequest = { showMaxGroupsDialog = false },
                title = {
                    Text(
                        text = "Grup Limiti",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(id = R.color.yazirengi)
                    )
                },
                text = {
                    Text(
                        text = "Maksimum katılabileceğiniz grup sayısına ulaştınız ($maxAllowedGroups). Yeni bir gruba katılmak için önce başka bir gruptan ayrılmalısınız.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.yazirengi)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showMaxGroupsDialog = false }) {
                        Text("Tamam")
                    }
                }
            )
        }
        
        Button(
            onClick = { 
                if (!isUserInGroup && !isGroupFull) {
                    if (hasReachedGroupLimit) {
                        showMaxGroupsDialog = true
                    } else {
                        if (group.isPrivate) {
                            showJoinDialog = true
                        } else {
                            // Açık grup için direkt katılım
                            scope.launch {
                                groupsAddViewModel.requestJoinGroup(
                                    groupId = group.groupId,
                                    userId = currentUserId,
                                    joinCode = null,
                                    participantNumber = group.participantNumber,
                                    members = group.members
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isUserInGroup -> colorResource(R.color.gri)
                    isGroupFull -> colorResource(R.color.gri)
                    hasReachedGroupLimit -> colorResource(R.color.pastelkirmizi)
                    else -> colorResource(R.color.kutubordrengi)
                }
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !isUserInGroup && !isGroupFull
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = when {
                        isUserInGroup -> painterResource(R.drawable.ic_personal_info)
                        isGroupFull -> painterResource(R.drawable.close)
                        hasReachedGroupLimit -> painterResource(R.drawable.close)
                        else -> painterResource(R.drawable.ic_personal_info)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when {
                        hasReachedGroupLimit -> colorResource(R.color.beyaz)
                        isGroupFull -> colorResource(R.color.pastelkirmizi)
                        else -> colorResource(R.color.beyaz)
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = when {
                        isUserInGroup -> "Zaten Katıldınız"
                        isGroupFull -> "✨ Grup Şu An Dolu! (${group.members.size}/${group.participantNumber})"
                        hasReachedGroupLimit -> "Grup Limitine Ulaştınız ($joinedGroupsCount/$maxAllowedGroups)"
                        else -> if (group.isPrivate) "Gruba Katıl" else "Gruba Katıl"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isGroupFull -> colorResource(R.color.pastelkirmizi)
                            else -> colorResource(R.color.beyaz)
                        }
                    )
                )
            }
        }

        if (showJoinDialog && !isGroupFull && !hasReachedGroupLimit && group.isPrivate) {
            JoinPrivateGroupDialog(
                group = group,
                onDismiss = { showJoinDialog = false },
                onJoinRequest = { code ->
                    scope.launch {
                        groupsAddViewModel.requestJoinGroup(group.groupId, currentUserId, code, group.participantNumber, group.members)
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
            .clickable(
                onClick = onClick)
            
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
