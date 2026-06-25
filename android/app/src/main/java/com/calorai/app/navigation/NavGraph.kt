package com.calorai.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.calorai.app.ui.components.AppIcons
import com.calorai.app.ui.screens.auth.LoginScreen
import com.calorai.app.ui.screens.auth.RegisterScreen
import com.calorai.app.ui.screens.history.HistoryScreen
import com.calorai.app.ui.screens.home.HomeScreen
import com.calorai.app.ui.screens.log.LogMealScreen
import com.calorai.app.ui.screens.profile.ProfileScreen
import com.calorai.app.ui.screens.weight.WeightScreen
import com.calorai.app.ui.theme.Background
import com.calorai.app.ui.theme.OrangeAccent
import com.calorai.app.ui.theme.OnSurfaceDim

// Custom iOS-like easing
private val EaseOut = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object LogMeal : Screen("log_meal")
    object Weight : Screen("weight")
    object History : Screen("history")
    object Profile : Screen("profile")
}

// Routes that show the bottom nav
private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Weight.route,
    Screen.History.route,
    Screen.Profile.route
)

@Composable
fun CalorAINavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = Background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                FloatingPillNav(
                    activeRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onLogMeal = { navController.navigate(Screen.LogMeal.route) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { tabEnterTransition() },
            exitTransition = { tabExitTransition() },
            popEnterTransition = { tabEnterTransition() },
            popExitTransition = { tabExitTransition() }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onLogMeal = { navController.navigate(Screen.LogMeal.route) },
                    onWeightClick = { navController.navigate(Screen.Weight.route) },
                    paddingValues = padding
                )
            }

            composable(Screen.Weight.route) {
                WeightScreen(paddingValues = padding)
            }

            // Log meal slides up from bottom like a modal sheet
            composable(
                route = Screen.LogMeal.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(380, easing = EaseOut)
                    ) + fadeIn(tween(200))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(tween(200))
                },
                popEnterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(380, easing = EaseOut)
                    )
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(tween(200))
                }
            ) {
                LogMealScreen(
                    onMealLogged = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    paddingValues = padding
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(paddingValues = padding)
            }

            composable(Screen.Profile.route) {
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

// Tab switches: gentle crossfade — navbar stays completely still
private fun tabEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))

private fun tabExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 180, easing = EaseOut))

@Composable
private fun FloatingPillNav(
    activeRoute: String,
    onNavigate: (String) -> Unit,
    onLogMeal: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(99.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(99.dp))
                .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(99.dp))
                .background(Color(0xFF262626))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPillItem(
                icon = AppIcons.Home,
                label = "Home",
                selected = activeRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )
            NavPillItem(
                icon = AppIcons.Heart,
                label = "Weight",
                selected = activeRoute == Screen.Weight.route,
                onClick = { onNavigate(Screen.Weight.route) }
            )

            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLogMeal
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = "Log Meal",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            NavPillItem(
                icon = AppIcons.BarChart,
                label = "History",
                selected = activeRoute == Screen.History.route,
                onClick = { onNavigate(Screen.History.route) }
            )
            NavPillItem(
                icon = AppIcons.Profile,
                label = "Profile",
                selected = activeRoute == Screen.Profile.route,
                onClick = { onNavigate(Screen.Profile.route) }
            )
        }
    }
}

@Composable
private fun NavPillItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (selected) OrangeAccent else OnSurfaceDim,
        animationSpec = tween(durationMillis = 200, easing = EaseOut),
        label = "navTint"
    )

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (selected) OrangeAccent else Color.Transparent)
        )
    }
}
