package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private val authManager = FirebaseAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                LoginScreen(
                    onBackClick = { finish() },
                    onLoginSuccess = { navigateToMainActivity() },
                    onLoginError = { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    },
                    onLogin = { email, password -> performLogin(email, password) }
                )
            }
        }
    }

    
    private suspend fun performLogin(email: String, password: String): Result<Unit> {
        return try {
            val result = authManager.loginUser(email, password)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Login gagal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLoginError: (String) -> Unit,
    onLogin: suspend (String, String) -> Result<Unit>
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.logo_kahasolusi),
                            contentDescription = "Kahasolusi Logo",
                            modifier = Modifier.height(40.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFF4F5)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Selamat Datang!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            
            Text(
                text = "Masuk untuk melanjutkan",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
            
            // Forgot Password
            TextButton(
                onClick = { /* Toast message */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Lupa Password?", color = Color(0xFF1976D2))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Button
            Button(
                onClick = {
                    // Validation
                    emailError = when {
                        email.isEmpty() -> "Email tidak boleh kosong"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                            "Format email tidak valid"
                        else -> null
                    }
                    
                    passwordError = when {
                        password.isEmpty() -> "Password tidak boleh kosong"
                        password.length < 6 -> "Password minimal 6 karakter"
                        else -> null
                    }
                    
                    if (emailError == null && passwordError == null) {
                        isLoading = true
                        scope.launch {
                            val result = onLogin(email, password)
                            isLoading = false
                            if (result.isSuccess) {
                                onLoginSuccess()
                            } else {
                                // Tampilkan error message
                                val exception = result.exceptionOrNull()
                                errorMessage = when {
                                    exception?.message?.contains("password", ignoreCase = true) == true -> 
                                        "Password yang Anda masukkan salah. Silakan coba lagi."
                                    exception?.message?.contains("user not found", ignoreCase = true) == true ||
                                    exception?.message?.contains("no user", ignoreCase = true) == true -> 
                                        "Email tidak terdaftar. Silakan periksa kembali email Anda."
                                    exception?.message?.contains("network", ignoreCase = true) == true -> 
                                        "Tidak ada koneksi internet. Periksa koneksi Anda."
                                    exception?.message?.contains("too many", ignoreCase = true) == true -> 
                                        "Terlalu banyak percobaan login. Coba lagi nanti."
                                    else -> 
                                        "Login gagal: ${exception?.message ?: "Terjadi kesalahan tidak diketahui"}"
                                }
                                showErrorDialog = true
                                onLoginError(errorMessage)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
        }
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Error",
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = "Login Gagal",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }
}