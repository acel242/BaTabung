package com.example.batabung.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.ai.model.ChatMessage
import com.example.batabung.util.FormatUtils
import kotlinx.coroutines.delay

/**
 * Chat Screen - Layar untuk berinteraksi dengan AI Assistant.
 * Dengan animasi smooth yang immersive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToBankList: () -> Unit = {},
    onLogout: () -> Unit = {},
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // Show API key dialog if not ready and no saved key
    LaunchedEffect(uiState.isAIReady, uiState.savedApiKey) {
        // Tunggu sebentar untuk load dari DataStore
        delay(500)
        if (!uiState.isAIReady && uiState.savedApiKey.isNullOrBlank()) {
            showApiKeyDialog = true
        }
    }
    
    // Smooth scroll to bottom when new message added
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            delay(100) // Small delay for animation
            listState.animateScrollToItem(
                index = uiState.messages.size - 1,
                scrollOffset = 0
            )
        }
    }
    
    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Animated avatar with pulsing effect
                            AnimatedAIAvatar(isOnline = uiState.isAIReady)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "BaTabung AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically()
                                ) {
                                    Text(
                                        text = if (uiState.isAIReady) "Online" else "Offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.isAIReady) 
                                            Color(0xFF4CAF50) 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToDashboard) {
                            Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard")
                        }
                        IconButton(onClick = onNavigateToBankList) {
                            Icon(Icons.Outlined.AccountBalance, contentDescription = "Kelola Bank")
                        }
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Clear chat")
                        }
                        IconButton(onClick = { showApiKeyDialog = true }) {
                            Icon(Icons.Outlined.Key, contentDescription = "API Key")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Outlined.Logout, contentDescription = "Logout")
                        }
                    }
                )
            }
        ) { paddingValues ->
            ChatContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                listState = listState,
                onShowApiKeyDialog = { showApiKeyDialog = true }
            )
        }
    } else {
        // When embedded in HomeScreen, no scaffold, just content
        ChatContent(
            modifier = modifier,
            uiState = uiState,
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            },
            listState = listState,
            onShowApiKeyDialog = { showApiKeyDialog = true }
        )
    }
    
    // API Key Dialog with animation
    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onConfirm = { apiKey ->
                viewModel.initializeAI(apiKey)
                showApiKeyDialog = false
            }
        )
    }
}

/**
 * Chat content composable - extracted for reuse with and without toolbar.
 */
@Composable
private fun ChatContent(
    modifier: Modifier = Modifier,
    uiState: ChatUiState,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onShowApiKeyDialog: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with AI status for embedded mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedAIAvatar(isOnline = uiState.isAIReady)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BaTabung AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.isAIReady) "Online" else "Offline - Klik ikon kunci untuk set API Key",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.isAIReady) 
                        Color(0xFF4CAF50) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            IconButton(onClick = onShowApiKeyDialog) {
                Icon(Icons.Outlined.Key, contentDescription = "API Key")
            }
        }
        
        HorizontalDivider()
        
        // Messages List with smooth animations
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = uiState.messages,
                key = { it.hashCode() }
            ) { message ->
                AnimatedChatBubble(message = message)
            }
        }
        
        // Input Area with animation
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ChatInputArea(
                value = inputText,
                onValueChange = onInputChange,
                onSend = onSend,
                isLoading = uiState.isLoading,
                isAIReady = uiState.isAIReady
            )
        }
    }
}

/**
 * Animated AI Avatar with pulsing effect when online.
 */
@Composable
private fun AnimatedAIAvatar(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier
            .size(36.dp)
            .scale(if (isOnline) scale else 1f)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SmartToy,
            contentDescription = null,
            tint = Color.White.copy(alpha = if (isOnline) glowAlpha + 0.3f else 0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Animated chat bubble with entrance animation.
 */
@Composable
private fun AnimatedChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetX = { if (message is ChatMessage.UserMessage) it else -it }
                ) +
                expandVertically(animationSpec = tween(200)),
        exit = fadeOut() + shrinkVertically()
    ) {
        ChatBubble(message = message, modifier = modifier)
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    when (message) {
        is ChatMessage.UserMessage -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        is ChatMessage.AIMessage -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        is ChatMessage.Loading -> {
            LoadingBubble(modifier = modifier)
        }
        
        is ChatMessage.Error -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated loading bubble with bouncing dots.
 */
@Composable
private fun LoadingBubble(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val offset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                delayMillis = index * 150,
                                easing = EaseInOutSine
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer { translationY = offset }
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.6f + (index * 0.15f)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    isAIReady: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text(
                        if (isAIReady) "Tanya tentang tabunganmu..." 
                        else "Set API key untuk mulai chat"
                    ) 
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                enabled = isAIReady && !isLoading
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Animated send button
            val buttonScale by animateFloatAsState(
                targetValue = if (value.isNotBlank() && isAIReady && !isLoading) 1f else 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "button"
            )
            
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && isAIReady && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .scale(buttonScale)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Kirim"
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Gemini API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Masukkan API key dari Google AI Studio untuk mengaktifkan fitur chat AI.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti saja")
            }
        }
    )
}
