package com.example.kahasolusi_kotlin

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kahasolusi_kotlin.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferences helper
        sharedPreferencesHelper = SharedPreferencesHelper(this)

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
        // Register button
        binding.btnRegister.setOnClickListener {
            performRegister()
        }
        
        // Already have account link
        binding.tvAlreadyHaveAccount.setOnClickListener {
            finish() // Go back to login
        }
        
        // Terms checkbox
        binding.cbAgreeTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnRegister.isEnabled = isChecked
        }
    }

    private fun performRegister() {
        val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim() ?: ""
        val agreeTerms = binding.cbAgreeTerms.isChecked

        // Clear previous errors
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilUsername.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Nama lengkap tidak boleh kosong"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email tidak valid"
            return
        }

        if (username.isEmpty()) {
            binding.tilUsername.error = "Username tidak boleh kosong"
            return
        }

        if (username.length < 3) {
            binding.tilUsername.error = "Username minimal 3 karakter"
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Konfirmasi password tidak sama"
            return
        }

        if (!agreeTerms) {
            Toast.makeText(this, "Anda harus menyetujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek apakah username sudah ada
        if (sharedPreferencesHelper.isUserExists(username)) {
            binding.tilUsername.error = "Username sudah terdaftar"
            Toast.makeText(this, "Username sudah digunakan. Silakan pilih username lain.", Toast.LENGTH_LONG).show()
            return
        }

        // Register user baru
        val isRegistered = sharedPreferencesHelper.registerUser(fullName, email, username, password)
        
        if (isRegistered) {
            Toast.makeText(this, "Registrasi berhasil! Silakan login dengan akun Anda.", Toast.LENGTH_LONG).show()
            finish() // Kembali ke login
        } else {
            Toast.makeText(this, "Registrasi gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
        }
    }
}