package com.example.goalmate.prenstatntion.MainActivity

import AllRequestsScreen
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.goalmate.groupandprivatecreate.GroupAndPrivate
import com.example.goalmate.prenstatntion.AnalysisScreen.AnalysisScreen
import com.example.goalmate.prenstatntion.homescreen.HomeScreen
import com.example.goalmate.prenstatntion.ExerciseAdd.AddHabitScreen
import com.example.goalmate.viewmodel.CompleteDayViewModel
import com.example.goalmate.viewmodel.HabitViewModel
import com.example.goalmate.viewmodel.StarCoinViewModel
import com.example.goalmate.R
import com.example.goalmate.ui.theme.YeniProjeTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.goalmate.data.AuthState
import com.example.goalmate.prenstatntion.GroupsListScreen.GroupDetailScreen
import com.example.goalmate.prenstatntion.ProfilScreen.ProfileScreen
import com.example.goalmate.prenstatntion.UserScreen.UserScreen
import com.example.goalmate.prenstatntion.groupsadd.GroupsAdd
import com.example.goalmate.prenstatntion.login.LoginScreen
import com.example.goalmate.viewmodel.RegisterViewModel
import com.example.goalmate.prenstatntion.verification.VerificationScreen
import com.example.goalmate.prenstatntion.welcome.WelcomeScreen
import com.example.goalmate.presentation.GroupsListScreen.GroupListScreen
import com.example.goalmate.utils.CloudinaryConfig
import com.example.goalmate.viewmodel.GroupsAddViewModel
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.app.NotificationCompat
import com.example.goalmate.prenstatntion.showGroupChatScreen.ShowGroupChatScreen
import com.example.goalmate.prenstatntion.viewProfile.ViewProfile
import com.example.goalmate.service.FirebaseMessagingService
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import com.google.firebase.firestore.FirestoreRegistrar

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // bildiirim izin verildiyse tokunu güncele
            updateFCMToken()
            Log.d("Notification", "Bildirim izni verildi")
        } else {
            // İzin reddedildi kullanıcıyı bilgilendir
            Toast.makeText(
                this,
                "Bildirimleri almak için izin vermeniz gerekiyor. Ayarlardan izin verebilirsiniz.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

   // tokunu güncele
    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "New token: $token")
                
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update(
                            mapOf(
                                "fcmToken" to token,
                                "lastTokenUpdate" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            Log.d("FCM", "Token updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Token update failed", e)
                        }
                }
            } else {
                Log.e("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }

    // Bildirim iznini kontrol et
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Bildirim İzni Gerekli")
                    .setMessage("Grup isteklerini ve önemli güncellemeleri alabilmek için bildirim iznine ihtiyacımız var.")
                    .setPositiveButton("İzin Ver") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("Şimdi Değil") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                updateFCMToken()
            }
        } else {
            updateFCMToken()
        }
    }



    /*
    private fun testNotification() {
        // Manuel bildirim testi
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, "group_notifications")
            .setContentTitle("Test Bildirimi 2222222")
            .setContentText("Bu bir test bildirimidir")
            .setSmallIcon(R.drawable.goal_mate)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        Log.d(TAG, "Test notification displayed")
    }

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test için FCM service'i başlatalım
        val fcmService = FirebaseMessagingService()
       // fcmService.testLogging()
        
        // FCM token'ı manuel olarak alalım ve loglayalım
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("FCM", "Token: ${task.result}")
                } else {
                    Log.e("FCM", "Token failed", task.exception)
                }
            }

        // Bildirim izinlerini kontrol et
        checkNotificationPermission()

        // Bildirim izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                // İzin varsa test bildirimi göster
               // testNotification()
            }
        } else {
           // testNotification()
        }

        askNotificationPermission() // İzin kontrolü yap
        enableEdgeToEdge()
        setContent {
            YeniProjeTheme {
                ChangingScreen()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != 
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChangingScreen() {
    val navController = rememberNavController()
    val habitViewModel: HabitViewModel = viewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val starCoinViewModel: StarCoinViewModel = viewModel()
    val completeDayViewModel: CompleteDayViewModel = viewModel()
    val groupsAddViewModel : GroupsAddViewModel = viewModel()
    val motivationQuoteViewModel : MotivationQuoteViewModel = viewModel()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()


    // Yeni başlatmayı ekle
    CloudinaryConfig.initCloudinary(context)

    // AuthState'i gözlemle
    val authState by registerViewModel.authState.collectAsState()

    // AuthState'e göre yönlendirme yap
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Idle -> {
                try {
                    // Mevcut rotayı güvenli bir şekilde al
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    
                    // Sadece belirli ekranlardan geliyorsa yönlendirme yap
                    if (currentRoute == "WelcomeScreen" || currentRoute == "LoginScreen") {
                        if (auth.currentUser == null) {
                            navController.navigate("WelcomeScreen") {
                                // Önceki destinationları temizle
                                popUpTo("LoginScreen") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("HomeScreen") {
                                // Önceki destinationları temizle
                                popUpTo("LoginScreen") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Navigation", "Navigation error", e)
                }
            }
            is AuthState.Success -> {
                try {
                    navController.navigate("HomeScreen") {
                        popUpTo("LoginScreen") { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    Log.e("Navigation", "Navigation error", e)
                }
            }
            is AuthState.VerificationRequired -> {
                try {
                    navController.navigate("verification") {
                        popUpTo("LoginScreen") { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    Log.e("Navigation", "Navigation error", e)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route
            if (!currentRoute.isNullOrBlank() &&
                !currentRoute.startsWith("AddHabitScreen") &&
                currentRoute != "login" &&
                currentRoute != "verification" &&
                !currentRoute.startsWith("AnalysisScreen") &&
                !currentRoute.startsWith("GroupDetailScreen") &&
                currentRoute != "LoginScreen" &&
                currentRoute != "GroupsAdd" &&
                currentRoute != "ProfileScreen" &&
                currentRoute != "register_screen" &&
                currentRoute != "WelcomeScreen" &&
                currentRoute != "UserScreen" &&
                !currentRoute.startsWith("ViewProfile")
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "WelcomeScreen") {
            composable(route = "WelcomeScreen") {
                WelcomeScreen(navController = navController)
            }

            composable(route = "LoginScreen") {
                LoginScreen(navController = navController, context = context)
            }

            
            composable(route = "ProfileScreen") {
                ProfileScreen(navController = navController)
            }



            composable(route = "register_screen") {
                LoginScreen(
                    navController = navController,
                    initialLoginMode = false,
                    context = context
                )
            }

            composable(route = "verification") {
                VerificationScreen(navController = navController, viewModel = registerViewModel , auth = auth, context = context, db = db)
            }

            composable(route = "HomeScreen") {
                HomeScreen(navController, habitViewModel, starCoinViewModel, completeDayViewModel , registerViewModel = registerViewModel, context = context, motivationQuoteViewModel = motivationQuoteViewModel)
            }



            composable(route = "UserScreen") {
                if (auth.currentUser != null) {
                    UserScreen(registerViewModel, navController, context)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("WelcomeScreen") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }

            composable(
                route = "AddHabitScreen?isGroup={isGroup}&habitId={habitId}",
                arguments = listOf(
                    navArgument("isGroup") {
                        type = NavType.BoolType
                        defaultValue = false 
                    },
                    navArgument("habitId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val isGroup = backStackEntry.arguments?.getBoolean("isGroup") ?: false
                val habitId = backStackEntry.arguments?.getInt("habitId")?.takeIf { it != -1 }
                Log.e("isGroup", "Received isGroup: $isGroup")
                AddHabitScreen(navController = navController, habitViewModel, isGroup, habitId)
            }

            composable(route = "GroupAndPrivate") {
                GroupAndPrivate(navController = navController, habitViewModel)
            }

            composable(route = "GroupsAdd") {
                GroupsAdd(navController , viewModel = groupsAddViewModel , registerViewModel, motivationQuoteViewModel = motivationQuoteViewModel)
            }


            composable(route = "GroupListScreen") {
                GroupListScreen(navController, viewModel = groupsAddViewModel , registerViewModel = registerViewModel)
            }

            composable(route="ShowGroupChatScreen"){
                ShowGroupChatScreen(navController = navController)
            }

            composable(
                route = "AnalysisScreen/{habitId}",
                arguments = listOf(
                    navArgument("habitId"){
                        type= NavType.IntType
                        defaultValue = 0
                    }

                )
            ) {backStackEntry->
                val habitId = backStackEntry.arguments?.getInt("habitId")?:0
                Log.e("habitId","çekilen ıd : $habitId")
                AnalysisScreen(navController = navController,
                    habitViewModel = habitViewModel, habitId = habitId , completedDayViewModel = completeDayViewModel, starCoinViewModel = starCoinViewModel )
            }

            composable(
                route = "GroupDetailScreen/{groupId}",
                arguments = listOf(
                    navArgument("groupId"){
                        type = NavType.StringType
                    }
                )
            ) {backStackEntry->
                val groupId = backStackEntry.arguments?.getString("groupId")?:""
                GroupDetailScreen(groupId = groupId,navController , groupsAddViewModel, motivationQuoteViewModel = motivationQuoteViewModel, registerViewModel = registerViewModel)
            }


            composable(
                route = "ViewProfile/{userId}",
                arguments = listOf(
                    navArgument("userId"){
                        type = NavType.StringType
                    }
                )
            ) {backStackEntry->
                val userId = backStackEntry.arguments?.getString("userId")?:""
                ViewProfile(userId = userId, navController = navController, groupsAddViewModel = groupsAddViewModel)
            }

            composable(route = "allRequests") {
                AllRequestsScreen(habitViewModel, registerViewModel = registerViewModel,navController)
            }

        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val lineColor = colorResource(R.color.yazirengi)
    val auth = FirebaseAuth.getInstance()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val context = LocalContext.current

    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            registerViewModel.getCurrentUser(context)
        }
    }

    NavigationBar(
        containerColor = colorResource(R.color.arkaplan),
        tonalElevation = 4.dp,
        modifier = Modifier.drawBehind {
            val strokeWidth = 4.dp.toPx()
            val y = 0f
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        }
    ) {
        val items = listOf(
            BottomNavItem("HomeScreen", "Home", R.drawable.home),
            BottomNavItem("GroupListScreen", "Groups", R.drawable.groups),
            BottomNavItem("GroupAndPrivate", "Add", R.drawable.add),
            BottomNavItem("Calendar", "Calendar", R.drawable.calendar),
            BottomNavItem("UserScreen", "Profile", R.drawable.profil)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    when {
                        item.route == "UserScreen" -> {
                            if (auth.currentUser != null) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate("WelcomeScreen") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                        else -> {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .background(
                                color = if (currentRoute == item.route)
                                    colorResource(R.color.yazirengi)
                                else
                                    Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            tint = if (currentRoute == item.route)
                                Color.White
                            else
                                colorResource(R.color.kutubordrengi)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: Int)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YeniProjeTheme{
        ChangingScreen()
    }
}