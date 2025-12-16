package com.example.batabung.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.util.FormatUtils
import kotlinx.coroutines.delay

/**
 * Dashboard Screen - Layar utama aplikasi dengan animasi smooth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BaTabung",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "AI Assistant"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Animated FAB with bounce effect
            var fabVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(500)
                fabVisible = true
            }
            
            AnimatedVisibility(
                visible = fabVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                FloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Tambah Transaksi"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.hasTabungan) {
            // Animated onboarding
            AnimatedEmptyStateScreen(
                onCreateTabungan = { viewModel.createDefaultTabungan() },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            DashboardContent(
                uiState = uiState,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onDeleteTransaksi = { viewModel.deleteTransaksi(it) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun AnimatedEmptyStateScreen(
    onCreateTabungan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon with pulsing effect
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Savings,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 200)) + 
                    slideInVertically(
                        animationSpec = tween(500, delayMillis = 200),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "Selamat Datang di BaTabung!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 400)) + 
                    slideInVertically(
                        animationSpec = tween(500, delayMillis = 400),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "Mulai menabung dan kelola keuanganmu dengan mudah",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500, delayMillis = 600)) + 
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialScale = 0.8f
                    )
        ) {
            Button(
                onClick = onCreateTabungan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat Tabungan Pertama",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToHistory: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onDeleteTransaksi: (Transaksi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saldo Card with entrance animation
        item {
            AnimatedCard(delay = 0) {
                SaldoCard(
                    saldo = uiState.saldo,
                    tabunganNama = uiState.tabungan?.nama ?: "",
                    target = uiState.tabungan?.target,
                    progress = uiState.progressPercentage
                )
            }
        }
        
        // Quick Stats with entrance animation
        item {
            AnimatedCard(delay = 100) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Pemasukan",
                        amount = uiState.pemasukanBulanIni,
                        icon = Icons.Filled.TrendingUp,
                        iconTint = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pengeluaran",
                        amount = uiState.pengeluaranBulanIni,
                        icon = Icons.Filled.TrendingDown,
                        iconTint = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Quick Actions with entrance animation
        item {
            AnimatedCard(delay = 200) {
                QuickActionsRow(
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
            }
        }
        
        // Recent Transactions Header
        item {
            AnimatedCard(delay = 300) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaksi Terakhir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("Lihat Semua")
                    }
                }
            }
        }
        
        // Recent Transactions
        if (uiState.recentTransaksi.isEmpty()) {
            item {
                AnimatedCard(delay = 400) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada transaksi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            itemsIndexed(uiState.recentTransaksi) { index, transaksi ->
                AnimatedTransactionItem(
                    transaksi = transaksi,
                    onDelete = { onDeleteTransaksi(transaksi) },
                    delay = 400 + (index * 50)
                )
            }
        }
        
        // Bottom Spacer for FAB
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * Wrapper composable for animated card entrance.
 */
@Composable
private fun AnimatedCard(
    delay: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(300)
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}

/**
 * Animated transaction item with slide-in and swipe effects.
 */
@Composable
private fun AnimatedTransactionItem(
    transaksi: Transaksi,
    onDelete: () -> Unit,
    delay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + 
                slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetX = { it / 2 }
                )
    ) {
        TransactionItem(
            transaksi = transaksi,
            onDelete = onDelete,
            modifier = modifier
        )
    }
}

@Composable
private fun SaldoCard(
    saldo: Long,
    tabunganNama: String,
    target: Long?,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Animated progress indicator
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tabunganNama,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Savings,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Saldo Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                // Animated counter effect for saldo
                Text(
                    text = FormatUtils.formatRupiah(saldo),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                if (target != null && target > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target: ${FormatUtils.formatRupiah(target)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    amount: Long,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = FormatUtils.formatRupiah(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Bulan ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNavigateToHistory: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Outlined.History,
            label = "Riwayat",
            onClick = onNavigateToHistory,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Outlined.Analytics,
            label = "Analisis",
            onClick = onNavigateToAnalytics,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    OutlinedCard(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaksi: Transaksi,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaksi.jenis == JenisTransaksi.MASUK
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountPrefix = if (isIncome) "+" else "-"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaksi.kategori.ifEmpty { if (isIncome) "Pemasukan" else "Pengeluaran" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatUtils.formatDate(transaksi.tanggal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (!transaksi.catatan.isNullOrBlank()) {
                        Text(
                            text = " • ${transaksi.catatan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Amount
            Text(
                text = "$amountPrefix${FormatUtils.formatRupiah(transaksi.jumlah)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
