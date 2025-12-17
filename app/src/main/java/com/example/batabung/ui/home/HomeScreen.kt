package com.example.batabung.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.ui.auth.AuthViewModel
import com.example.batabung.ui.bank.BankListContent
import com.example.batabung.ui.bank.BankViewModel
import com.example.batabung.ui.chat.ChatScreen
import com.example.batabung.ui.chat.ChatViewModel

/**
 * Sealed class untuk item bottom navigation.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Bank : BottomNavItem(
        route = "bank_tab",
        title = "Bank",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance
    )
    
    data object ChatAI : BottomNavItem(
        route = "chat_tab",
        title = "Chat AI",
        selectedIcon = Icons.Filled.SmartToy,
        unselectedIcon = Icons.Outlined.SmartToy
    )
}

/**
 * Home Screen dengan Bottom Navigation.
 * Menampilkan unified Bank/E-Wallet list dan Chat AI.
 * Includes logout button di TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBankDetail: (String) -> Unit,
    onNavigateToAddBank: () -> Unit,
    onLogout: () -> Unit,
    bankViewModel: BankViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(BottomNavItem.Bank, BottomNavItem.ChatAI)
    
    // Logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Apakah Anda yakin ingin keluar? Data Anda tetap tersimpan di cloud.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut {
                            onLogout()
                        }
                    }
                ) {
                    Text("Keluar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BaTabung",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { paddingValues ->
        // Content based on selected tab
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    // Moving right
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    // Moving left
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "tab_content"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    // Bank List Tab
                    BankListContent(
                        modifier = Modifier.padding(paddingValues),
                        onNavigateToBankDetail = onNavigateToBankDetail,
                        onNavigateToAddBank = onNavigateToAddBank,
                        viewModel = bankViewModel
                    )
                }
                1 -> {
                    // Chat AI Tab
                    ChatScreen(
                        modifier = Modifier.padding(paddingValues),
                        viewModel = chatViewModel,
                        onNavigateBack = { selectedTab = 0 },
                        onNavigateToDashboard = { selectedTab = 0 },
                        onNavigateToBankList = { selectedTab = 0 },
                        onLogout = {
                            authViewModel.signOut {
                                onLogout()
                            }
                        },
                        showTopBar = false
                    )
                }
            }
        }
    }
}
