package com.example.goalmate.prenstatntion.MainActivity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.goalmate.prenstatntion.ProfilScreen.ProfileScreen
import com.example.goalmate.prenstatntion.UserScreen.UserScreen
import com.example.goalmate.prenstatntion.login.LoginScreen
import com.example.goalmate.viewmodel.RegisterViewModel
import com.example.goalmate.prenstatntion.verification.VerificationScreen
import com.example.goalmate.prenstatntion.welcome.WelcomeScreen
import com.example.goalmate.utils.CloudinaryConfig
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YeniProjeTheme {
                ChangingScreen()

            }
        }
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
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Yeni başlatmayı ekle
    CloudinaryConfig.initCloudinary(context)

    // AuthState'i gözlemle
    val authState by registerViewModel.authState.collectAsState()

    // AuthState'e göre yönlendirme yap
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Idle -> {
                navController.navigate("WelcomeScreen")
            }
            is AuthState.Success -> {
                navController.navigate("HomeScreen") {
                    popUpTo("LoginScreen") { inclusive = true }
                }
            }
            is AuthState.VerificationRequired -> {
                navController.navigate("verification") {
                    popUpTo("LoginScreen") { inclusive = true }
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
                currentRoute != "LoginScreen" &&
                currentRoute != "ProfileScreen" &&
                currentRoute != "register_screen" &&
                currentRoute != "WelcomeScreen"
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
                VerificationScreen(navController = navController, viewModel = registerViewModel , auth = auth, context = context)
            }

            composable(route = "HomeScreen") {
                HomeScreen(navController, habitViewModel, starCoinViewModel, completeDayViewModel , registerViewModel = registerViewModel, context = context)
            }

            composable(route = "UserScreen") {
                UserScreen(registerViewModel,navController,context)
            }

            composable(
                route = "AddHabitScreen?isGroup={isGroup}",
                arguments = listOf(
                    navArgument("isGroup") {
                        type = NavType.BoolType
                        defaultValue = false // Eğer parametre gelmezse varsayılan değer
                    }
                )
            ) { backStackEntry ->
                val isGroup = backStackEntry.arguments?.getBoolean("isGroup") ?: false
                Log.e("isGroup", "Received isGroup: $isGroup")
                AddHabitScreen(navController = navController, habitViewModel, isGroup)
            }

            composable(route = "GroupAndPrivate") {
                GroupAndPrivate(navController = navController, habitViewModel)
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
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val lineColor = colorResource(R.color.yazirengi) // Çizgi için renk

    NavigationBar(
        containerColor = colorResource(R.color.arkaplan),

        tonalElevation = 4.dp, // Hafif gölgelendirme
        modifier = Modifier
            .drawBehind { // Çizgiyi çizecek alan
                val strokeWidth = 4.dp.toPx() // Çizginin kalınlığı
                val y = 0f // Çizginin başlangıç y koordinatı (üst kısım)
                drawLine(
                    color = lineColor, // Çizgi rengi
                    start = Offset(0f, y),
                    end = Offset(size.width, y), // Çizginin bitiş noktası
                    strokeWidth = strokeWidth
                )
            }
    ) {
        val items = listOf(
            BottomNavItem("HomeScreen", "Home", R.drawable.home),
            BottomNavItem("Groups", "Groups", R.drawable.groups),
            BottomNavItem("GroupAndPrivate", "Add", R.drawable.add),
            BottomNavItem("Calendar", "Calendar", R.drawable.calendar),
            BottomNavItem("UserScreen", "Profile", R.drawable.profil)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .background(
                                color = if (currentRoute == item.route)
                                    colorResource(R.color.yazirengi) // Aktif durumda dış arka plan rengi
                                else
                                    Color.Transparent, // Pasif durumda şeffaf
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            tint = if (currentRoute == item.route)
                                Color.White // Aktif durum için ikon iç rengi
                            else
                                colorResource(R.color.kutubordrengi) // Pasif durum rengi
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent // İkonun altındaki vurgu kaldırıldı
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