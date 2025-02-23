package com.example.goalmate.prenstatntion.ExerciseAdd

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.ui.text.font.FontWeight
import com.example.goalmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(
    onIconSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val categories = listOf("Egzersiz", "Hobi", "Finans",
        "Eğitim", "Sosyal", "Doğa", "Seyahat", "Hayvanlar"
    )

    val iconsByCategory = mapOf(
        "Egzersiz" to listOf(
            R.drawable.exercise1, R.drawable.exercise2,R.drawable.exercise3,R.drawable.exercise4,
            R.drawable.exercise5,R.drawable.exercise6,R.drawable.exercise7,R.drawable.exercise8,
            R.drawable.exercise9,R.drawable.exercise10,R.drawable.exercise11,R.drawable.exercise12,
         ),
        "Hobi" to listOf(
            R.drawable.hobi1, R.drawable.hobi2, R.drawable.hobi3,R.drawable.hobi4,
            R.drawable.hobi5,R.drawable.hobi6,R.drawable.hobi7, R.drawable.hobi8,
            R.drawable.hobi9,R.drawable.hobi10, R.drawable.hobi11,R.drawable.hobi12
        ),
        "Finans" to listOf(
            R.drawable.finance1, R.drawable.finance2, R.drawable.finance3,R.drawable.finance4,
            R.drawable.finance5,R.drawable.finance6,R.drawable.finance7,R.drawable.finance8,
            R.drawable.finance9,R.drawable.finance10,R.drawable.finance11,R.drawable.finance12
        ),
        "Eğitim" to listOf(
            R.drawable.education1, R.drawable.education2, R.drawable.education3,
            R.drawable.education4, R.drawable.education5, R.drawable.education6,
            R.drawable.education7, R.drawable.education8, R.drawable.education9,
            R.drawable.education10, R.drawable.education11, R.drawable.education12,
        ),
        "Sosyal" to listOf(
            R.drawable.social1, R.drawable.social2, R.drawable.social3,R.drawable.social4,
            R.drawable.social5, R.drawable.social6, R.drawable.social7,R.drawable.social8,
            R.drawable.social9, R.drawable.social10, R.drawable.social11,R.drawable.social12
        ),
        "Doğa" to listOf(
            R.drawable.nature1, R.drawable.nature2, R.drawable.nature3, R.drawable.nature4,
            R.drawable.nature5, R.drawable.nature6, R.drawable.nature7, R.drawable.nature8,
            R.drawable.nature9, R.drawable.nature10, R.drawable.nature11, R.drawable.nature12,
        ),
        "Seyahat" to listOf(
            R.drawable.travel1, R.drawable.travel2, R.drawable.travel3, R.drawable.travel4,
            R.drawable.travel5, R.drawable.travel6, R.drawable.travel7, R.drawable.travel8,
            R.drawable.travel9, R.drawable.travel10, R.drawable.travel11, R.drawable.travel12,
        ),
        "Hayvanlar" to listOf(
            R.drawable.animal1, R.drawable.animal2, R.drawable.animal3, R.drawable.animal4,
            R.drawable.animal5, R.drawable.animal6, R.drawable.animal7, R.drawable.animal8,
        )
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val bottomSheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetContainerColor = colorResource(R.color.arkaplan),
        contentColor = colorResource(R.color.ilerleme_çubuğu),
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp) // Ekstra padding ekledik
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Kategoriler arasında geçiş için LazyRow
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        items(categories) { category ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clickable { selectedCategory = category }
                                    .padding(12.dp)
                                    .background(
                                        color = if (selectedCategory == category) colorResource(R.color.kutu_rengi) else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium) // Butonlar için yuvarlak köşeler
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), // Bold yazı stili
                                    color = if (selectedCategory == category) Color.White else colorResource(R.color.yazirengi),
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Seçilen kategorinin ikonları
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp), // İkonlar arasındaki mesafeyi artırdık
                        verticalArrangement = Arrangement.spacedBy(16.dp), // Dikey mesafe
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        iconsByCategory[selectedCategory]?.let { iconList ->
                            items(iconList.size) { index ->
                                val iconResId = iconList[index]
                                IconButton(
                                    onClick = {
                                        onIconSelected(iconResId)
                                        onDismissRequest() // İkon seçildikten sonra ekran kapanır
                                    },
                                    modifier = Modifier
                                        .size(56.dp) // İkon boyutunu daha belirgin yapalım
                                        .padding(8.dp)
                                        .background(
                                            color = colorResource(R.color.acikgri),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    Image(
                                        painter = painterResource(id = iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Kaydet ve İptal butonları
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onDismissRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text("İptal", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { onDismissRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.kutubordrengi)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text("Kaydet", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        },
        sheetPeekHeight = 400.dp, // Yüksekliği artırdık
        modifier = Modifier.fillMaxSize()
    ){

    }
}