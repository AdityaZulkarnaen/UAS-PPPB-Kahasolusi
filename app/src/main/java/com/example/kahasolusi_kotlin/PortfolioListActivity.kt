package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioListBinding
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class PortfolioListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioListBinding
    private lateinit var storageManager: LocalStorageManager
    private val portfolioRepo = FirebasePortfolioRepository()
    private val technologyRepo = FirebaseTechnologyRepository()
    
    private lateinit var portfolioAdapter: PortfolioAdapter
    private lateinit var technologyAdapter: TechnologyAdapter
    private val portfolioList = mutableListOf<Portfolio>()
    private val technologyList = mutableListOf<Technology>()
    private var currentTab = 0 // 0 = Portfolio, 1 = Technology

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize storage manager
        storageManager = LocalStorageManager(this)

        // Setup ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Portfolio"

        setupUI()
        loadPortfolios()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kali activity di-resume (setelah add/edit)
        if (currentTab == 0) {
            loadPortfolios()
        } else {
            loadTechnologies()
        }
    }

    private fun setupUI() {
        // Setup Portfolio RecyclerView
        portfolioAdapter = PortfolioAdapter(
            portfolioList,
            onEditClick = { portfolio ->
                editPortfolio(portfolio)
            },
            onDeleteClick = { portfolio ->
                confirmDeletePortfolio(portfolio)
            }
        )

        // Setup Technology RecyclerView
        technologyAdapter = TechnologyAdapter(
            technologyList,
            onEditClick = { technology ->
                editTechnology(technology)
            },
            onDeleteClick = { technology ->
                confirmDeleteTechnology(technology)
            }
        )

        binding.rvPortfolio.apply {
            layoutManager = LinearLayoutManager(this@PortfolioListActivity)
            adapter = portfolioAdapter
        }

        // Setup Tab Layout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                when (currentTab) {
                    0 -> {
                        // Portfolio tab
                        binding.rvPortfolio.adapter = portfolioAdapter
                        binding.fabAddPortfolio.text = "Tambah Portfolio"
                        loadPortfolios()
                    }
                    1 -> {
                        // Technology tab
                        binding.rvPortfolio.adapter = technologyAdapter
                        binding.fabAddPortfolio.text = "Tambah Teknologi"
                        loadTechnologies()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        // Floating Action Button - Tambah Portfolio/Technology
        binding.fabAddPortfolio.setOnClickListener {
            if (currentTab == 0) {
                // Portfolio tab
                val intent = Intent(this, PortfolioAdminActivity::class.java)
                startActivity(intent)
            } else {
                // Technology tab
                val intent = Intent(this, TechnologyAdminActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadPortfolios() {
        portfolioList.clear()
        showLoading(true)

        lifecycleScope.launch {
            val result = portfolioRepo.getAllPortfolios()
            result.onSuccess { portfolios ->
                showLoading(false)
                portfolioList.addAll(portfolios)
                updateUI()
            }.onFailure { e ->
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, 
                    "Gagal load portfolio: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }
    }

    private fun editPortfolio(portfolio: Portfolio) {
        val intent = Intent(this, PortfolioAdminActivity::class.java)
        intent.putExtra("portfolio_id", portfolio.id)
        intent.putExtra("portfolio_judul", portfolio.judul)
        intent.putExtra("portfolio_kategori", portfolio.kategori)
        intent.putExtra("portfolio_lokasi", portfolio.lokasi)
        intent.putExtra("portfolio_deskripsi", portfolio.deskripsi)
        intent.putExtra("portfolio_gambar", portfolio.gambarUri)
        intent.putExtra("portfolio_techstack", portfolio.techStack)
        intent.putExtra("edit_mode", true)
        startActivity(intent)
    }

    private fun confirmDeletePortfolio(portfolio: Portfolio) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Portfolio")
            .setMessage("Apakah Anda yakin ingin menghapus \"${portfolio.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePortfolio(portfolio)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePortfolio(portfolio: Portfolio) {
        showLoading(true)

        lifecycleScope.launch {
            // Delete from Firestore
            val result = portfolioRepo.deletePortfolio(portfolio.id)
            result.onSuccess {
                // Delete image from local storage
                if (portfolio.gambarUri.isNotEmpty()) {
                    storageManager.deleteImage(portfolio.gambarUri)
                }
                
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, "Portfolio dihapus", Toast.LENGTH_SHORT).show()
                loadPortfolios()
            }.onFailure { e ->
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, 
                    "Gagal menghapus: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Technology Methods
    private fun loadTechnologies() {
        technologyList.clear()
        showLoading(true)

        lifecycleScope.launch {
            val result = technologyRepo.getAllTechnologies()
            result.onSuccess { technologies ->
                showLoading(false)
                technologyList.addAll(technologies)
                updateUI()
            }.onFailure { e ->
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, 
                    "Gagal load teknologi: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }
    }

    private fun editTechnology(technology: Technology) {
        val intent = Intent(this, TechnologyAdminActivity::class.java)
        intent.putExtra("technology_id", technology.id)
        intent.putExtra("technology_nama", technology.nama)
        intent.putExtra("technology_icon", technology.iconUri)
        intent.putExtra("edit_mode", true)
        startActivity(intent)
    }

    private fun confirmDeleteTechnology(technology: Technology) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Teknologi")
            .setMessage("Apakah Anda yakin ingin menghapus \"${technology.nama}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteTechnology(technology)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTechnology(technology: Technology) {
        showLoading(true)

        lifecycleScope.launch {
            // Delete from Firestore
            val result = technologyRepo.deleteTechnology(technology.id)
            result.onSuccess {
                // Delete icon from local storage
                if (technology.iconUri.isNotEmpty()) {
                    storageManager.deleteImage(technology.iconUri)
                }
                
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, "Teknologi dihapus", Toast.LENGTH_SHORT).show()
                loadTechnologies()
            }.onFailure { e ->
                showLoading(false)
                Toast.makeText(this@PortfolioListActivity, 
                    "Gagal menghapus: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        val list = if (currentTab == 0) portfolioList else technologyList
        
        if (list.isEmpty()) {
            binding.rvPortfolio.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvPortfolio.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            
            if (currentTab == 0) {
                portfolioAdapter.notifyDataSetChanged()
            } else {
                technologyAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.fabAddPortfolio.isEnabled = !isLoading
        // You can add progress bar here if needed
    }
}
