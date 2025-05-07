package com.example.goalmate.prenstatntion.GroupMembers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.goalmate.R
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.example.goalmate.viewmodel.BadgesViewModel
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembers(
    groupId: String,
    groupName: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel,
    badgesViewModel: BadgesViewModel
) {
    val members = groupsAddViewModel.groupMembers.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        groupsAddViewModel.getGroupMembers(groupId)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.arkaplan))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Geri",
                            tint = colorResource(R.color.yazirengi)
                        )
                    }

                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(R.color.yazirengi),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.2f)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    containerColor = colorResource(id = R.color.kutubordrengi),
                    contentColor = colorResource(id = R.color.beyaz),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = data.visuals.message)
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
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(colorResource(id = R.color.beyaz)),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.beyaz)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_personal_info),
                                contentDescription = null,
                                tint = colorResource(R.color.kutubordrengi),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grup Üyeleri (${members.value.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.yazirengi)
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            itemsIndexed(members.value) { index, memberId ->
                                Column {
                                    ModernParticipantItem(
                                        memberId = memberId,
                                        ranking = index + 1,
                                        isCurrentUser = memberId == currentUserId,
                                        groupsAddViewModel = groupsAddViewModel,
                                        navController = navController,
                                        groupId = groupId,
                                        badgesViewModel = badgesViewModel,
                                        snackbarHostState = snackbarHostState
                                    )
                                    
                                    // Don't add divider after the last item
                                    if (index < members.value.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 8.dp, bottom = 8.dp),
                                            thickness = 1.dp,
                                            color = colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
                                        )
                                    }
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
fun ModernParticipantItem(
    memberId: String,
    ranking: Int,
    groupId: String,
    isCurrentUser: Boolean,
    groupsAddViewModel: GroupsAddViewModel,
    navController: NavController,
    badgesViewModel: BadgesViewModel,
    snackbarHostState: SnackbarHostState
) {
    val userNames = groupsAddViewModel.userNames.collectAsState().value
    val profileImages = groupsAddViewModel.profileImages.collectAsState().value
    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value
    val scope = rememberCoroutineScope()

    val userName = userNames[memberId] ?: "Yükleniyor..."
    val profileImage = profileImages[memberId] ?: ""
    val isGroupLeader = groupDetailState?.let {
        when (it) {
            is com.example.goalmate.extrensions.GroupDetailState.Success -> it.group.createdBy == memberId
            else -> false
        }
    } ?: false

    var isPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            colorResource(id = R.color.kutubordrengi).copy(alpha = 0.1f)
        else
            colorResource(id = R.color.gri),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "backgroundAnimation"
    )

    LaunchedEffect(memberId) {
        groupsAddViewModel.getUsersName(memberId)
        groupsAddViewModel.getProfile(memberId)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable {
                    isPressed = true
                    showMenu = true
                },
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = if (isCurrentUser) {
                BorderStroke(
                    width = 1.dp,
                    color = colorResource(id = R.color.yesil2).copy(alpha = 0.3f)
                )
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ranking Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 1.5.dp,
                            color = colorResource(id = R.color.yesil2).copy(
                                alpha = when (ranking) {
                                    1 -> 1f
                                    2 -> 0.8f
                                    3 -> 0.6f
                                    else -> 0.4f
                                }
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(
                            colorResource(id = R.color.yesil2).copy(
                                alpha = when (ranking) {
                                    1 -> 0.2f
                                    2 -> 0.15f
                                    3 -> 0.1f
                                    else -> 0.05f
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ranking.toString(),
                        color = colorResource(id = R.color.yesil2).copy(
                            alpha = when (ranking) {
                                1 -> 1f
                                2 -> 0.8f
                                3 -> 0.6f
                                else -> 0.4f
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Profile Image
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = colorResource(R.color.kutubordrengi),
                            shape = CircleShape
                        )
                ) {
                    AsyncImage(
                        model = when {
                            profileImage.isNotEmpty() -> {
                                when {
                                    profileImage.startsWith("http") || profileImage.startsWith("content") -> profileImage
                                    else -> getProfilePainter(profileImage, R.drawable.personel)
                                }
                            }
                            else -> R.drawable.personel
                        },
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )

                    if (isCurrentUser) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.yesil2))
                                .border(2.dp, colorResource(id = R.color.beyaz), CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userName + if (isCurrentUser) " (Sen)" else "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorResource(id = R.color.yazirengi)
                    )

                    if (isGroupLeader) {
                        Text(
                            text = "Grup Yöneticisi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = colorResource(id = R.color.kutubordrengi)
                        )
                    }
                }

                if (isGroupLeader) {
                    Icon(
                        painter = painterResource(R.drawable.crown),
                        contentDescription = "Admin",
                        tint = colorResource(id = R.color.kutubordrengi),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
                isPressed = false
            },
            modifier = Modifier
                .background(colorResource(id = R.color.kutubordrengi))
                .width(150.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Profili Görüntüle",
                        color = Color.White
                    )
                },
                onClick = {
                    showMenu = false
                    navController.navigate("ViewProfile/${memberId}")
                }
            )

            if (!isCurrentUser && !isGroupLeader) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                DropdownMenuItem(
                    text = {
                        Text(
                            "Gruptan Çıkar",
                            color = Color.White
                        )
                    },
                    onClick = {
                        showMenu = false
                        showConfirmationDialog = true
                    }
                )
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(
                        text = "Kullanıcıyı Çıkar",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(id = R.color.yazirengi)
                    )
                },
                text = {
                    Text(
                        text = "Bu kullanıcıyı gruptan çıkarmak istediğinizden emin misiniz? Bu işlem geri alınamaz ve kullanıcı tekrar gruba katılamaz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.yazirengi)
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showConfirmationDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(R.color.kutubordrengi).copy(alpha = 0.1f))
                        ) {
                            Text(
                                "İptal",
                                color = colorResource(id = R.color.kutubordrengi),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        TextButton(
                            onClick = {
                                showConfirmationDialog = false
                                groupsAddViewModel.leaveGroup(groupId = groupId, userId = memberId)
                                badgesViewModel.fetchKickedMemberCount()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "$userName gruptan çıkarıldı",
                                        duration = SnackbarDuration.Short
                                    )
                                    groupsAddViewModel.getGroupMembers(groupId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(R.color.kutubordrengi))
                        ) {
                            Text(
                                "Evet, Çıkar",
                                color = colorResource(id = R.color.beyaz),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                },
                dismissButton = null,
                containerColor = colorResource(id = R.color.beyaz),
                shape = RoundedCornerShape(16.dp),
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }
}