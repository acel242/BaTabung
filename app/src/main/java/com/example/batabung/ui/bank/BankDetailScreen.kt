package com.example.batabung.ui.bank

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisBank
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.ui.dashboard.DashboardViewModel
import com.example.batabung.util.FormatUtils
import kotlinx.coroutines.delay

/**
 * Screen untuk menampilkan detail bank sebagai Dashboard Tabungan.
 * Struktur baru: 1 Bank = 1 Tabungan
 * 
 * Menampilkan:
 * - Nama bank + alias
 * - Saldo
 * - Total pemasukan & pengeluaran bulan ini
 * - Riwayat transaksi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailScreen(
    bankId: String,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    
    // Load bank data
    LaunchedEffect(bankId) {
        viewModel.loadBankById(bankId)
        delay(100)
        isVisible = true
    }
    
    val bank = uiState.bank
    val screenTitle = bank?.displayName ?: "Detail Bank"
    val bankIcon = if (bank?.jenis == JenisBank.EWALLET) Icons.Default.Wallet else Icons.Default.AccountBalance
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = screenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                visible = fabVisible && bank != null,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Transaksi") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bank == null) {
            // Bank not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Bank tidak ditemukan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Dashboard Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Saldo Card
                item {
                    AnimatedCard(delay = 0) {
                        SaldoCard(
                            saldo = uiState.saldo,
                            bank = bank
                        )
                    }
                }
                
                // Quick Stats
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
                
                // Quick Action: History
                item {
                    AnimatedCard(delay = 200) {
                        OutlinedCard(
                            onClick = onNavigateToHistory,
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
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lihat Semua Riwayat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Recent Transactions Header
                item {
                    AnimatedCard(delay = 300) {
                        Text(
                            text = "Transaksi Terakhir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                                        Text(
                                            text = "Tekan tombol + untuk menambah transaksi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
                            delay = 400 + (index * 50)
                        )
                    }
                }
                
                // Bottom Spacer for FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

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
        enter = fadeIn(tween(300)) + slideInVertically(
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

@Composable
private fun AnimatedTransactionItem(
    transaksi: Transaksi,
    delay: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetX = { it / 2 }
        )
    ) {
        TransactionItem(transaksi = transaksi)
    }
}

@Composable
private fun SaldoCard(
    saldo: Long,
    bank: Bank,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (bank.jenis == JenisBank.EWALLET) {
        listOf(
            Color(0xFF6366F1),
            Color(0xFF8B5CF6)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = bank.nama,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (bank.alias.isNotBlank()) {
                            Text(
                                text = bank.alias,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (bank.jenis == JenisBank.EWALLET) 
                            Icons.Filled.Wallet else Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Saldo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Text(
                    text = FormatUtils.formatRupiah(saldo),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun TransactionItem(
    transaksi: Transaksi,
    modifier: Modifier = Modifier
) {
    val isIncome = transaksi.jenis == JenisTransaksi.MASUK
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountPrefix = if (isIncome) "+" else "-"
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaksi.kategori.ifEmpty { if (isIncome) "Pemasukan" else "Pengeluaran" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = FormatUtils.formatDate(transaksi.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = "$amountPrefix${FormatUtils.formatRupiah(transaksi.jumlah)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
