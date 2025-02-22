package com.example.goalmate.prenstatntion.ExerciseAdd

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goalmate.data.localdata.Habit
import com.example.goalmate.viewmodel.HabitViewModel
import com.example.yeniproje.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddExerciseAddScreen(
    navController: NavController,
    habitViewModel: HabitViewModel,
    isGroup: Boolean
) {
    // State değişkenleri
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Günlük") }
    var isPrivate by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<Int?>(null) }
    var selectedColor by remember {mutableStateOf<Int?>(null)}// Seçilen renk
    var showIconPicker by remember { mutableStateOf(false) }
    val habitsCount  = habitViewModel.countActiveHabits.collectAsState().value
    Log.e("habitsCount","habitsCount . $habitsCount")

    val startDate by habitViewModel.currentTime.collectAsState()
    Log.e("startDateget","startDate : $startDate")

    val context = LocalContext.current
    var toastNumber by remember { mutableIntStateOf(0) }

    Log.e("isGroup",isGroup.toString())
    val groupOrNormal = if (isGroup) "group" else "normal"


    val colorList = listOf(
        R.color.egzersizrengi, R.color.okumarengi, R.color.calismarengi, R.color.suicmerengi, R.color.dogarengi, R.color.meditasyonrengi
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(R.color.beyaz)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp)) // Yukarıdan biraz boşluk bırakıyoruz
            // Geri tuşu
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = "geri tuşu",
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(35.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simge Seçim Alanı
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(color = colorResource(R.color.arkaplan)),
                contentAlignment =Alignment.CenterStart,
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                colorResource(
                                    id = selectedColor ?: R.color.dogarengi
                                )
                            )
                            .clickable {
                                showIconPicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedIcon != null) {
                            Icon(
                                painter = painterResource(id = selectedIcon!!),
                                tint = Color.Black,
                                contentDescription = "Seçilen İkon",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.habits),
                                contentDescription = "Default Icon",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    // "İkon Seç" metni
                    Text(
                        text = "Tıkla ikon Seç",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight(600),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            if (showIconPicker){
                IconPickerScreen(
                    onIconSelected = { selectedIcon = it },
                    onDismissRequest = { showIconPicker = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            if (showIconPicker){

                // hiç bişi gözükmüyecek

            }else{
                // Renk Seçim Alanı
                Text(
                    text = "Renk Seç",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorList.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(color = colorResource(color))
                                .clickable {
                                    selectedColor = color
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedIcon == color) {
                                Icon(
                                    painter = painterResource(R.drawable.taget2),
                                    contentDescription = "Seçilen Renk",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alışkanlık Adı
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 22) {
                            name = it
                        } else {
                            if (toastNumber < 3) {
                                Toast.makeText(context, "Sadece 20 karakter girebilirsiniz!", Toast.LENGTH_SHORT).show()
                                toastNumber += 1
                            }
                        }
                    },
                    label = { Text("Alışkanlık Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frekans Seçimi
                FrequencySelection(frequency = frequency) { frequency = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Gizlilik Seçimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PrivacyButton(isPrivate = !isPrivate, onClick = { isPrivate = false }, label = "Herkese Açık")
                    PrivacyButton(isPrivate = isPrivate, onClick = { isPrivate = true }, label = "Gizli")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Süre Girişi
                ExerciseDurationInput(
                    hours = hours,
                    minutes = minutes,
                    onHoursChange = { hours = it },
                    onMinutesChange = { minutes = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Kaydet Butonu
                Button(
                    onClick = {
                        if (name.isNotEmpty() && hours.isNotEmpty() && minutes.isNotEmpty()) {
                            val startDateLong = startDate
                            Log.d("StartDate", "Start Date: ${formatter(startDateLong)}")
                            val finishDateLong = finishDate(startDateLong,frequency)
                            Log.d("FinishDate", "Finish Date: ${formatter(finishDateLong)}")

                           time =  formatTime(hours.toInt(),minutes.toInt())

                            val habit = Habit(
                                name = name,
                                frequency = frequency,
                                isPrivate = isPrivate,
                                time = time,
                                startDate = startDateLong,
                                finishDate = finishDateLong,
                                lastCompletedDate = System.currentTimeMillis() / (1000 * 60 * 60 * 24),
                                iconResId = selectedIcon,
                                habitType = groupOrNormal,
                                colorResId = selectedColor,
                                isExpired = false
                            )

                            habitViewModel.addExercise(habit)

                            if (habitsCount <= 5){
                                Toast.makeText(context, "$name başarıyla eklendi", Toast.LENGTH_SHORT).show()
                            }
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.kutubordrengi))
                ) {
                    Text("Ekle")
                }
            }
        }
    }
}


@Composable
fun FrequencySelection(frequency: String, onFrequencyChange: (String) -> Unit) {
    Column {
        Text(
            text = "Alışkanlık Sıklığı",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Günlük", "Haftalık", "Aylık").forEach { option ->
                RadioButtonWithText(
                    selected = frequency == option,
                    onClick = { onFrequencyChange(option) },
                    label = option
                )
            }
        }
    }
}

@Composable
fun ExerciseDurationInput(
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Alışkanlık Süresi (Saat:Dakika)",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = hours,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.toIntOrNull() in 0..23) onHoursChange(it) },
                label = { Text("Saat") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = minutes,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.toIntOrNull() in 0..59) onMinutesChange(it) },
                label = { Text("Dakika") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PrivacyButton(isPrivate: Boolean, onClick: () -> Unit, label: String) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = 150.dp, height = 50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrivate) colorResource(R.color.kutubordrengi) else MaterialTheme.colorScheme.secondary
        )


    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
    }
}

@Composable
fun RadioButtonWithText(selected: Boolean, onClick: () -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = colorResource(R.color.kutubordrengi))
        )
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}



@SuppressLint("DefaultLocale")
fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}



@RequiresApi(Build.VERSION_CODES.O)
fun formatter(date: Long): String {
    // Long zaman damgasını Instant nesnesine dönüştür
    val instant = Instant.ofEpochMilli(date)

    // DateTimeFormatter ile biçimlendirme
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

    // Biçimlendirilmiş tarihi döndür
    return formatter.format(instant)
}





    @RequiresApi(Build.VERSION_CODES.O)
    fun finishDate(startDate: Long, frequency: String): Long {
        val startZoned = Instant.ofEpochMilli(startDate).atZone(ZoneOffset.UTC)
        return when (frequency) {
            "Günlük" -> startZoned.plusDays(1).toInstant().toEpochMilli()
            "Haftalık" -> startZoned.plusWeeks(1).toInstant().toEpochMilli()
            "Aylık" -> startZoned.plusMonths(1).toInstant().toEpochMilli()
            else -> startDate // Geçersiz frekansta başlangıç tarihi döndür
        }
    }

