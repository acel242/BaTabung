package com.example.batabung.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.batabung.ui.analytics.AnalyticsScreen
import com.example.batabung.ui.auth.LoginScreen
import com.example.batabung.ui.bank.AddBankScreen
import com.example.batabung.ui.bank.BankDetailScreen
import com.example.batabung.ui.home.HomeScreen
import com.example.batabung.ui.transaction.AddTransactionScreen
import com.example.batabung.ui.transaction.HistoryScreen

/**
 * Navigation routes untuk aplikasi.
 * Start destination: Login (Supabase Auth)
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val BANK_DETAIL = "bank_detail/{bankId}"
    const val ADD_TRANSACTION = "add_transaction/{bankId}"
    const val HISTORY = "history/{bankId}"
    const val ANALYTICS = "analytics"
    const val ADD_BANK = "add_bank"
    
    fun bankDetail(bankId: String) = "bank_detail/$bankId"
    fun addTransaction(bankId: String) = "add_transaction/$bankId"
    fun history(bankId: String) = "history/$bankId"
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
 * Start destination: Login screen dengan Supabase Auth.
 */
@Composable
fun BaTabungNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) {
        // Login Screen - Entry point dengan Supabase Auth
        composable(
            route = Routes.LOGIN,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        // Clear login from back stack
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen - Unified Bank/E-Wallet list with Bottom Navigation
        composable(
            route = Routes.HOME,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(
                onNavigateToBankDetail = { bankId ->
                    navController.navigate(Routes.bankDetail(bankId))
                },
                onNavigateToAddBank = {
                    navController.navigate(Routes.ADD_BANK)
                },
                onLogout = {
                    // Navigate back to login, clearing back stack
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        
        // Bank Detail - main Tabungan Dashboard
        composable(
            route = Routes.BANK_DETAIL,
            arguments = listOf(
                navArgument("bankId") { type = NavType.StringType }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId") ?: ""
            BankDetailScreen(
                bankId = bankId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Routes.addTransaction(bankId))
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.history(bankId))
                }
            )
        }
        
        // Add Transaction - modal style dengan vertical slide + bounce
        composable(
            route = Routes.ADD_TRANSACTION,
            arguments = listOf(
                navArgument("bankId") { type = NavType.StringType }
            ),
            enterTransition = { verticalEnterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { verticalExitTransition() }
        ) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId") ?: ""
            AddTransactionScreen(
                bankId = bankId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // History - horizontal slide
        composable(
            route = Routes.HISTORY,
            arguments = listOf(
                navArgument("bankId") { type = NavType.StringType }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val bankId = backStackEntry.arguments?.getString("bankId") ?: ""
            HistoryScreen(
                bankId = bankId,
                onNavigateBack = {
                    navController.popBackStack()
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
        
        // Add Bank - modal style (no jenis parameter, selection inside form)
        composable(
            route = Routes.ADD_BANK,
            enterTransition = { verticalEnterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { verticalExitTransition() }
        ) {
            AddBankScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
