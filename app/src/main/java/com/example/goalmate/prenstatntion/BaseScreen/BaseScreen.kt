package com.example.goalmate.prenstatntion.BaseScreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun  BaseScreen(content: @Composable () -> Unit) {

    val systemUiController = rememberSystemUiController()

    // Durum çubuğunun rengini belirle
    LaunchedEffect (Unit) {
        systemUiController.setSystemBarsColor(
            color = Color(0xFFFDFBF9), // Durum çubuğu rengini buradan değiştirebilirsiniz
            darkIcons = true // Yazı rengini siyah yapmak için 'true' (koyu metin)
        )
    }

    // MaterialTheme kullanarak yazı rengini siyah yapıyoruz
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(onSurface = Color.Black)
    ) {
        content() // İçerik render ediliyor
    }
}


