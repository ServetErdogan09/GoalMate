package com.example.goalmate.prenstatntion.UserScreen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.goalmate.data.AuthState
import com.example.goalmate.viewmodel.RegisterViewModel
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.R
import com.example.goalmate.data.localdata.NextRankInfo
import com.example.goalmate.prenstatntion.homescreen.getProfilePainter
import com.example.goalmate.utils.Constants

@Composable
fun UserScreen(
    registerViewModel: RegisterViewModel,
    navController: NavController,
    context: Context
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    val profileImage by registerViewModel.profileImage.collectAsState()
    val profileName by registerViewModel.userName.collectAsState()

    val auth = FirebaseAuth.getInstance()
    val authState by registerViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Idle -> {
                if (auth.currentUser == null) {
                    navController.navigate("WelcomeScreen") {
                        popUpTo("UserScreen") { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .background(colorResource(R.color.arkaplan))
    ) {
        // Profile Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            val painter = when {
                profileImage.startsWith("http") || profileImage.startsWith("content") -> {
                    rememberAsyncImagePainter(
                        model = profileImage,
                        error = painterResource(R.drawable.bildl)
                    )
                }
                else -> {
                    painterResource(id = getProfilePainter(profileImage, R.drawable.personel))
                }
            }

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(R.color.kutubordrengi), CircleShape)
            )

            // User Info
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = profileName ?: "Kullanıcı",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = auth.currentUser?.email ?: "",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Rank Progress Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            elevation = 4.dp,
            backgroundColor = colorResource(R.color.arkaplan)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Constants.getRankIcon(rank = Constants.getRankFromPoints(registerViewModel.userPoints.collectAsState().value))),
                            contentDescription = "Current Rank",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Constants.getRankFromPoints(registerViewModel.userPoints.collectAsState().value),
                            style = MaterialTheme.typography.h6,
                            color = colorResource(R.color.yazirengi)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                val progress = calculateProgress(
                    currentPoints = registerViewModel.userPoints.collectAsState().value,
                    nextRankInfo = getNextRankInfo(registerViewModel.userPoints.collectAsState().value)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(colorResource(R.color.kutubordrengi).copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(colorResource(R.color.kutubordrengi))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${registerViewModel.userPoints.collectAsState().value} puan",
                        style = MaterialTheme.typography.caption,
                        color = colorResource(R.color.yazirengi)
                    )
                    Text(
                        text = "${getNextRankInfo(registerViewModel.userPoints.collectAsState().value).nextRankMinPoints} puan",
                        style = MaterialTheme.typography.caption,
                        color = colorResource(R.color.yazirengi)
                    )
                }
            }
        }

        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MenuItemWithDivider(text = "Bildirimler", icon = R.drawable.notification)
          //  MenuItemWithDivider(text = "Hesap Ekleme", icon = R.drawable.bill)
            MenuItemWithDivider(text = "Profil Düzenleme", icon = R.drawable.edit)
            MenuItemWithDivider(
                text = "Rozetler",
                onClick = { navController.navigate("BadgesScreen") },
                icon = R.drawable.cup
            )
            MenuItemWithDivider(text = "Rütbe Artma", icon = R.drawable.rank , onClick = {
                navController.navigate("AchievementScreen")
            })
            MenuItemWithDivider(text = "Kurallar", icon = R.drawable.rules)
            MenuItemWithDivider(
                text = "Hesap Kapatma",
                onClick = { showDeleteConfirmDialog = true },
                icon = R.drawable.close
            )
            MenuItemWithDivider(
                text = "Çıkış Yap",
                onClick = { showSignOutConfirmDialog = true },
                icon = R.drawable.exit
            )
        }
    }

    // Hesap Silme Dialog'u
    if (showDeleteConfirmDialog) {
        val showPasswordDialog by registerViewModel.showPasswordDialog.collectAsState()
        val tempPassword by registerViewModel.tempPassword.collectAsState()
        
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { registerViewModel.hidePasswordConfirmDialog() },
                title = { 
                    Text(
                        text = "Şifrenizi Giriniz",
                        style = MaterialTheme.typography.h6
                    )
                },
                text = {
                    OutlinedTextField(
                        value = tempPassword,
                        onValueChange = { registerViewModel.updateTempPassword(it) },
                        label = { Text("Şifre") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            registerViewModel.deleteAccount(context)
                        }
                    ) {
                        Text("Onayla")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { registerViewModel.hidePasswordConfirmDialog() }
                    ) {
                        Text("İptal")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { 
                    Text(
                        text = "Hesap Silme Onayı",
                        style = MaterialTheme.typography.h6
                    )
                },
                text = { 
                    Text(
                        text = "Hesabınızı silmek istediğinizden emin misiniz?\nBu işlem geri alınamaz!",
                        style = MaterialTheme.typography.body1
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            registerViewModel.showPasswordConfirmDialog()
                        }
                    ) {
                        Text("Evet, Sil")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteConfirmDialog = false }
                    ) {
                        Text("İptal")
                    }
                }
            )
        }
    }

    // Çıkış Yapma Dialog'u
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = {
                Text(
                    text = "Çıkış Onayı",
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = "Çıkış yapmak istediğinizden emin misiniz?",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        registerViewModel.signOut(context = context)
                        showSignOutConfirmDialog = false
                    }
                ) {
                    Text("Evet, Çık")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSignOutConfirmDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun MenuItemWithDivider(
    text: String,
    onClick: () -> Unit = {},
    icon: Int
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = colorResource(R.color.kutubordrengi),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface
            )
        }
        Divider(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp
        )
    }
}



fun getNextRankInfo(currentPoints: Int): NextRankInfo {
    return when (currentPoints) {
        in 0..99 -> NextRankInfo("Başlangıç", 100, 100 - currentPoints)
        in 100..299 -> NextRankInfo("Çaylak", 300, 300 - currentPoints)
        in 300..599 -> NextRankInfo("Disiplinli", 600, 600 - currentPoints)
        in 600..999 -> NextRankInfo("Kararlı", 1000, 1000 - currentPoints)
        in 1000..1599 -> NextRankInfo("Usta", 1600, 1600 - currentPoints)
        in 1600..2299 -> NextRankInfo("Bilge", 2300, 2300 - currentPoints)
        in 2300..3199 -> NextRankInfo("Efsane", 3200, 3200 - currentPoints)
        else -> NextRankInfo("Maksimum", currentPoints, 0)
    }
}

fun calculateProgress(currentPoints: Int, nextRankInfo: NextRankInfo): Float {
    if (nextRankInfo.nextRank == "Maksimum") return 1f
    
    val currentRankMinPoints = when (currentPoints) {
        in 0..99 -> 0
        in 100..299 -> 100
        in 300..599 -> 300
        in 600..999 -> 600
        in 1000..1599 -> 1000
        in 1600..2299 -> 1600
        in 2300..3199 -> 2300
        else -> 3200
    }
    
    val pointsInCurrentRank = currentPoints - currentRankMinPoints
    val totalPointsNeededForNextRank = nextRankInfo.nextRankMinPoints - currentRankMinPoints
    
    return pointsInCurrentRank.toFloat() / totalPointsNeededForNextRank.toFloat()
}