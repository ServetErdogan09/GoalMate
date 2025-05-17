package com.example.goalmate.prenstatntion.verification

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.goalmate.viewmodel.RegisterViewModel
import com.example.goalmate.viewmodel.VerificationState
import com.example.goalmate.R
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.goalmate.data.AuthState
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel(),
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    context: Context
) {
    var isVerifying by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()
    var hasAttemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            try {
                auth.currentUser?.reload()?.await()
                val user = auth.currentUser
                if (user?.isEmailVerified == true && !hasAttemptedSave) {
                    isVerifying = true
                    hasAttemptedSave = true
                    Log.d("VerificationScreen", "Email verified, saving user data...")
                    
                    // Kullanıcı verilerini kaydet
                    viewModel.saveUserToFirestore(user.uid, context)
                }
                
                // AuthState'i kontrol et
                when (authState) {
                    is AuthState.ProfileRequired -> {
                        Log.d("VerificationScreen", "Navigation to ProfileScreen")
                        navController.navigate("ProfileScreen") {
                            popUpTo("verification") { inclusive = true }
                        }
                        break
                    }
                    is AuthState.Error -> {
                        Log.e("VerificationScreen", "Error: ${(authState as AuthState.Error).message}")
                        isVerifying = false
                        hasAttemptedSave = false
                    }
                    else -> {
                        Log.d("VerificationScreen", "Waiting for data save completion...")
                    }
                }
            } catch (e: Exception) {
                Log.e("VerificationScreen", "Error checking verification status", e)
                isVerifying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.email),
            contentDescription = "Email Verification",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            "Email Doğrulama",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            "Email adresinize gönderilen doğrulama linkine tıklayın.\nDoğrulama sonrası otomatik olarak yönlendirileceksiniz.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isVerifying) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
            Text(
                "Doğrulama işlemi devam ediyor...",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = { 
                auth.currentUser?.sendEmailVerification()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Doğrulama Mailini Tekrar Gönder")
        }

        // Hata durumunu göster
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
} 