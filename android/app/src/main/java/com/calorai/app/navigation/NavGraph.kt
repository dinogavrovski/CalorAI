package com.calorai.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.calorai.app.ui.screens.auth.LoginScreen
import com.calorai.app.ui.screens.auth.RegisterScreen
import com.calorai.app.ui.screens.history.HistoryScreen
import com.calorai.app.ui.screens.home.HomeScreen
import com.calorai.app.ui.screens.log.LogMealScreen
import com.calorai.app.ui.screens.profile.ProfileScreen
import com.calorai.app.ui.theme.OrangeAccent
import com.calorai.app.ui.theme.OnSurfaceVariant
import com.calorai.app.ui.theme.Surface1
import com.calorai.app.ui.theme.OnSurface

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object LogMeal : Screen("log_meal")
    object History : Screen("history")
    object Profile : Screen("profile")
}

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
            PremiumBottomBar(
                activeRoute = activeRoute ?: Screen.Home.route,
                onNavigate = { route ->
                    if (activeRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun PremiumBottomBar(
    activeRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home tab
            NavBarIcon(
                icon = Icons.Default.Home,
                contentDescription = "Home",
                selected = activeRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )

            // Center Log tab — big orange circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate(Screen.LogMeal.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Meal",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // History tab
            NavBarIcon(
                icon = Icons.Default.Schedule,
                contentDescription = "History",
                selected = activeRoute == Screen.History.route,
                onClick = { onNavigate(Screen.History.route) }
            )
        }
    }
}

@Composable
private fun NavBarIcon(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) OrangeAccent else OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
