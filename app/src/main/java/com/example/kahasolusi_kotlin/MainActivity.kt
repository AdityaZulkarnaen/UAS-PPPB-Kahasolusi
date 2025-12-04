package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kahasolusi_kotlin.databinding.ActivityMainBinding
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authManager = FirebaseAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup navigation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Display welcome message only if user is logged in
        if (authManager.isUserLoggedIn()) {
            displayWelcomeMessage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Show/hide menu items based on login status
        val isLoggedIn = authManager.isUserLoggedIn()
        
        // Login menu - only show when NOT logged in
        menu?.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        
        // User menus - only show when logged in
        menu?.findItem(R.id.action_profile)?.isVisible = isLoggedIn
        menu?.findItem(R.id.action_portfolio_admin)?.isVisible = isLoggedIn
        menu?.findItem(R.id.action_logout)?.isVisible = isLoggedIn
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                navigateToLogin()
                true
            }
            R.id.action_profile -> {
                showUserProfile()
                true
            }
            R.id.action_portfolio_admin -> {
                navigateToPortfolioAdmin()
                true
            }
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun displayWelcomeMessage() {
        val currentUser = authManager.getCurrentUser()
        currentUser?.let {
            val displayName = it.displayName ?: it.email ?: "User"
            Toast.makeText(this, "Selamat datang, $displayName!", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            authManager.logoutUser()
            Toast.makeText(
                this@MainActivity,
                "Logout berhasil. Silakan login untuk mengakses fitur lengkap.",
                Toast.LENGTH_LONG
            ).show()
            
            // Refresh menu
            invalidateOptionsMenu()
        }
    }
    
    private fun showUserProfile() {
        val currentUser = authManager.getCurrentUser()
        currentUser?.let {
            val displayName = it.displayName ?: "Tidak tersedia"
            val email = it.email ?: "Tidak tersedia"
            val message = "Profil Pengguna:\n" +
                    "Nama: $displayName\n" +
                    "Email: $email\n" +
                    "UID: ${it.uid}"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun navigateToPortfolioAdmin() {
        val intent = Intent(this, PortfolioListActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}