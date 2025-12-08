package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.composables.CMSScreen
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import kotlinx.coroutines.launch

class PortfolioListActivity : ComponentActivity() {

    private val portfolioRepo = FirebasePortfolioRepository()
    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var portfolioList by remember { mutableStateOf<List<Portfolio>>(emptyList()) }
                var technologyList by remember { mutableStateOf<List<Technology>>(emptyList()) }
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(selectedTab) {
                    isLoading = true
                    if (selectedTab == 0) {
                        portfolioRepo.getAllPortfolios().onSuccess {
                            portfolioList = it
                        }
                    } else {
                        technologyRepo.getAllTechnologies().onSuccess {
                            technologyList = it
                        }
                    }
                    isLoading = false
                }

                CMSScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    portfolioList = portfolioList,
                    technologyList = technologyList,
                    isLoading = isLoading,
                    onBackClick = { finish() },
                    onAddClick = {
                        if (selectedTab == 0) {
                            startActivity(Intent(this, PortfolioAdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, TechnologyAdminActivity::class.java))
                        }
                    },
                    onEditPortfolio = { portfolio ->
                        val intent = Intent(this, PortfolioAdminActivity::class.java).apply {
                            putExtra("edit_mode", true)
                            putExtra("portfolio_id", portfolio.id)
                            putExtra("portfolio_judul", portfolio.judul)
                            putExtra("portfolio_kategori", portfolio.kategori)
                            putExtra("portfolio_lokasi", portfolio.lokasi)
                            putExtra("portfolio_deskripsi", portfolio.deskripsi)
                            putExtra("portfolio_gambar", portfolio.gambarUri)
                            putStringArrayListExtra("portfolio_techstack", ArrayList(portfolio.getTechStackList()))
                        }
                        startActivity(intent)
                    },
                    onDeletePortfolio = { portfolio ->
                        lifecycleScope.launch {
                            portfolioRepo.deletePortfolio(portfolio.id).onSuccess {
                                Toast.makeText(this@PortfolioListActivity, "Portfolio berhasil dihapus", Toast.LENGTH_SHORT).show()
                                // Reload data
                                portfolioRepo.getAllPortfolios().onSuccess {
                                    portfolioList = it
                                }
                            }
                        }
                    },
                    onEditTechnology = { technology ->
                        val intent = Intent(this, TechnologyAdminActivity::class.java).apply {
                            putExtra("edit_mode", true)
                            putExtra("technology_id", technology.id)
                            putExtra("technology_nama", technology.nama)
                            putExtra("technology_icon", technology.iconUri)
                        }
                        startActivity(intent)
                    },
                    onDeleteTechnology = { technology ->
                        lifecycleScope.launch {
                            technologyRepo.deleteTechnology(technology.id).onSuccess {
                                Toast.makeText(this@PortfolioListActivity, "Teknologi berhasil dihapus", Toast.LENGTH_SHORT).show()
                                // Reload data
                                technologyRepo.getAllTechnologies().onSuccess {
                                    technologyList = it
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
