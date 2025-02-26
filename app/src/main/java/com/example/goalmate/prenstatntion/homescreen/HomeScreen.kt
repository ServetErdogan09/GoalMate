package com.example.goalmate.prenstatntion.homescreen

import android.annotation.SuppressLint
import android.content.Context
import android.media.SoundPool
import android.os.Build
import android.util.Log
import androidx.compose.material3.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.goalmate.data.localdata.CompletedDay
import com.example.goalmate.data.localdata.Habit
import com.example.goalmate.data.localdata.HabitHistory
import com.example.goalmate.extrensions.ExerciseUiState
import com.example.goalmate.prenstatntion.AnalysisScreen.remainingDays
import com.example.goalmate.prenstatntion.AnalysisScreen.totalHabit
import com.example.goalmate.prenstatntion.ExerciseAdd.finishDate
import com.example.goalmate.viewmodel.CompleteDayViewModel
import com.example.goalmate.viewmodel.HabitViewModel
import com.example.goalmate.viewmodel.StarCoinViewModel
import com.example.goalmate.R
import com.example.goalmate.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.derivedStateOf
import coil.compose.rememberAsyncImagePainter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HabitViewModel = viewModel(),
    starCoinViewModel: StarCoinViewModel = viewModel(),
    completeDayViewModel: CompleteDayViewModel,
    registerViewModel: RegisterViewModel = viewModel(),
    context: Context
) {

    val uiState by viewModel.uiState.collectAsState()
    val habits = remember(uiState) {
        when (uiState) {
            is ExerciseUiState.Success -> (uiState as ExerciseUiState.Success).habits
            else -> emptyList()
        }
    }

    val userName by registerViewModel.userName.collectAsState()
    val profileImage by registerViewModel.profileImage.collectAsState()


    Log.e("userName","profileImage : $profileImage")
    Log.e("userName","userName : $userName")

    var starIconPosition by remember { mutableStateOf(Offset.Zero) }

    val currentTime by viewModel.currentTime.collectAsState()

    val totalHabits by viewModel.countActiveHabits.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getCountActiveHabit()
    }

    LaunchedEffect(totalHabits) {
        Log.d("HomeScreen", """
            Toplam Alışkanlık Sayısı Güncellendi
            Yeni Toplam: $totalHabits
            ----------------------
        """.trimIndent())
    }

    val completedHabits = habits.count { it.isCompleted }
    val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    val isChecked = viewModel.isChecked.collectAsState().value
    var showExplosion by remember { mutableStateOf(false) }

    val totalHabitsPoint by starCoinViewModel.starTotalPoints.collectAsState() // toplam yıldız sayısı

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { viewModel.resetHabitsForNewDay() }
            launch { viewModel.getExercises() }
        }
    }

    LaunchedEffect(isChecked) {
        if (isChecked) {
            showExplosion = true
            delay(5000)
            showExplosion = false
            starCoinViewModel.getTotalStarPoints()
            Log.e("HomeScreen", "Star points requested")
            viewModel.onCheckboxClickedFalse()
        }
    }

    // UI State'i dinle ve değişikliklerde yeniden yükle
    LaunchedEffect(uiState) {
        when (uiState) {
            is ExerciseUiState.Success -> {
                viewModel.getExercises()
                viewModel.getCountActiveHabit()
            }
            else -> {}
        }
    }

    // LazyListState'i tanımla
    val lazyListState = rememberLazyListState()
    

    val isScrollingUp = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 || 
            lazyListState.firstVisibleItemScrollOffset == 0 ||
            lazyListState.isScrollInProgress.not()
        }
    }

    // Ekran açıldığında profil verilerini yenile
    LaunchedEffect(Unit) {
        registerViewModel.getCurrentUser(context)
    }

    // Profil resmi değiştiğinde log atalım
    LaunchedEffect(profileImage) {
        Log.d("HomeScreen", "Profil resmi güncellendi: $profileImage")
    }

    Scaffold(
        containerColor = colorResource(R.color.arkaplan),
        bottomBar = {
            AnimatedVisibility(
                visible = isScrollingUp.value,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {

            }
        }
    ) { innerPadding ->

        Box(Modifier.fillMaxSize()){

            viewModel.starAnimations.forEach { startPosition ->
                AnimatedStarsSequence(
                    startPosition = startPosition,
                    endPosition = starIconPosition,
                    onAnimationEnd = { viewModel.removeStarAnimation(startPosition) }
                )
            }

            if (showExplosion) {
                PatlayanAnimasyon(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Yıldız ikonunun olduğu bölgeye hizala
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp)
                    ) {
                        // Profil Resmi ve Hoş Geldin Mesajı
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Profil Resmi
                            Image(
                                painter = when {
                                    profileImage.isNotEmpty() -> {
                                        when {
                                            profileImage.startsWith("http") || profileImage.startsWith("content") -> {
                                                rememberAsyncImagePainter(
                                                    model = profileImage,
                                                    error = painterResource(R.drawable.personel)
                                                )
                                            }
                                            else -> {
                                                // Resource ID kontrolünü Composable dışında yapıyoruz
                                                painterResource(getProfilePainter(profileImage, R.drawable.personel))
                                            }
                                        }
                                    }
                                    else -> painterResource(R.drawable.personel)
                                },
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.yazirengi), CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp)) // Resim ile metin arasına boşluk

                            // Hoş Geldin Mesajı
                            Column {
                                Text(
                                    text = userName,
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Hoş Geldin",
                                    color = colorResource(R.color.yazirengi),
                                    fontSize = 14.sp
                                )
                            }

                        }

                        Spacer(modifier = Modifier.width(40.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth() // Tam genişlik kullanımı
                                .padding(top = 15.dp, bottom = 15.dp)
                        ) {
                            // Puan Bölümü
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End, // Sağda hizalama
                                modifier = Modifier.weight(1f) // Puanı sağa it
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.starsolid), // Yıldız ikonu
                                    contentDescription = "yıldız",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .onGloballyPositioned { coordinates ->
                                            // Merkez pozisyonunu hesapla
                                            starIconPosition = coordinates
                                                .positionInRoot()
                                                .let {
                                                    Offset(
                                                        it.x + coordinates.size.width / 2,
                                                        it.y + coordinates.size.height / 2
                                                    )
                                                }
                                        }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Log.e("starTotalPoints", "starTotalPoints: $totalHabitsPoint")
                                Text(
                                    text = "${totalHabitsPoint ?:0}",
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold,
                                    overflow = TextOverflow.Visible,
                                    maxLines = 1,
                                    textAlign = TextAlign.Start
                                )
                            }

                            Spacer(modifier = Modifier.width(40.dp))

                            // Coin Bölümü
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End, // Sağda hizalama
                                modifier = Modifier.weight(1f) // Coin'i de sağa it
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sackdollar), // Coin ikonu
                                    contentDescription = "Coin",
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "0", // Coin değeri
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.width(80.dp), // Sabit genişlik
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Motive edici söz, alışkanlık durumu ve ilerleme çubuğu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp, end = 5.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(colorResource(R.color.beyaz))
                            .border(
                                width = 2.dp,
                                color = colorResource(R.color.yazirengi),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .verticalScroll(rememberScrollState())  // İçeriği kaydırılabilir hale getir
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp) // İçerik boşlukları
                        ) {
                            Text(
                                text = "“Her gün kendini geliştirmek için bir fırsattır.”",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 16.sp,
                                color = colorResource(R.color.yazirengi),
                                fontWeight = FontWeight(700),
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$completedHabits/$totalHabits Alışkanlık Tamamlandı",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorResource(R.color.yazirengi),
                                fontWeight = FontWeight(500),
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()  // Satırın tamamını doldurur
                                    .height(20.dp)   // Gerekirse satırın yüksekliğini ayarlayabilirsiniz
                            ) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)  // LinearProgressIndicator genişliği ekranın tamamını kapsar
                                        .clip(RoundedCornerShape(5.dp))
                                        .height(12.dp),  // Yüksekliği 12.dp olarak artırdık
                                    color = colorResource(R.color.yazirengi),
                                )

                                Spacer(modifier = Modifier.width(10.dp)) // LinearProgressIndicator ile Text arasında boşluk

                                Text(
                                    text = "${"%.0f".format(progress * 100)}%",
                                    color = colorResource(R.color.yazirengi),
                                    textAlign = TextAlign.Start, // Başlangıç hizasında
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)  // Dikeyde ortalamayı sağlar
                                        .padding(start = 8.dp) // Biraz boşluk ekleyebilirsiniz
                                )
                            }
                        }

                        // Alt çizgiyi Box'ın dışında bir başka katman olarak ekliyoruz
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp) // Alt çizginin kalınlığı
                                .background(colorResource(R.color.yazirengi)) // Alt çizginin rengi
                                .align(Alignment.BottomCenter) // Altta hizalama
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (uiState) {
                    is ExerciseUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    is ExerciseUiState.Error -> {
                        val errorMessage = (uiState as ExerciseUiState.Error).message
                        Log.e("errorMessage", errorMessage)
                    }
                    is ExerciseUiState.Success -> {
                        if (habits.isNotEmpty()) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Bugünkü alışkanlığını tamamla ve 10",
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.yazirengi)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Image(
                                    painter = painterResource(R.drawable.starsolid),
                                    contentDescription = "Star Icon",
                                    modifier = Modifier.size(30.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = "yıldız kazan!",
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.yazirengi)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 8.dp))
                                    .background(colorResource(R.color.beyaz))
                                    .padding(12.dp)
                            ) {
                                itemsIndexed(
                                    items = habits,
                                    key = { _, habit -> habit.id }
                                ) { _, habit ->
                                    HabitCard(habit = habit, viewModel = viewModel, navController = navController, completeDayViewModel = completeDayViewModel, starCoinViewModel = starCoinViewModel, currentTime = currentTime)
                                    Log.e("habit", "Habit: $habit")

                                }
                            }


                        } else {
                            Text(
                                text = "Henüz alışkanlık eklenmemiş.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {}
                }
            }
        }

    }
}



@RequiresApi(Build.VERSION_CODES.O)
fun isCompletionButtonEnabled(lastCompletedDate: Long?, remainingDays: Long): Boolean {
    if (remainingDays <= 0) {
        Log.d("ButtonControl", "Alışkanlık süresi dolmuş")
        return false
    }

    try {
        val istanbulZone = ZoneId.of("Europe/Istanbul")

        val today = LocalDate.now(istanbulZone)

        // Daha önce tamamlanmadıysa (lastCompletedDate == null), buton aktif olur
        if (lastCompletedDate == null) {
            Log.d("ButtonControl", "Alışkanlık hiç tamamlanmış, tamamlanabilir.")
            return true
        }

        val lastDate = Instant.ofEpochMilli(lastCompletedDate)
            .atZone(istanbulZone)
            .toLocalDate()

        // Eğer son tamamlanma tarihi bugüne eşitse, tekrar tamamlamaya izin verme
        if (lastDate.isEqual(today)) {
            Log.d("ButtonControl", "Bugün zaten tamamlanmış, tekrar tamamlanamaz.")
            return false
        }

        Log.d("ButtonControl", "Tamamlamaya izin var.")
        return true
    } catch (e: Exception) {
        Log.e("ButtonControl", "Error: ${e.message}")
        return false
    }
}


@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitCard(
    navController: NavController,
    habit: Habit,
    viewModel: HabitViewModel,
    completeDayViewModel: CompleteDayViewModel,
    starCoinViewModel: StarCoinViewModel,
    currentTime: Long
) {
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var checkboxPosition by remember { mutableStateOf(Offset.Zero) }

    val remainingDaysMap = viewModel.habitRemainingDaysMap.collectAsState().value
    val remainingDays = remainingDaysMap[habit.id] ?: 0L

    var startDate by remember { mutableStateOf(0L) }
    startDate = currentTime

    val finishDate by remember { mutableStateOf(finishDate(startDate,habit.frequency)) }




    LaunchedEffect(Unit) {
        viewModel.calculateRemainingDays(habit,currentTime)
    }


    var isDialogOpen by remember { mutableStateOf(false) }


    val isButtonEnabled by remember(habit.lastCompletedDate) {
        derivedStateOf {
            isCompletionButtonEnabled(habit.lastCompletedDate,remainingDays)
        }
    }


    var isChecked by remember(habit.isCompleted) {
        mutableStateOf(habit.isCompleted)
    }

    val today = LocalDate.now().toEpochDay()
    Log.e("today", "Bugün: $today")
    val completedDay = CompletedDay(habitId = habit.id , date = today, isCompleted = habit.isCompleted)

    LaunchedEffect(habit.id) {
        completeDayViewModel.getCompletedDayByDate(completedDay.habitId, completedDay.date, completedDay.isCompleted)
    }

    val dismissState = rememberDismissState { dismissValue ->
        if (dismissValue == DismissValue.DismissedToEnd) {
            showDialog.value = true
        }
        false
    }

    Spacer(modifier = Modifier.height(5.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, colorResource(R.color.yazirengi), shape = RoundedCornerShape(10.dp))
            .padding(10.dp)
            .clickable {
                navController.navigate("AnalysisScreen/${habit.id}")
            }
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.arkaplan)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.arkaplan)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            },
            dismissContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = if (habit.colorResId == null) colorResource(R.color.dogarengi) else colorResource(
                                    habit.colorResId
                                ),
                                shape = CircleShape
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.iconResId == null) {
                            Icon(
                                painter = painterResource(R.drawable.habits),
                                contentDescription = "icon",
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(habit.iconResId),
                                contentDescription = "icon",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            text = habit.name,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.yazirengi),
                            fontFamily = FontFamily(Font(R.font.noto_regular)),
                            fontSize = 18.sp,
                            style = if (habit.isCompleted || remainingDays <= 0) {
                                TextStyle(textDecoration = TextDecoration.LineThrough)
                            } else {
                                TextStyle.Default
                            }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.zaman),
                                contentDescription = "icon",
                                modifier = Modifier
                                    .size(15.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(
                                text = habit.frequency,
                                fontWeight = FontWeight(550),
                                color = colorResource(R.color.yazirengi),
                                fontFamily = FontFamily(Font(R.font.noto_regular)),
                                fontSize = 15.sp,
                            )


                            Text(
                                text = if (remainingDays <= 0) " - Alışkanlık bitti!" else " - $remainingDays gün kaldı",
                                fontWeight = FontWeight(550),
                                color = colorResource(R.color.yazirengi),
                                fontFamily = FontFamily(Font(R.font.noto_regular)),
                                fontSize = 15.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Box(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .fillMaxSize() // Ekranın tamamını kapla
                            .onGloballyPositioned { coordinates ->
                                val centerX =
                                    coordinates.positionInWindow().x + coordinates.size.width / 2
                                val centerY =
                                    coordinates.positionInWindow().y + coordinates.size.height / 2
                                checkboxPosition = Offset(centerX, centerY)
                            }
                    ) {
                        Checkbox(
                            checked = habit.lastCompletedDate?.let { lastCompletedDate ->
                                val today = LocalDate.now(ZoneId.of("Europe/Istanbul"))
                                val lastDate = Instant.ofEpochMilli(lastCompletedDate)
                                    .atZone(ZoneId.of("Europe/Istanbul"))
                                    .toLocalDate()

                                lastDate.isEqual(today)  // Eğer bugün tamamlandıysa checked = true olur
                            } ?: false,  // Eğer hiç tamamlanmadıysa false olur
                            onCheckedChange = { checked ->
                                if (remainingDays <= 0) {
                                    isDialogOpen = true
                                    return@Checkbox
                                }

                                val canComplete = isCompletionButtonEnabled(habit.lastCompletedDate, remainingDays)
                                Log.d("Checkbox", "Can complete: $canComplete")

                                if (canComplete) {
                                    viewModel.addStarAnimation(checkboxPosition)
                                    viewModel.updateHabitCompletion(habit)  // Alışkanlığı tamamlandı olarak güncelle
                                    playSound(R.raw.patlama, context)
                                    viewModel.onCheckboxClickedTrue()
                                    completeDayViewModel.updateCompletedDays(
                                        habitId = habit.id,
                                        date = System.currentTimeMillis(), // Güncellenen tarihi kaydet
                                        completed = true
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Bu alışkanlığı bugün zaten tamamladınız. Yarın tekrar deneyebilirsiniz.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(R.color.kutubordrengi),
                                uncheckedColor = colorResource(R.color.yazirengi),
                                checkmarkColor = Color.White,
                                disabledCheckedColor = colorResource(R.color.kutubordrengi).copy(alpha = 0.5f),
                                disabledUncheckedColor = colorResource(R.color.yazirengi).copy(alpha = 0.5f)
                            )
                        )
                    }
                }

            }

        )

    }
    Log.e("isDialogOpen", "isDialogOpen : $isDialogOpen")

    if (isDialogOpen) {
        val sumDay = sumDay(startMillis = habit.startDate, finishMillis = habit.finishDate)
        HabitCompletionDialog(
            habitName = habit.name,
            completionStatus = completionStatus(startMillis = habit.startDate, finishMillis = habit.finishDate, completedDays = habit.completedDays),
            progress = calculateProgress(completedDays = habit.completedDays, finishMillis = habit.finishDate, startMillis = habit.startDate),
            targetInfo = " Hedef: $sumDay Gün - Tamamlanan: ${habit.completedDays} Gün",
            onDismiss = { isDialogOpen = false },
            frequency = habit.frequency,
            onReset = {
                val updateHabit = habit.copy(
                    startDate = startDate,
                    finishDate = finishDate,
                    completedDays = 0,
                    isCompleted = false,
                    colorResId = habit.colorResId,
                    iconResId = habit.iconResId,
                    isExpired = false,
                    lastCompletedDate = null,
                    lastResetDate = null
                )

                viewModel.updateHabit(updateHabit)

                // CompletedDay tablosunu da güncelle
                completeDayViewModel.updateCompletedDays(
                    habitId = habit.id,
                    date = System.currentTimeMillis(),
                    completed = false
                )

                // Dialog'u kapat
                isDialogOpen = false
            },
            onDelete = {
                viewModel.deleteHabit(habit)
                isDialogOpen = false
            }
        )
    }


    if (showDialog.value) {
        val kalanGun = remainingDays(habit.completedDays, totalHabit(habit.frequency))
        CustomAlertDialog(onDismiss = { showDialog.value = false }, onConfirm = { showDialog.value = false }, kalanGun,remainingDays, habit, viewModel,)
    }


}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    kalanGun: Int,
    remainingDays : Long,
    habit: Habit,
    viewModel: HabitViewModel
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)) // Köşeleri yuvarlama
            .background(color = colorResource(R.color.arkaplan)), // Arka plan rengi
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){


                Icon(
                    painter = painterResource(R.drawable.baseline_info_24),
                    contentDescription = "Uyarı",
                    modifier = Modifier.size(50.dp),
                    tint = colorResource(R.color.yazirengi)
                )

                Text(
                    text = "Silme Onayı",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.kutubordrengi)
                )
            }

        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (remainingDays <= 0) {
                        "Süreniz dolmuş, tamamladınız. Silmek istediğine emin misin?"
                    } else {
                        if (kalanGun > 0) {
                            "Alışkanlığın bitmesine sadece $kalanGun gün kaldı. Silmek istediğine emin misin?"
                        } else {
                            "Harika! Bu alışkanlığı tamamladın. Silmek istediğine emin misin?"
                        }
                    },
                    fontSize = 16.sp,
                    color = colorResource(R.color.yazirengi),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val habitHistory = HabitHistory(
                        habitName = habit.name,
                        startDate = habit.startDate,
                        endDate = habit.finishDate,
                        frequency = habit.frequency,
                        daysCompleted = habit.completedDays,
                        habitType = habit.habitType
                    )
                    viewModel.deleteHabit(habit)
                    viewModel.insertHabitHistory(habitHistory)
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.pastelkirmizi))
            ) {
                Text("Sil", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("İptal", color = Color.Black)
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
fun sumDay(startMillis: Long, finishMillis: Long) : Int{
    val startDate = Instant.ofEpochMilli(startMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val finishDate = Instant.ofEpochMilli(finishMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()

    // Başlangıç ve bitiş tarihleri arasındaki farkı gün cinsinden hesapla
    return ChronoUnit.DAYS.between(startDate, finishDate).toInt()
}


@RequiresApi(Build.VERSION_CODES.O)
fun completionStatus(startMillis: Long, finishMillis: Long, completedDays: Int): String {

    val totalDays = sumDay(startMillis, finishMillis)

    // Tamamlanan gün sayısını, hedeflenen gün sayısı ile karşılaştır
    return if (completedDays >= totalDays) {
        "Tamamlandı"
    } else {
        "Tamamlanmadı"
    }
}



@Composable
fun AnimatedStarsSequence(
    startPosition: Offset,
    endPosition: Offset,
    onAnimationEnd: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // 8 tane yıldız animasyonu için başlangıç konumları
        val starPositions = List(8) { startPosition }

        // Yıldızları sırayla animasyonla hareket ettiriyoruz
        starPositions.forEachIndexed { index, position ->
            AnimatedStar(
                startPosition = position,
                endPosition = endPosition,
                delayMillis = index.toLong() * 300, // Gecikme, her yıldız için biraz hızlanacak
                onAnimationEnd = onAnimationEnd
            )
        }
    }
}

@Composable
fun AnimatedStar(
    startPosition: Offset,
    endPosition: Offset,
    delayMillis: Long,
    onAnimationEnd: () -> Unit
) {
    var animationProgress by remember { mutableFloatStateOf(0f) }

    // Opaklık animasyonu
    val alpha by animateFloatAsState(
        targetValue = 1f - animationProgress,
        animationSpec = tween(4000, easing = FastOutSlowInEasing), // Başlangıçta yavaş sonra hızlanma
        label = ""
    )

    LaunchedEffect(Unit) {
        delay(delayMillis) // Animasyonlar arasında gecikme
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(3000, easing = FastOutSlowInEasing) // Daha akıcı animasyon
        ) { value, _ ->
            animationProgress = value
        }
        onAnimationEnd()
    }

    val currentPosition = lerp(
        start = startPosition,
        stop = endPosition,
        fraction = animationProgress
    )

    Box(
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    currentPosition.x.roundToInt(),
                    currentPosition.y.roundToInt()
                )
            }
            .size(30.dp)
            .graphicsLayer(alpha = alpha) // Yıldızın opaklık değişimi
    ) {
        Image(
            painter = painterResource(R.drawable.starsolid),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}

fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
    return start + (stop - start) * fraction
}


@Composable
fun PatlayanAnimasyon(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1 // Sadece 1 kez çalışsın
    )

    Box(
        modifier = modifier
            .zIndex(1f), // Diğer elementlerin üzerinde görünsün
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}


fun playSound(
    soundResId: Int,
    context: Context
) {
    val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    val soundId = soundPool.load(context, soundResId, 1)

    soundPool.setOnLoadCompleteListener { _, _, _ ->
        // Sesin hızını (pitch) azaltarak daha kalın hale getiriyoruz
        soundPool.play(soundId, 1f, 1f, 0, 0, 0.9f) // 0.8f pitch değeri daha kalın bir ses yapar
    }
}
@Composable
fun HabitCompletionDialog(
    habitName: String,
    completionStatus: String,
    progress: Float,
    targetInfo: String,
    frequency: String,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .shadow(8.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Başarı İkonu",
                    tint = colorResource(R.color.yildiz),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$frequency Süreniz Doldu!",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.yazirengi)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(habitName)
                        }
                        append(" için $frequency süreniz tamamlandı! Devam etmek ister misiniz?")
                    },
                    style = TextStyle(fontSize = 16.sp),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.yazirengi)
                )

                Spacer(modifier = Modifier.height(24.dp))

                HabitProgressSection(completionStatus, progress, targetInfo)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.yesil)),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Sıfırla", color = Color.White)
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.pastelkirmizi)),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Sil", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun HabitProgressSection(
    completionStatus: String,
    progress: Float,
    targetInfo: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("Durum: ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium, color = if (completionStatus == "Tamamlandı") colorResource(R.color.yesil) else colorResource(R.color.pastelkirmizi))) {
                    append(completionStatus)
                }
            },
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            color = colorResource(R.color.yazirengi)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = colorResource(R.color.kutubordrengi),
            trackColor = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = targetInfo,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
            color = colorResource(R.color.yazirengi)
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun calculateProgress(completedDays: Int, finishMillis : Long, startMillis : Long) : Float{
    val totalDays = sumDay(startMillis, finishMillis)
    return if (completedDays > 0)  completedDays.toFloat() / totalDays.toFloat() else  0f

}


private fun getProfilePainter(profileImage: String, defaultResId: Int): Int {
    return try {
        profileImage.toInt()
    } catch (e: NumberFormatException) {
        defaultResId
    }
}


