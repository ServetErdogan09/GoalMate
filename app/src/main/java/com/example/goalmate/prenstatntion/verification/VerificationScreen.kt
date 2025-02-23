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

@Composable
fun VerificationScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel(),
    auth: FirebaseAuth,
    context: Context
) {
    val verificationState by viewModel.verificationState.collectAsState()

    // Periyodik olarak email doğrulama durumunu kontrol et
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // 2 saniyede bir kontrol et
            auth.currentUser?.reload()
            if (auth.currentUser?.isEmailVerified == true) {
                viewModel.saveUserDataAfterVerification(context) // Email doğrulandığında bilgileri kaydet
                break
            }
        }
    }

    // Başarılı doğrulama durumunda yönlendir
    LaunchedEffect(verificationState) {
        if (verificationState is VerificationState.Success) {
            navController.navigate("HomeScreen") {
                popUpTo("LoginScreen") { inclusive = true }
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
            painter = painterResource(id = R.drawable.email_verification_image),
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

        when (verificationState) {
            is VerificationState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
            is VerificationState.Error -> {
                Text(
                    (verificationState as VerificationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            else -> {}
        }

        Button(
            onClick = { viewModel.resendVerificationCode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Doğrulama Mailini Tekrar Gönder")
        }
    }
} 