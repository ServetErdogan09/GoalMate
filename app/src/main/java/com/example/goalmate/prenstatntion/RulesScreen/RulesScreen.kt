package com.example.goalmate.prenstatntion.RulesScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.goalmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Geri Dön"
                        )
                    }
                },
                title = { Text("") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "🎯 KURALLAR",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "1. 📅 Gruptan Çıkış\n" +
                        "- İlk 24 saat içinde çıkarsan puan kaybı olmaz.\n" +
                        "- 24 saatten sonra çıkarsan alışkanlık süresine göre puan kesilir:\n" +
                        "   • Haftalık gruplar: -100 puan\n" +
                        "   • Aylık gruplar: -300 puan\n" +
                        "- Günlük gruplarda çıkış yapılamaz.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "2. 🚫 Uygunsuz Dil ve Davranış\n" +
                        "- Küfür, hakaret veya +18 içerikler yasaktır.\n" +
                        "- Sistem bu mesajları otomatik olarak sansürleyebilir.\n" +
                        "- Gerekirse grup yöneticisi kullanıcıyı gruptan çıkarabilir.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "3. 🏆 Puan Sistemi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Alışkanlığını tamamladığında aşağıdaki puanları kazanırsın:")
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text("📌 Günlük → 10 puan")
                Text("📌 Haftalık → 70 puan")
                Text("📌 Aylık → 300 puan")
                Text("🎁 Tüm grup tamamladıysa → Bonus puan!")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "4. 🤝 Topluluk Davranışları\n" +
                        "- Saygılı, destekleyici ve motive edici bir ortam önemlidir.\n" +
                        "- Ortak hedef: birlikte gelişmek ve alışkanlıkları sürdürülebilir kılmak.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}