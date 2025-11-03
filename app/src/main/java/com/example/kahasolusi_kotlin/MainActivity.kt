package com.example.kahasolusi_kotlin

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kahasolusi_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SharedPreferences helper
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        
        // Check if user is logged in
        if (!sharedPreferencesHelper.isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin()
            return
        }

        // Setup custom title with gradient
        setupGradientTitle()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Display welcome message with user data
        displayWelcomeMessage()
    }

    private fun setupGradientTitle() {
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
    
    private fun displayWelcomeMessage() {
        val userData = sharedPreferencesHelper.getLoggedInUser()
        userData?.let {
            Toast.makeText(this, "Selamat datang, ${it.fullName}!", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            R.id.action_profile -> {
                showUserProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun performLogout() {
        sharedPreferencesHelper.logoutUser()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }
    
    private fun showUserProfile() {
        val userData = sharedPreferencesHelper.getLoggedInUser()
        userData?.let {
            val message = "Profil Pengguna:\n" +
                    "Nama: ${it.fullName}\n" +
                    "Email: ${it.email}\n" +
                    "Username: ${it.username}"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}