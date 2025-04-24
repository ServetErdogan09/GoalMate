package com.example.goalmate.prenstatntion.MainActivity

import AllRequestsScreen
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
import com.google.firebase.firestore.FirebaseFirestore
import com.example.goalmate.prenstatntion.BaseScreen.BaseScreen
import com.example.goalmate.prenstatntion.RulesScreen.RulesScreen
import com.example.goalmate.prenstatntion.ScoreBoard.ScoreBoardScreen
import com.example.goalmate.prenstatntion.showGroupChatScreen.ShowGroupChatScreen
import com.example.goalmate.prenstatntion.viewProfile.ViewProfile
import com.example.goalmate.presentation.badgesScreen.BadgesScreen
import com.example.goalmate.viewmodel.BadgesViewModel
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import com.example.goalmate.viewmodel.ScoreBoardViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            YeniProjeTheme {
                BaseScreen {
                    ChangingScreen()
                }

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
    val groupsAddViewModel : GroupsAddViewModel = viewModel()
    val motivationQuoteViewModel : MotivationQuoteViewModel = viewModel()
    val badgesViewModel : BadgesViewModel = viewModel()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scoreBoardViewModel : ScoreBoardViewModel = viewModel()


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
                currentRoute != "RulesScreen" &&
                currentRoute != "WelcomeScreen" &&
                currentRoute != "UserScreen" &&
                currentRoute != "BadgesScreen" &&
                !currentRoute.startsWith("showGroupChatScreen") &&
                !currentRoute.startsWith("ScoreBoardScreen") &&
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
            composable(route = "BadgesScreen") {
                BadgesScreen(navController = navController, badgesViewModel = badgesViewModel,context)
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
                HomeScreen(navController, habitViewModel, starCoinViewModel, completeDayViewModel , registerViewModel = registerViewModel, context = context, motivationQuoteViewModel = motivationQuoteViewModel, groupsAddViewModel = groupsAddViewModel)
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


            composable(route = "RulesScreen") {
                RulesScreen(navController)
            }

            composable(
                route = "showGroupChatScreen/{groupedId}/{groupName}/{members}/{daysLeft}/{habitType}",
                arguments = listOf(
                    navArgument("groupedId") {
                        type = NavType.StringType
                    },
                    navArgument("groupName") {  // groupName parametresini ekledik
                        type = NavType.StringType
                    },
                    navArgument(name = "members"){
                        type = NavType.IntType
                    },
                    navArgument("daysLeft"){
                        type = NavType.LongType
                    },

                    navArgument("habitType"){
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val groupedId = backStackEntry.arguments?.getString("groupedId") ?: ""
                val groupName = backStackEntry.arguments?.getString("groupName") ?:""
                val habitType = backStackEntry.arguments?.getString("habitType") ?:"Günlük"
                val groupMembers = backStackEntry.arguments?.getInt("members") ?: 0
                val groupDaysLeft = backStackEntry.arguments?.getLong("daysLeft") ?: 0  // getInt yerine getLong ve doğru parametre
                ShowGroupChatScreen(navController = navController, groupedId = groupedId, groupsAddViewModel = groupsAddViewModel, groupName = groupName, members = groupMembers, daysLeft = groupDaysLeft , habitType = habitType)
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
                route = "GroupDetailScreen/{groupId}/{groupName}",
                arguments = listOf(
                    navArgument("groupId"){
                        type = NavType.StringType
                    },
                    navArgument("groupName"){
                        type = NavType.StringType
                    }
                )
            ) {backStackEntry->
                val groupId = backStackEntry.arguments?.getString("groupId")?:""
                val groupName = backStackEntry.arguments?.getString("groupName")?:""
                GroupDetailScreen(groupId = groupId, navController = navController , groupName = groupName, motivationQuoteViewModel = motivationQuoteViewModel, registerViewModel = registerViewModel, groupsAddViewModel = groupsAddViewModel)
            }


            composable(
                route = "ScoreBoardScreen/{groupId}",
                arguments = listOf(
                    navArgument("groupId"){
                        type = NavType.StringType
                    }
                )
            ){backStackEntry->

                val groupId = backStackEntry.arguments?.getString("groupId") ?:""
                ScoreBoardScreen(viewModel = scoreBoardViewModel , groupId , navController)
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
                ViewProfile(userId = userId, navController = navController, groupsAddViewModel = groupsAddViewModel ,registerViewModel)
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