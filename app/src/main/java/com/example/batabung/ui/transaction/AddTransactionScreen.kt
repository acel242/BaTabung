package com.example.batabung.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.ui.dashboard.DashboardViewModel

/**
 * Screen untuk menambah transaksi baru.
 * Struktur baru: Transaksi langsung ke Bank (1 Bank = 1 Tabungan)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    bankId: String,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var selectedJenis by remember { mutableStateOf(JenisTransaksi.MASUK) }
    var jumlah by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var showKategoriDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    // Load bank data
    LaunchedEffect(bankId) {
        viewModel.loadBankById(bankId)
    }
    
    val kategoriPemasukan = listOf("Gaji", "Bonus", "Hadiah", "Investasi", "Lainnya")
    val kategoriPengeluaran = listOf("Makan", "Transport", "Belanja", "Hiburan", "Tagihan", "Kesehatan", "Lainnya")
    val kategoris = if (selectedJenis == JenisTransaksi.MASUK) kategoriPemasukan else kategoriPengeluaran
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Jenis Transaksi Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionTypeButton(
                        text = "Pemasukan",
                        icon = Icons.Filled.TrendingUp,
                        isSelected = selectedJenis == JenisTransaksi.MASUK,
                        color = Color(0xFF4CAF50),
                        onClick = { 
                            selectedJenis = JenisTransaksi.MASUK
                            kategori = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TransactionTypeButton(
                        text = "Pengeluaran",
                        icon = Icons.Filled.TrendingDown,
                        isSelected = selectedJenis == JenisTransaksi.KELUAR,
                        color = Color(0xFFF44336),
                        onClick = { 
                            selectedJenis = JenisTransaksi.KELUAR
                            kategori = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Jumlah Input
            OutlinedTextField(
                value = jumlah,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        jumlah = it
                        showError = false
                    }
                },
                label = { Text("Jumlah (Rp)") },
                placeholder = { Text("Contoh: 100000") },
                leadingIcon = { 
                    Text(
                        "Rp",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showError && jumlah.isEmpty(),
                supportingText = if (showError && jumlah.isEmpty()) {
                    { Text("Jumlah tidak boleh kosong") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Kategori Dropdown
            ExposedDropdownMenuBox(
                expanded = showKategoriDropdown,
                onExpandedChange = { showKategoriDropdown = it }
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    placeholder = { Text("Pilih kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showKategoriDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = showKategoriDropdown,
                    onDismissRequest = { showKategoriDropdown = false }
                ) {
                    kategoris.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                kategori = item
                                showKategoriDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Catatan Input
            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan (Opsional)") },
                placeholder = { Text("Tambahkan catatan...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Submit Button
            Button(
                onClick = {
                    if (jumlah.isEmpty() || jumlah.toLongOrNull() == null || jumlah.toLong() <= 0) {
                        showError = true
                    } else {
                        viewModel.addTransaksi(
                            bankId = bankId,
                            jenis = selectedJenis,
                            jumlah = jumlah.toLong(),
                            kategori = kategori.ifEmpty { if (selectedJenis == JenisTransaksi.MASUK) "Pemasukan" else "Pengeluaran" },
                            catatan = catatan.ifEmpty { null }
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simpan Transaksi",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TransactionTypeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) color else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
