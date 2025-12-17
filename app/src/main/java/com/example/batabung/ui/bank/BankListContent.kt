package com.example.batabung.ui.bank

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisBank

/**
 * BankListContent - Konten unified list untuk Bank dan E-Wallet.
 * Digunakan di dalam HomeScreen dengan bottom navigation.
 */
@Composable
fun BankListContent(
    modifier: Modifier = Modifier,
    onNavigateToBankDetail: (String) -> Unit,
    onNavigateToAddBank: () -> Unit,
    viewModel: BankViewModel = hiltViewModel()
) {
    val banksFiltered by viewModel.banksFiltered.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    var bankToDelete by remember { mutableStateOf<Bank?>(null) }
    
    // Load all banks on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAllBanks()
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter == FilterType.ALL,
                    onClick = { viewModel.setFilter(FilterType.ALL) },
                    label = { Text("Semua") },
                    leadingIcon = if (currentFilter == FilterType.ALL) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = currentFilter == FilterType.BANK,
                    onClick = { viewModel.setFilter(FilterType.BANK) },
                    label = { Text("Bank") },
                    leadingIcon = if (currentFilter == FilterType.BANK) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else {
                        { Icon(Icons.Default.AccountBalance, contentDescription = null, Modifier.size(18.dp)) }
                    }
                )
                FilterChip(
                    selected = currentFilter == FilterType.EWALLET,
                    onClick = { viewModel.setFilter(FilterType.EWALLET) },
                    label = { Text("E-Wallet") },
                    leadingIcon = if (currentFilter == FilterType.EWALLET) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else {
                        { Icon(Icons.Default.Wallet, contentDescription = null, Modifier.size(18.dp)) }
                    }
                )
            }
            
            if (banksFiltered.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💳",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Belum Ada Akun Keuangan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tambahkan bank atau e-wallet untuk memulai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = banksFiltered,
                        key = { it.id }
                    ) { bank ->
                        BankCardItem(
                            bank = bank,
                            onClick = { onNavigateToBankDetail(bank.id) },
                            onToggleActive = { viewModel.toggleBankActive(bank) },
                            onDelete = { bankToDelete = bank }
                        )
                    }
                    
                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
        
        // FAB
        ExtendedFloatingActionButton(
            onClick = onNavigateToAddBank,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Tambah Akun") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
    
    // Delete confirmation dialog
    bankToDelete?.let { bank ->
        AlertDialog(
            onDismissRequest = { bankToDelete = null },
            title = { Text("Hapus ${bank.displayName}?") },
            text = { Text("Bank dan semua transaksi terkait akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBank(bank)
                        bankToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { bankToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * BankCardItem - Card untuk menampilkan info bank/e-wallet.
 */
@Composable
internal fun BankCardItem(
    bank: Bank,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bank.isAktif)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (bank.isAktif) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bank icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = when (bank.jenis) {
                    JenisBank.BANK -> MaterialTheme.colorScheme.primaryContainer
                    JenisBank.EWALLET -> MaterialTheme.colorScheme.tertiaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (bank.jenis) {
                            JenisBank.BANK -> Icons.Default.AccountBalance
                            JenisBank.EWALLET -> Icons.Default.Wallet
                        },
                        contentDescription = null,
                        tint = when (bank.jenis) {
                            JenisBank.BANK -> MaterialTheme.colorScheme.onPrimaryContainer
                            JenisBank.EWALLET -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Bank info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bank.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bank.isAktif)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (bank.alias.isNotBlank()) {
                    Text(
                        text = bank.alias,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (bank.isAktif)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (bank.jenis == JenisBank.BANK) "Bank" else "E-Wallet",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bank.isAktif)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    }
                    if (!bank.isAktif) {
                        Text(
                            text = "• Nonaktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // Actions
            Row {
                Switch(
                    checked = bank.isAktif,
                    onCheckedChange = { onToggleActive() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
