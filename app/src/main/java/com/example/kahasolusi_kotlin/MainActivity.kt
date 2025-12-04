package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
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

        // Setup navigation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Display welcome message only if user is logged in
        if (sharedPreferencesHelper.isLoggedIn()) {
            displayWelcomeMessage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Show/hide menu items based on login status
        menu?.findItem(R.id.action_profile)?.isVisible = sharedPreferencesHelper.isLoggedIn()
        menu?.findItem(R.id.action_portfolio_admin)?.isVisible = sharedPreferencesHelper.isLoggedIn()
        menu?.findItem(R.id.action_logout)?.isVisible = sharedPreferencesHelper.isLoggedIn()
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        val userData = sharedPreferencesHelper.getLoggedInUser()
        userData?.let {
            Toast.makeText(this, "Selamat datang, ${it.fullName}!", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun performLogout() {
        sharedPreferencesHelper.logoutUser()
        Toast.makeText(this, "Logout berhasil. Silakan login untuk mengakses fitur lengkap.", Toast.LENGTH_LONG).show()
        
        // Refresh menu
        invalidateOptionsMenu()
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
    
    private fun navigateToPortfolioAdmin() {
        val intent = Intent(this, PortfolioListActivity::class.java)
        startActivity(intent)
    }
}