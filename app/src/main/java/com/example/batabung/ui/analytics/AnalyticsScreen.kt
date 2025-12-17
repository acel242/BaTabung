package com.example.batabung.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.ui.dashboard.DashboardViewModel
import com.example.batabung.util.FormatUtils

/**
 * Analytics Screen - Layar untuk melihat analisis keuangan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analisis Keuangan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly Summary Card
            item {
                MonthlySummaryCard(
                    pemasukan = uiState.pemasukanBulanIni,
                    pengeluaran = uiState.pengeluaranBulanIni
                )
            }
            
            
            // Analysis Insight
            item {
                AnalysisInsightCard(
                    pemasukan = uiState.pemasukanBulanIni,
                    pengeluaran = uiState.pengeluaranBulanIni,
                    saldo = uiState.saldo
                )
            }
            
            // Tips Card
            item {
                TipsCard()
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    pemasukan: Long,
    pengeluaran: Long,
    modifier: Modifier = Modifier
) {
    val selisih = pemasukan - pengeluaran
    val isPositive = selisih >= 0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Ringkasan ${FormatUtils.formatMonthYear(System.currentTimeMillis())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Pemasukan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pemasukan",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = FormatUtils.formatRupiah(pemasukan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pengeluaran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pengeluaran",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = FormatUtils.formatRupiah(pengeluaran),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Selisih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selisih",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isPositive) "+" else ""}${FormatUtils.formatRupiah(selisih)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun AnalysisInsightCard(
    pemasukan: Long,
    pengeluaran: Long,
    saldo: Long,
    modifier: Modifier = Modifier
) {
    val rasio = if (pemasukan > 0) (pengeluaran.toFloat() / pemasukan.toFloat()) * 100 else 0f
    val isHealthy = rasio <= 70
    
    val (title, message, icon, color) = when {
        pemasukan == 0L && pengeluaran == 0L -> {
            if (saldo > 0) {
                listOf(
                    "Belum Ada Transaksi Bulan Ini",
                    "Tambahkan transaksi untuk melihat analisis keuanganmu.",
                    Icons.Outlined.Info,
                    MaterialTheme.colorScheme.primary
                )
            } else {
                listOf(
                    "Mulai Menabung!",
                    "Tambahkan pemasukan pertamamu untuk memulai perjalanan menabung.",
                    Icons.Outlined.Savings,
                    MaterialTheme.colorScheme.primary
                )
            }
        }
        rasio <= 50 -> listOf(
            "Keuangan Sangat Sehat! 🎉",
            "Luar biasa! Pengeluaranmu hanya ${rasio.toInt()}% dari pemasukan. Terus pertahankan!",
            Icons.Filled.Celebration,
            Color(0xFF4CAF50)
        )
        rasio <= 70 -> listOf(
            "Keuangan Sehat 👍",
            "Bagus! Pengeluaranmu ${rasio.toInt()}% dari pemasukan. Masih dalam batas aman.",
            Icons.Filled.ThumbUp,
            Color(0xFF8BC34A)
        )
        rasio <= 90 -> listOf(
            "Perlu Perhatian ⚠️",
            "Pengeluaranmu ${rasio.toInt()}% dari pemasukan. Coba kurangi pengeluaran agar bisa menabung lebih banyak.",
            Icons.Filled.Warning,
            Color(0xFFFF9800)
        )
        else -> listOf(
            "Pengeluaran Tinggi 🚨",
            "Pengeluaranmu ${rasio.toInt()}% dari pemasukan. Perlu evaluasi prioritas pengeluaran.",
            Icons.Filled.ErrorOutline,
            Color(0xFFF44336)
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = (color as Color).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title as String,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message as String,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TargetProgressCard(
    saldo: Long,
    target: Long,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val remaining = target - saldo
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Target",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Terkumpul",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = FormatUtils.formatRupiah(saldo),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = FormatUtils.formatRupiah(target),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (remaining > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Kurang ${FormatUtils.formatRupiah(remaining)} lagi untuk mencapai target!",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tips Menabung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "💡 Sisihkan 20% dari pemasukan untuk tabungan sebelum dipakai untuk pengeluaran lainnya.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
            )
        }
    }
}
