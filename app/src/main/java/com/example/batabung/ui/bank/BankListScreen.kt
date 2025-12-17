package com.example.batabung.ui.bank

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankListScreen(
    jenisBank: String = "BANK",
    onNavigateBack: () -> Unit,
    onNavigateToAddBank: () -> Unit,
    onNavigateToBankDetail: (String) -> Unit,
    viewModel: BankViewModel = hiltViewModel()
) {
    val banksFiltered by viewModel.banksFiltered.collectAsState()
    var bankToDelete by remember { mutableStateOf<Bank?>(null) }
    
    // Load filtered banks
    LaunchedEffect(jenisBank) {
        viewModel.loadBanksByJenis(jenisBank)
    }
    
    val screenTitle = if (jenisBank == "BANK") "Daftar Bank" else "Daftar E-Wallet"
    val emptyTitle = if (jenisBank == "BANK") "Belum Ada Bank" else "Belum Ada E-Wallet"
    val emptySubtitle = if (jenisBank == "BANK") "Tambahkan bank untuk memulai" else "Tambahkan e-wallet untuk memulai"
    val fabText = if (jenisBank == "BANK") "Tambah Bank" else "Tambah E-Wallet"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBank,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(fabText) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (banksFiltered.isEmpty()) {
            // Empty state
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
                    Text(
                        text = if (jenisBank == "BANK") "🏦" else "💳",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = emptyTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = emptySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = banksFiltered,
                    key = { it.id }
                ) { bank ->
                    BankCard(
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

@Composable
private fun BankCard(
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
