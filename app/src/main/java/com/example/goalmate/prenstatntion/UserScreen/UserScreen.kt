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

@Composable
fun UserScreen(
    registerViewModel: RegisterViewModel,
    navController: NavController,
    context: Context
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }



    // AuthState'i gözlemle
    val authState by registerViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Idle -> {
                navController.navigate("WelcomeScreen") {
                    popUpTo("UserScreen") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hata mesajı gösterimi
        when (val state = authState) {
            is AuthState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.body1
                )
            }
            else -> {}
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hesap Silme Butonu
            Button(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(
                    text = "Hesap Sil",
                    style = MaterialTheme.typography.button
                )
            }

            // Çıkış Yapma Butonu
            Button(
                onClick = { showSignOutConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(
                    text = "Çıkış Yap",
                    style = MaterialTheme.typography.button
                )
            }
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
                        registerViewModel.signOut()
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