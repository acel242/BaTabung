package com.example.batabung.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.batabung.data.repository.AuthState

// Custom Colors for this screen
private val DarkBackground = Color(0xFF121418) // Dark Charcoal/Navy
private val SoftGreen = Color(0xFF57D6A8) // Mint
private val DarkInputBackground = Color(0xFF1E2329)
private val MutedWhite = Color(0xFFE0E0E0)
private val MutedGrey = Color(0xFF9CA3AF)

/**
 * Login/Register screen dengan email & password authentication.
 * Menggunakan Supabase Auth untuk autentikasi.
 * Redesigned: Calm, Minimalist, Modern.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Navigate ke home ketika authenticated dan sync selesai
    LaunchedEffect(authState, uiState.syncComplete) {
        if (authState is AuthState.Authenticated && uiState.syncComplete) {
            viewModel.resetSyncComplete()
            onLoginSuccess()
        }
    }
    
    // Jika sudah authenticated saat screen dibuka, langsung navigate
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated && !uiState.isLoading) {
            onLoginSuccess()
        }
    }
    
    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A202C), // Slightly lighter top
                            DarkBackground     // Darker bottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo Section
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SoftGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Logo",
                        tint = SoftGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "BaTabung", // App Name
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = SoftGreen
                )
                
                Text(
                    text = "Selamat Datang",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Form Section
                
                // Email Input
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    placeholder = { Text("Email", color = MutedGrey) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MutedGrey
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBackground,
                        unfocusedContainerColor = DarkInputBackground,
                        disabledContainerColor = DarkInputBackground,
                        cursorColor = SoftGreen,
                        focusedBorderColor = SoftGreen.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Input
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    placeholder = { Text("Password", color = MutedGrey) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MutedGrey
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                                tint = MutedGrey
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBackground,
                        unfocusedContainerColor = DarkInputBackground,
                        disabledContainerColor = DarkInputBackground,
                        cursorColor = SoftGreen,
                        focusedBorderColor = SoftGreen.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (uiState.isSignUp) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = {
                            focusManager.clearFocus()
                            if (!uiState.isSignUp) viewModel.signIn()
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                
                // Confirm Password (only for Sign Up)
                AnimatedVisibility(
                    visible = uiState.isSignUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.updateConfirmPassword(it) },
                            placeholder = { Text("Konfirmasi Password", color = MutedGrey) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MutedGrey
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) "Sembunyikan password" else "Tampilkan password",
                                        tint = MutedGrey
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkInputBackground,
                                unfocusedContainerColor = DarkInputBackground,
                                disabledContainerColor = DarkInputBackground,
                                cursorColor = SoftGreen,
                                focusedBorderColor = SoftGreen.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.signUp()
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Error Message
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
                
                // Login/Register Button
                Button(
                    onClick = {
                        if (uiState.isSignUp) viewModel.signUp() else viewModel.signIn()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SoftGreen.copy(alpha = 0.5f)
                    ),
                    shape = CircleShape, // Pill shape
                    enabled = !uiState.isLoading,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isSignUp) "Daftar" else "Masuk",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Toggle Login/Register
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (uiState.isSignUp) "Sudah punya akun? " else "Belum punya akun? ",
                        color = MutedGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = { viewModel.toggleSignUpMode() },
                        enabled = !uiState.isLoading,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (uiState.isSignUp) "Masuk" else "Daftar",
                            color = SoftGreen,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
