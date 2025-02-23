package com.example.goalmate.prenstatntion.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.goalmate.R

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.sariarka)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo6),
                contentDescription = "Welcome Image",
                modifier = Modifier
                    .size(250.dp)
                    .padding(top = 48.dp, bottom = 20.dp)
            )



            Image(
                painter = painterResource(id = R.drawable.resim1),
                contentDescription = "Welcome Image",
                modifier = Modifier
                    .size(350.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
            )

            Text(
                text = "Her büyük başarı, küçük adımlarla başlar. Hemen başlayın ve alışkanlıklarınızı değiştirin!",
                fontWeight = FontWeight(600),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.yazirengi),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { 
                    navController.navigate("register_screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.yazirengi)
                )
            ) {
                Text("Hesap Oluştur")
            }

            OutlinedButton(
                onClick = {
                    navController.navigate("LoginScreen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.yazirengi)
                )
            ) {
                Text("Giriş Yap")
            }
        }
    }
} 