package com.example.kahasolusi_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private val authManager = FirebaseAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                RegisterScreen(
                    onBackClick = { finish() },
                    onRegisterSuccess = { 
                        Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                        finish() 
                    },
                    onLoginClick = { finish() },
                    onRegister = { email, password, fullName -> performRegister(email, password, fullName) }
                )
            }
        }
    }
    
    private suspend fun performRegister(email: String, password: String, fullName: String): Result<Unit> {
        return try {
            val result = authManager.registerUser(email, password, fullName)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Registrasi gagal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}