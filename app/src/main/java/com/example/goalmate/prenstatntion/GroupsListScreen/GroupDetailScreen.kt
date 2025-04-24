package com.example.goalmate.prenstatntion.GroupsListScreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.goalmate.prenstatntion.AnalysisScreen.totalHabit
import com.example.goalmate.utils.NetworkUtils
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import com.example.goalmate.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupName : String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel,
    motivationQuoteViewModel: MotivationQuoteViewModel,
    registerViewModel: RegisterViewModel
) {
    val context = LocalContext.current
    var showNoInternetDialog by remember { mutableStateOf(false) }

    // İnternet bağlantısını kontrol et
    LaunchedEffect(Unit) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            showNoInternetDialog = true
        }
    }

    // İnternet yok uyarı dialogu
    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { 
                navController.popBackStack()
            },
            title = {
                Text(
                    text = "İnternet Bağlantısı Yok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Gruplara erişmek için internet bağlantısı gereklidir. Lütfen internet bağlantınızı kontrol edip tekrar deneyin.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.kutubordrengi)
                    )
                ) {
                    Text("Tamam")
                }
            }
        )
        return
    }

    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val joinGroupState = groupsAddViewModel.joinGroupState.collectAsState().value
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var showCloseGroupDialog by remember { mutableStateOf(false) }
    var showLeaveGroupDialog by remember { mutableStateOf(false) }

    // Grup detaylarını yükle ve state'i temizle
    LaunchedEffect(groupId) {
        // Önce state'i temizle
        groupsAddViewModel.resetJoinGroupState()
        // Sonra grup detaylarını yükle
        groupsAddViewModel.getGroupById(groupId)
    }


    // Kullanıcı grup üyesi ise showGroupChatScreen sayfasına yönlendirme yap
    LaunchedEffect(groupDetailState) {

       // val currentTime = System.currentTimeMillis() // burası değiştirip sunucudan alınacak test amaçlı böyle kalsın
        val currentTime = NetworkUtils.getTime(context) // burası değiştirip sunucudan alınacak test amaçlı böyle kalsın
        if (groupDetailState is GroupDetailState.Success && currentUserId != null) {
            val group = groupDetailState.group
            // Grup aktif VE kullanıcı üye ise yönlendir
            if (group.groupStatus == "ACTIVE" && group.members.contains(currentUserId)) {
                // State'in tamamen güncellenmesi için biraz bekle
                delay(300)

                val members = group.members.size

                val daysLeft = remainingDays(group = group , currentTime = currentTime)
                Log.e("members","$daysLeft kalan gün saysıı")


                //  grouptan gönderebilirz kalan gün sayısını ve toplam katılımcı sayısını navigation ile diğer ekrana parametre olarka gönderebilriiz
                navController.navigate("showGroupChatScreen/${groupId}/${groupName}/${members}/${daysLeft}/${group.frequency}")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Sayfadan çıkarken tüm state'leri temizle
            snackbarHostState.currentSnackbarData?.dismiss()
            groupsAddViewModel.resetJoinGroupState()
        }
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
                        val groupCode = group.groupCode
                        val isUserMember = group.members.contains(currentUserId)
                        val isGroupAdmin = currentUserId == group.createdBy

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                GroupHeaderSection(group)
                            }

                            item {
                                GroupStatusSection(group, groupsAddViewModel)
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
                                        navController = navController,
                                        groupCode = groupCode
                                    )
                                }

                                // Grup Yönetimi Butonları
                                if (isUserMember) {
                                    item {
                                        GroupManagementButtons(
                                            isGroupAdmin = isGroupAdmin,
                                            onLeaveClick = { showLeaveGroupDialog = true },
                                            onCloseClick = { showCloseGroupDialog = true }
                                        )
                                    }
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
                                        groupCode = groupCode,
                                        navController = navController,
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

    // Grup kapatma onay dialog'u
    if (showCloseGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCloseGroupDialog = false },
            title = { Text("Grubu Kapat") },
            text = { Text("Bu grubu kapatmak istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            groupsAddViewModel.closeGroup(groupId)
                            showCloseGroupDialog = false
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pastelkirmizi)
                    )
                ) {
                    Text("Grubu Kapat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseGroupDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Gruptan ayrılma onay dialog'u
    if (showLeaveGroupDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveGroupDialog = false },
            title = { Text("Gruptan Ayrıl") },
            text = { Text("Bu gruptan ayrılmak istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            groupsAddViewModel.leaveGroup(groupId)
                            showLeaveGroupDialog = false
                            // Verilerin güncellenmesi için kısa bir gecikme ekle
                            kotlinx.coroutines.delay(500)
                            navController.navigate("groupListScreen") {
                                popUpTo("groupListScreen") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pastelkirmizi)
                    )
                ) {
                    Text("Ayrıl")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveGroupDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun GroupHeaderSection(group: Group) {
    // Debug log ekleyelim
    LaunchedEffect(Unit) {
        Log.d("GroupDetailScreen", "Group Details:")
        Log.d("GroupDetailScreen", "private: ${group.isPrivate}")
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
    showOnlyLeader: Boolean,
    groupCode : String,
    groupLeaderId: String,
    navController: NavController
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val group = (groupDetailState as? GroupDetailState.Success)?.group
    val isUserMember = group?.members?.contains(currentUserId) ?: false
    val scope = rememberCoroutineScope()


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
            if (!isUserMember && group != null) {
                Spacer(modifier = Modifier.height(16.dp))
                JoinGroupButton(
                    group = group,
                    onJoinClick = {
                        scope.launch {
                            currentUserId?.let { userId ->
                                group.groupId.let { groupId ->
                                    groupsAddViewModel.requestJoinGroup(
                                        groupId = groupId,
                                        userId = userId,
                                        joinCode = groupCode,
                                        participantNumber = group.muxParticipationCount,
                                        members = group.members
                                    )
                                }
                            }
                        }
                    },
                    onRequestJoinClick = {
                        scope.launch {
                            currentUserId?.let { userId ->
                                group.groupId.let { groupId ->
                                    groupsAddViewModel.requestJoinGroup(
                                        groupId = groupId,
                                        userId = userId,
                                        joinCode = null,
                                        participantNumber = group.muxParticipationCount,
                                        members = group.members
                                    )
                                }
                            }
                        }
                    },
                    isUserInGroup = isUserMember,
                    isRequestPending = false,
                    isGroupFull = group.members.size >= group.muxParticipationCount
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
    group: Group,
    onJoinClick: () -> Unit,
    onRequestJoinClick: () -> Unit,
    isUserInGroup: Boolean,
    isRequestPending: Boolean,
    isGroupFull: Boolean
) {
    val maxParticipants = group.muxParticipationCount
    val currentParticipants = group.members.size
    val isFull = currentParticipants >= maxParticipants
    var showJoinPrivateGroupDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isGroupActive = group.groupStatus == "ACTIVE"

    Button(
        onClick = {
            if (isFull || isGroupActive) {
                return@Button
            }
            if (group.isPrivate) {
                showJoinPrivateGroupDialog = true
            } else {
                // Açık grup - direkt katılım
                scope.launch {
                    currentUserId?.let { userId ->
                        group.groupId.let { groupId ->
                            onJoinClick()
                        }
                    }
                }
            }
        },
        enabled = !isUserInGroup && !isRequestPending && !isFull && !isGroupActive,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isGroupActive -> Color.Gray
                isFull -> Color.Gray
                else -> colorResource(R.color.kutubordrengi)
            }
        )
    ) {
        Text(
            text = when {
                isUserInGroup -> "Gruptasınız"
                isRequestPending -> "Katılım İsteği Gönderildi"
                isGroupActive -> "Grup Aktif - Katılım Kapalı"
                isFull -> "Grup Dolu ($currentParticipants/$maxParticipants)"
                group.isPrivate -> "Gruba Katıl"
                else -> "Gruba Katıl"
            }
        )
    }

    if (showJoinPrivateGroupDialog) {
        JoinPrivateGroupDialog(
            group = group,
            onDismiss = { showJoinPrivateGroupDialog = false },
            onJoinRequest = { joinCode ->
                scope.launch {
                    currentUserId?.let { userId ->
                        group.groupId.let { groupId ->
                            if (!joinCode.isNullOrEmpty()) {
                                // Kod ile katılım - direkt katılım
                                onJoinClick()
                            } else {
                                // Kod olmadan katılım - istek gönderme
                                onRequestJoinClick()
                            }
                        }
                    }
                }
                showJoinPrivateGroupDialog = false
            }
        )
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
                        onClick = { 
                            selectedOption = JoinOption.REQUEST
                            joinCode = "" // İstek seçildiğinde kodu temizle
                        }
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
                Text(
                    text = when (selectedOption) {
                        JoinOption.REQUEST -> "İstek Gönder"
                        JoinOption.CODE -> "Katıl"
                        null -> "İptal"
                    }
                )
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

@Composable
fun GroupManagementButtons(
    isGroupAdmin: Boolean,
    onLeaveClick: () -> Unit,
    onCloseClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Grup Yönetimi",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.yazirengi)
            )
            if (isGroupAdmin) {
                Button(
                    onClick = onCloseClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pastelkirmizi)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text("Grubu Kapat")
                    }
                }
            } else {
                Button(
                    onClick = onLeaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.kutubordrengi)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text("Gruptan Ayrıl")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupStatusSection(group: Group, viewModel: GroupsAddViewModel) {
    var remainingTime by remember { mutableStateOf<Long>(0) }
    var daysLeft by remember { mutableStateOf(0L) }
    var hoursLeft by remember { mutableStateOf(0L) }
    var minutesLeft by remember { mutableStateOf(0L) }
    var isTimeExpired by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(group) {
        while (true) {
            val currentTime = NetworkUtils.getTime(context)
            val deadline = group.startDeadline
            val timeUntilDeadline = deadline - currentTime

            when (group.groupStatus) {
                "WAITING" -> {
                    if (timeUntilDeadline > 0) {
                        isTimeExpired = false
                        remainingTime = timeUntilDeadline
                        daysLeft = TimeUnit.MILLISECONDS.toDays(remainingTime)
                        val remainingHours = remainingTime - TimeUnit.DAYS.toMillis(daysLeft)
                        hoursLeft = TimeUnit.MILLISECONDS.toHours(remainingHours)
                        val remainingMinutes = remainingHours - TimeUnit.HOURS.toMillis(hoursLeft)
                        minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMinutes)
                    } else {
                        isTimeExpired = true
                        daysLeft = 0
                        hoursLeft = 0
                        minutesLeft = 0
                    }
                }
                "ACTIVE" -> {
                    isTimeExpired = false
                    val startDate = group.actualStartDate ?: currentTime
                    val activeTime = currentTime - startDate
                    daysLeft = TimeUnit.MILLISECONDS.toDays(activeTime)

                }
            }
            delay(1000) // Her saniye güncelle
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isTimeExpired && group.groupStatus == "WAITING" -> colorResource(R.color.pastelkirmizi).copy(alpha = 0.1f)
                group.groupStatus == "WAITING" -> colorResource(R.color.gri)
                group.groupStatus == "ACTIVE" -> colorResource(R.color.yesil2).copy(alpha = 0.1f)
                group.groupStatus == "EXPIRED" -> colorResource(R.color.pastelkirmizi).copy(alpha = 0.1f)
                group.groupStatus == "CLOSED" -> colorResource(R.color.pastelkirmizi).copy(alpha = 0.1f)
                else -> colorResource(R.color.gri)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                when {
                                    isTimeExpired && group.groupStatus == "WAITING" -> R.drawable.close
                                    group.groupStatus == "WAITING" -> R.drawable.zaman
                                    group.groupStatus == "ACTIVE" -> R.drawable.open
                                    else -> R.drawable.close
                                }
                            ),
                            contentDescription = null,
                            tint = when {
                                isTimeExpired && group.groupStatus == "WAITING" -> colorResource(R.color.pastelkirmizi)
                                group.groupStatus == "WAITING" -> colorResource(R.color.kutubordrengi)
                                group.groupStatus == "ACTIVE" -> colorResource(R.color.yesil2)
                                else -> colorResource(R.color.pastelkirmizi)
                            },
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = when {
                                    isTimeExpired && group.groupStatus == "WAITING" -> "Süre Doldu"
                                    group.groupStatus == "WAITING" -> "Başlamayı Bekliyor"
                                    group.groupStatus == "ACTIVE" -> "Aktif"
                                    group.groupStatus == "EXPIRED" -> "Süre Doldu"
                                    group.groupStatus == "CLOSED" -> "Kapalı"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    isTimeExpired && group.groupStatus == "WAITING" -> colorResource(R.color.pastelkirmizi)
                                    group.groupStatus == "WAITING" -> colorResource(R.color.kutubordrengi)
                                    group.groupStatus == "ACTIVE" -> colorResource(R.color.yesil2)
                                    else -> colorResource(R.color.pastelkirmizi)
                                }
                            )
                            if (isTimeExpired && group.groupStatus == "WAITING") {
                                Text(
                                    text = "İşlem Bekleniyor",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorResource(R.color.pastelkirmizi)
                                )
                            }
                        }
                    }
                }

                // Katılımcı Sayısı
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_personal_info),
                        contentDescription = null,
                        tint = when {
                            group.members.size >= group.muxParticipationCount -> colorResource(R.color.yesil2)
                            group.members.size >= group.minParticipationCount -> colorResource(R.color.kutubordrengi)
                            else -> colorResource(R.color.pastelkirmizi)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${group.members.size}/${group.muxParticipationCount}\n(min: ${group.minParticipationCount})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.yazirengi),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (group.groupStatus != "CLOSED" && group.groupStatus != "EXPIRED") {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isTimeExpired && group.groupStatus == "WAITING") {
                    Text(
                        text = "Grup durumu güncelleniyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.pastelkirmizi),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (group.groupStatus == "WAITING") {
                            TimeUnit(value = daysLeft, unit = "Gün")
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.titleLarge,
                                color = colorResource(R.color.kutubordrengi),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            TimeUnit(value = hoursLeft, unit = "Saat")
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.titleLarge,
                                color = colorResource(R.color.kutubordrengi),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            TimeUnit(value = minutesLeft, unit = "Dk")

                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun remainingDays(group: Group, currentTime: Long): Long {
    val startDate = group.actualStartDate ?: currentTime
    Log.e("members", "$startDate başlama tarihi")
    val frequency = group.frequency

    // Başlangıç tarihini sıfırla (saat, dakika, saniye sıfırlansın)
    val startCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = startDate
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    // Şu anki tarihi sıfırla
    val currentCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = currentTime
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    // Toplam gün sayısını hesapla
    val totalDays = totalHabit(frequency)

    // İki tarih arasındaki farkı gün cinsinden hesapla
    val diffInMillis = currentCalendar.timeInMillis - startCalendar.timeInMillis
    val daysBetween = (diffInMillis / (24 * 60 * 60 * 1000)).toInt()

    Log.e("members", "Başlangıç tarihi: ${startCalendar.time}")
    Log.e("members", "Şu anki tarih: ${currentCalendar.time}")
    Log.e("members", "Gün farkı: $daysBetween")
    Log.e("members", "Toplam gün: $totalDays")

    // Eğer geçen gün sayısı toplam günden fazlaysa 0 döndür
    if (daysBetween >= totalDays) {
        return 0L
    }

    // Kalan gün sayısını hesapla
    val remainingDays = (totalDays - daysBetween).toLong()
    Log.e("members", "Kalan gün: $remainingDays")

    return remainingDays
}

@Composable
private fun TimeUnit(value: Long, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colorResource(R.color.kutubordrengi)
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.yazirengi)
        )
    }
}
