package com.example.batabung.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.batabung.ui.analytics.AnalyticsScreen
import com.example.batabung.ui.chat.ChatScreen
import com.example.batabung.ui.dashboard.DashboardScreen
import com.example.batabung.ui.transaction.AddTransactionScreen
import com.example.batabung.ui.transaction.HistoryScreen

/**
 * Navigation routes untuk aplikasi.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_TRANSACTION = "add_transaction"
    const val HISTORY = "history"
    const val CHAT = "chat"
    const val ANALYTICS = "analytics"
}

/**
 * Durasi animasi transisi dalam milidetik.
 */
private const val ANIMATION_DURATION = 400

/**
 * Enter transition: Slide in dari kanan dengan fade.
 */
private fun enterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Exit transition: Slide out ke kiri dengan fade.
 */
private fun exitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION / 2,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Pop enter transition: Slide in dari kiri dengan fade.
 */
private fun popEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Pop exit transition: Slide out ke kanan dengan fade.
 */
private fun popExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION / 2,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Vertical enter transition: Slide up dengan fade untuk modal screens.
 */
private fun verticalEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight / 2 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Vertical exit transition: Slide down dengan fade untuk modal screens.
 */
private fun verticalExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight / 2 },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION / 2,
            easing = FastOutSlowInEasing
        )
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

/**
 * Navigation graph untuk aplikasi BaTabung dengan smooth transitions.
 */
@Composable
fun BaTabungNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.CHAT
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) {
        // Dashboard - main screen, menggunakan transisi default horizontal
        composable(
            route = Routes.DASHBOARD,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            DashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Routes.ANALYTICS)
                }
            )
        }
        
        // Add Transaction - modal style dengan vertical slide + bounce
        composable(
            route = Routes.ADD_TRANSACTION,
            enterTransition = { verticalEnterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { verticalExitTransition() }
        ) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // History - horizontal slide
        composable(
            route = Routes.HISTORY,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Chat - special transition dengan scaling untuk immersive feel
        composable(
            route = Routes.CHAT,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = tween(ANIMATION_DURATION / 2)
                )
            }
        ) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD)
                }
            )
        }
        
        // Analytics - fade + scale untuk data visualization feel
        composable(
            route = Routes.ANALYTICS,
            enterTransition = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = {
                fadeOut(animationSpec = tween(ANIMATION_DURATION / 2)) +
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                )
            }
        ) {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
