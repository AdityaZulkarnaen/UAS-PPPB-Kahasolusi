package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager
import com.example.kahasolusi_kotlin.ui.dashboard.DashboardFragment
import com.example.kahasolusi_kotlin.ui.home.HomeFragment
import com.example.kahasolusi_kotlin.ui.notifications.NotificationsFragment
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val authManager = FirebaseAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display welcome message only if user is logged in
        if (authManager.isUserLoggedIn()) {
            displayWelcomeMessage()
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    authManager = authManager,
                    onNavigateToLogin = { navigateToLogin() },
                    onNavigateToPortfolioAdmin = { navigateToPortfolioAdmin() },
                    onLogout = { performLogout() },
                    onShowProfile = { showUserProfile() }
                )
            }
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
            recreate() // Recreate activity to refresh UI
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.Task)
    object Technology : Screen("technology", "Technology", Icons.Default.Code)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authManager: FirebaseAuthManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToPortfolioAdmin: () -> Unit,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showMenu by remember { mutableStateOf(false) }
    val isLoggedIn = authManager.isUserLoggedIn()

    val screens = listOf(Screen.Home, Screen.Portfolio, Screen.Technology)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedScreen.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFF4F5),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("Login") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToLogin()
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    showMenu = false
                                    onShowProfile()
                                },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("CMS Management") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToPortfolioAdmin()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFEFF4F5)
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF031117),
                            selectedTextColor = Color(0xFF031117),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFB4E4F7)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            FragmentContent(selectedScreen = selectedScreen)
        }
    }
}

@Composable
fun FragmentContent(selectedScreen: Screen) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply {
                id = android.view.View.generateViewId()
            }
        },
        update = { view ->
            val activity = view.context as? MainActivity ?: return@AndroidView
            val fragmentManager = activity.supportFragmentManager
            val fragment = when (selectedScreen) {
                Screen.Home -> HomeFragment()
                Screen.Portfolio -> DashboardFragment()
                Screen.Technology -> NotificationsFragment()
            }
            
            fragmentManager.beginTransaction()
                .replace(view.id, fragment)
                .commit()
        }
    )
}