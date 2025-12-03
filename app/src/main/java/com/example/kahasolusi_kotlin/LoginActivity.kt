package com.example.kahasolusi_kotlin

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kahasolusi_kotlin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferences helper
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        
        setupUI()
        setupClickListeners()
        loadRememberedCredentials()
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
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }
        
        // Register text click
        binding.tvRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        // Remember Me checkbox
        binding.cbRememberMe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Credentials will be remembered", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRememberedCredentials() {
        // Load remembered credentials jika ada
        val rememberedCredentials = sharedPreferencesHelper.getRememberedCredentials()
        rememberedCredentials?.let { (username, password) ->
            binding.etUsername.setText(username)
            binding.etPassword.setText(password)
            binding.cbRememberMe.isChecked = true
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val rememberMe = binding.cbRememberMe.isChecked

        // Validasi input
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username tidak boleh kosong"
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }

        // Clear previous errors
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        // Coba login menggunakan SharedPreferences
        if (sharedPreferencesHelper.loginUser(username, password, rememberMe)) {
            // Login berhasil
            val message = if (rememberMe) {
                "Login berhasil! Kredensial akan diingat."
            } else {
                "Login berhasil!"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            navigateToMainActivity()
        } else {
            // Login gagal
            if (sharedPreferencesHelper.isUserExists(username)) {
                binding.tilPassword.error = "Password salah"
                Toast.makeText(this, "Password yang Anda masukkan salah", Toast.LENGTH_LONG).show()
            } else {
                binding.tilUsername.error = "Username tidak ditemukan"
                Toast.makeText(this, "Username tidak terdaftar. Silakan daftar terlebih dahulu.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}