package com.example.goalmate.prenstatntion.homescreen

import android.annotation.SuppressLint
import android.content.Context
import android.media.SoundPool
import android.os.Build
import android.util.Log
import androidx.compose.material3.*
import androidx.annotation.RequiresApi

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.derivedStateOf
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.goalmate.data.localdata.GroupRequest
import com.example.goalmate.extrensions.RequestStatus
import com.example.goalmate.extrensions.RequestsUiState
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.Text
import androidx.compose.material3.RadioButtonDefaults.colors
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.app.ActivityCompat
import com.example.goalmate.utils.Constants
import kotlinx.coroutines.CoroutineScope


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HabitViewModel = viewModel(),
    starCoinViewModel: StarCoinViewModel = viewModel(),
    completeDayViewModel: CompleteDayViewModel,
    registerViewModel: RegisterViewModel = viewModel(),
    groupsAddViewModel: GroupsAddViewModel,
    context: Context,
    motivationQuoteViewModel: MotivationQuoteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val habits = remember(uiState) {
        when (uiState) {
            is ExerciseUiState.Success -> (uiState as ExerciseUiState.Success).habits
            else -> emptyList()
        }
    }

    val userName by registerViewModel.userName.collectAsState()
    val totalPoint = registerViewModel.userPoints.collectAsState().value
    val profileImage by registerViewModel.profileImage.collectAsState()


    Log.e("userName","profileImage : $profileImage")
    Log.e("userName","userName : $userName")

    val currentTime by viewModel.currentTime.collectAsState()

    val totalHabits by viewModel.countActiveHabits.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getCountActiveHabit()
        groupsAddViewModel.getCurrentTotalPoint()
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

    LaunchedEffect(uiState) {
        when (uiState) {
            is ExerciseUiState.Success -> {
                viewModel.getExercises()
                viewModel.getCountActiveHabit()
            }
            else -> {}
        }
    }

    val lazyListState = rememberLazyListState()
    
    val isScrollingUp = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 || 
            lazyListState.firstVisibleItemScrollOffset == 0 ||
            lazyListState.isScrollInProgress.not()
        }
    }

    LaunchedEffect(Unit) {
        registerViewModel.getCurrentUser(context)
    }

    LaunchedEffect(profileImage) {
        Log.d("HomeScreen", "Profil resmi güncellendi: $profileImage")
    }

    var showRequestsSheet by remember { mutableStateOf(false) }
    val requestsState by viewModel.requestsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    // Bildirim izni kontrolü
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showPermissions by remember { mutableStateOf(viewModel.isPermissionDenied(context)) }
    
    LaunchedEffect(Unit) {
        when (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
            PackageManager.PERMISSION_GRANTED -> {
                // İzin zaten verilmiş, bir şey yapmaya gerek yok
            }
            PackageManager.PERMISSION_DENIED -> {
                if (showPermissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    ) {
                        showPermissionDialog = true
                    } else {
                        // İzin reddedilmiş ve bir daha gösterilmemesi seçtiyse
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Bildirimleri almak için ayarlardan izin vermelisiniz",
                                actionLabel = "Ayarlar",
                                duration = SnackbarDuration.Long
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    // Ayarlar sayfasına yönlendir
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        android.net.Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                }
                                SnackbarResult.Dismissed -> {
                                    // Kullanıcı snackbar'ı kapattı
                                }
                            }
                        }
                        viewModel.setPermissionDenied(context, false)
                    }
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPermissionDialog = false
                viewModel.setPermissionDenied(context, false)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Bildirimleri almak için ayarlardan izin vermelisiniz",
                        actionLabel = "Ayarlar",
                        duration = SnackbarDuration.Long
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                        SnackbarResult.Dismissed -> {
                            // Kullanıcı snackbar'ı kapattı
                        }
                    }
                }
            },
            title = {
                Text(
                    text = "Bildirim İzni",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.yazirengi)
                )
            },
            text = {
                Text(
                    text = "Alışkanlıklarınızı takip edebilmemiz için bildirim iznine ihtiyacımız var. İzin vermek ister misiniz?",
                    color = colorResource(R.color.yazirengi)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            101
                        )
                    }
                ) {
                    Text("İzin Ver", color = colorResource(R.color.kutubordrengi))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPermissionDialog = false
                        viewModel.setPermissionDenied(context, false)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Bildirimleri almak için ayarlardan izin vermelisiniz",
                                actionLabel = "Ayarlar",
                                duration = SnackbarDuration.Long
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        android.net.Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                }
                                SnackbarResult.Dismissed -> {
                                    // Kullanıcı snackbar'ı kapattı
                                }
                            }
                        }
                    }
                ) {
                    Text("Reddet", color = colorResource(R.color.pastelkirmizi))
                }
            }
        )
    }

    Scaffold(
        containerColor = colorResource(R.color.arkaplan),
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp) // BottomNavigation'ın üzerinde göstermek için padding ekliyoruz
            )
        },
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

            if (showExplosion) {
                PatlayanAnimasyon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, colorResource(R.color.yazirengi), CircleShape)
                            ) {
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
                                                    painterResource(getProfilePainter(profileImage, R.drawable.personel))
                                                }
                                            }
                                        }
                                        else -> painterResource(R.drawable.personel)
                                    },
                                    contentDescription = "Profile Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clickable {
                                            navController.navigate("BadgesScreen")
                                        }
                                        .fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = userName,
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    RankBadge(rank = Constants.getRankFromPoints(totalPoint) , modifier = Modifier)
                                    /*
                                    Text(
                                        text = "Hoş Geldin",
                                        color = colorResource(R.color.yazirengi),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )

                                     */
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .width(130.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { showRequestsSheet = true }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.message),
                                    contentDescription = "Message",
                                    tint = colorResource(R.color.sarı),
                                    modifier = Modifier.size(30.dp)
                                )
                                
                                if (((requestsState as? RequestsUiState.Success)?.unreadCount ?: 0) > 0) {
                                    Badge(
                                        containerColor = colorResource(R.color.pastelkirmizi),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "${(requestsState as RequestsUiState.Success).unreadCount}",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }



                            Spacer(modifier = Modifier.width(30.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sackdollar),
                                    contentDescription = "Coin",
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "$totalPoint",
                                    color = colorResource(R.color.yazirengi),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

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
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
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
                                    .fillMaxWidth()
                                    .height(20.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(5.dp))
                                        .height(12.dp),
                                    color = colorResource(R.color.yazirengi),
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${"%.0f".format(progress * 100)}%",
                                    color = colorResource(R.color.yazirengi),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 8.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
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

                            Spacer(modifier = Modifier.height(10.dp))

                        }
                    }

                    else -> {}
                }
            }
        }

    }

    if (showRequestsSheet) {
        RequestsBottomSheet(
            requestsState = requestsState,
            onDismiss = { showRequestsSheet = false },
            onRequestClick = { request -> 
                navController.navigate("requestDetails/${request.id}")
            },
            onViewAllClick = {
                navController.navigate("allRequests")
                showRequestsSheet = false
            }
        )
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
                            checked = habit.isCompleted,
                            onCheckedChange = { checked ->
                                if (remainingDays <= 0) {
                                    isDialogOpen = true
                                    return@Checkbox
                                }

                                if (checked) {
                                   // viewModel.addStarAnimation(checkboxPosition)
                                    playSound(R.raw.patlama, context)
                                    viewModel.onCheckboxClickedTrue()
                                }

                                viewModel.updateHabitCompletion(habit)
                                completeDayViewModel.updateCompletedDays(
                                    habitId = habit.id,
                                    date = System.currentTimeMillis(),
                                    completed = checked
                                )
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
    remainingDays: Long,
    habit: Habit,
    viewModel: HabitViewModel
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, colorResource(R.color.kutubordrengi), RoundedCornerShape(24.dp))
            .background(Color.White),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Sil",
                        modifier = Modifier.size(30.dp),
                        tint = colorResource(R.color.pastelkirmizi)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Alışkanlığı Sil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.yazirengi)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    color = colorResource(R.color.yazirengi).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Text(
                    text = "Bu işlem geri alınamaz",
                    fontSize = 14.sp,
                    color = colorResource(R.color.pastelkirmizi),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.kutubordrengi).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Vazgeç",
                        color = colorResource(R.color.kutubordrengi),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = {
                        val habitHistory = HabitHistory(
                            habitName = habit.name,
                            startDate = habit.startDate,
                            frequency = habit.frequency,
                            daysCompleted = habit.completedDays,
                            habitType = habit.habitType
                        )
                        viewModel.deleteHabit(habit)
                        viewModel.insertHabitHistory(habitHistory)
                        onConfirm()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.pastelkirmizi)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Sil",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {}
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


 fun getProfilePainter(profileImage: String, defaultResId: Int): Int {
    return try {
        profileImage.toInt()
    } catch (e: NumberFormatException) {
        defaultResId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsBottomSheet(
    requestsState: RequestsUiState,
    onDismiss: () -> Unit,
    onRequestClick: (GroupRequest) -> Unit,
    onViewAllClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.beyaz),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grup İstekleri",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.yazirengi)
                )
                
                TextButton(
                    onClick = onViewAllClick
                ) {
                    Text(
                        text = "Tümünü Gör",
                        color = colorResource(R.color.kutubordrengi)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (requestsState) {
                is RequestsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is RequestsUiState.Success -> {
                    if (requestsState.requests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Henüz hiç istek yok",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(R.color.yazirengi)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            itemsIndexed(requestsState.requests.take(3)) { _, request ->
                                RequestItem(
                                    request = request,
                                    onClick = { onRequestClick(request) }
                                )
                                HorizontalDivider(color = colorResource(R.color.yazirengi).copy(alpha = 0.2f))
                            }
                        }
                    }
                }
                
                is RequestsUiState.Error -> {
                    Text(
                        text = requestsState.message,
                        color = colorResource(R.color.pastelkirmizi),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RequestItem(
    request: GroupRequest,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profil resmi
        AsyncImage(
            model = request.senderImage ?: R.drawable.personel,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = request.senderName,
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.yazirengi)
            )
            Text(
                text = "${request.groupName} grubuna katılmak istiyor",
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.yazirengi).copy(alpha = 0.7f)
            )
        }

        when (request.status) {
            RequestStatus.PENDING -> {
                Icon(
                    painter = painterResource(R.drawable.baseline_info_24),
                    contentDescription = "Pending",
                    tint = colorResource(R.color.sarı),
                    modifier = Modifier.size(24.dp)
                )
            }
            RequestStatus.ACCEPTED -> {
                Icon(
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = "Accepted",
                    tint = colorResource(R.color.yesil),
                    modifier = Modifier.size(24.dp)
                )
            }
            RequestStatus.REJECTED -> {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Rejected",
                    tint = colorResource(R.color.pastelkirmizi),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}



@Composable
fun PointColor(modifier: Modifier, userPoint: Int) {
    val ranks = listOf(
        "Acemi" to Color(0xFF48C9B0),
        "Başlangıç" to Color(0xFF5DADE2),
        "Çaylak" to Color(0xFF9B59B6),
        "Disiplinli" to Color(0xFFF4D03F),
        "Kararlı" to Color(0xFFE74C3C),
        "Usta" to Color(0xFF2ECC71),
        "Bilge" to Color(0xFF8E44AD),
        "Efsane" to Color(0xFFFFD700)
    )

    // Rank'e göre rengi bul
    val pointColor = ranks.find { it.first.equals(Constants.getRankFromPoints(userPoint), ignoreCase = true) }?.second ?: colorResource(R.color.kutubordrengi)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = userPoint.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pointColor
        )
        Text(
            text = "point",
            fontSize = 13.sp,
            color = pointColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
 }




@Composable
fun RankBadge(rank: String , modifier: Modifier) {
    val ranks = listOf(
        "Acemi" to listOf(Color(0xFF48C9B0), Color(0xFF45B39D)),
        "Başlangıç" to listOf(Color(0xFF5DADE2), Color(0xFF3498DB)),
        "Çaylak" to listOf(Color(0xFF9B59B6), Color(0xFF8E44AD)),
        "Disiplinli" to listOf(Color(0xFFF4D03F), Color(0xFFF1C40F)),
        "Kararlı" to listOf(Color(0xFFE74C3C), Color(0xFFC0392B)),
        "Usta" to listOf(Color(0xFF2ECC71), Color(0xFF27AE60)),
        "Bilge" to listOf(Color(0xFF8E44AD), Color(0xFF6C3483)),
        "Efsane" to listOf(Color(0xFFFFD700), Color(0xFFFFA500))
    )

    ranks.find { it.first.lowercase() == rank.lowercase() }?.let { (rankName, colors) ->
        Box(
            modifier
                .background(
                    brush = Brush.horizontalGradient(colors = colors.map { it.copy(alpha = 0.15f) }),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(colors = colors),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.height(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = colors[1],
                    modifier = Modifier.size(12.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = rankName,
                    color = colors[1],
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        shadow = Shadow(
                            color = colors[0].copy(alpha = 0.3f),
                            offset = Offset(0f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}
