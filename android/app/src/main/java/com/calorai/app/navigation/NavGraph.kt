package com.calorai.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.calorai.app.ui.screens.auth.LoginScreen
import com.calorai.app.ui.screens.auth.RegisterScreen
import com.calorai.app.ui.screens.history.HistoryScreen
import com.calorai.app.ui.screens.home.HomeScreen
import com.calorai.app.ui.screens.log.LogMealScreen
import com.calorai.app.ui.screens.profile.ProfileScreen
import com.calorai.app.ui.theme.Green400
import com.calorai.app.ui.theme.OnSurfaceVariant
import com.calorai.app.ui.theme.Surface1

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object LogMeal : Screen("log_meal")
    object History : Screen("history")
    object Profile : Screen("profile")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.LogMeal, "Log", Icons.Default.AddCircle),
    BottomNavItem(Screen.History, "History", Icons.Default.List),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
)

@Composable
fun CalorAINavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = {
            fadeIn(animationSpec = tween(280)) +
                    slideInHorizontally(initialOffsetX = { it / 5 }, animationSpec = tween(280))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(targetOffsetX = { -it / 5 }, animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(280)) +
                    slideInHorizontally(initialOffsetX = { -it / 5 }, animationSpec = tween(280))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(targetOffsetX = { it / 5 }, animationSpec = tween(200))
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Home.route) { padding ->
                HomeScreen(onLogMeal = { navController.navigate(Screen.LogMeal.route) }, paddingValues = padding)
            }
        }

        composable(Screen.LogMeal.route) {
            MainScaffold(navController = navController, currentRoute = Screen.LogMeal.route) { padding ->
                LogMealScreen(
                    onMealLogged = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    paddingValues = padding
                )
            }
        }

        composable(Screen.History.route) {
            MainScaffold(navController = navController, currentRoute = Screen.History.route) { padding ->
                HistoryScreen(paddingValues = padding)
            }
        }

        composable(Screen.Profile.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Profile.route) { padding ->
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    paddingValues = padding
                )
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val activeRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface1,
                tonalElevation = Dp(0f)
            ) {
                bottomNavItems.forEach { item ->
                    val selected = activeRoute == item.screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green400,
                            selectedTextColor = Green400,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant,
                            indicatorColor = Green400.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
