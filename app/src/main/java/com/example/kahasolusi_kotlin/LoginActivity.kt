package com.example.kahasolusi_kotlin

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.databinding.ActivityLoginBinding
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authManager = FirebaseAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Setup header with gradient
        setupGradientHeader()
        
        // Setup back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupGradientHeader() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        
        val customView = layoutInflater.inflate(R.layout.toolbar_title, null)
        val titleView = customView.findViewById<TextView>(android.R.id.text1) ?: 
                       (customView as? android.view.ViewGroup)?.getChildAt(0) as? TextView
        
        titleView?.post {
            val paint = titleView.paint
            val width = paint.measureText(titleView.text.toString())
            
            val textShader = LinearGradient(
                0f, 0f, width, titleView.height.toFloat(),
                intArrayOf(
                    android.graphics.Color.parseColor("#0E2144"),
                    android.graphics.Color.parseColor("#1E2642"),
                    android.graphics.Color.parseColor("#F16724"), 
                ),
                floatArrayOf(0f, 0.22f, 0.9f),
                Shader.TileMode.CLAMP
            )
            
            titleView.paint.shader = textShader
            titleView.invalidate()
        }
        
        supportActionBar?.customView = customView
    }

    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            performLogin()
        }
        
        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur reset password akan segera tersedia", Toast.LENGTH_SHORT).show()
        }
        
        // Register text click
        binding.tvRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""

        // Clear previous errors
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        // Validasi input
        if (email.isEmpty()) {
            binding.tilUsername.error = "Email tidak boleh kosong"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilUsername.error = "Format email tidak valid"
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return
        }

        // Show loading state
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Masuk..."

        // Login with Firebase
        lifecycleScope.launch {
            try {
                val result = authManager.loginUser(email, password)
                if (result.isSuccess) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login berhasil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMainActivity()
                } else {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Masuk"
                    val error = result.exceptionOrNull()?.message ?: "Login gagal"
                    Toast.makeText(
                        this@LoginActivity,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}