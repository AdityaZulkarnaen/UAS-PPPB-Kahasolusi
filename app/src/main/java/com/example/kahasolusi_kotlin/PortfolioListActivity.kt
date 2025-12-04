package com.example.kahasolusi_kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioListBinding
import com.google.android.material.tabs.TabLayout

class PortfolioListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioListBinding
    private lateinit var portfolioAdapter: PortfolioAdapter
    private lateinit var technologyAdapter: TechnologyAdapter
    private val portfolioList = mutableListOf<Portfolio>()
    private val technologyList = mutableListOf<Technology>()
    private var currentTab = 0 // 0 = Portfolio, 1 = Technology

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadPortfolios()
        setupClickListeners()
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
        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

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

        val sharedPref = getSharedPreferences("PortfolioPrefs", MODE_PRIVATE)
        val count = sharedPref.getInt("portfolio_count", 0)

        // Load all portfolios from SharedPreferences
        val allPrefs = sharedPref.all
        val portfolioIds = mutableSetOf<String>()

        // Extract unique portfolio IDs
        for (key in allPrefs.keys) {
            if (key.startsWith("portfolio_") && key.endsWith("_judul")) {
                val id = key.removePrefix("portfolio_").removeSuffix("_judul")
                portfolioIds.add(id)
            }
        }

        // Load each portfolio
        for (id in portfolioIds) {
            val judul = sharedPref.getString("portfolio_${id}_judul", "") ?: ""
            if (judul.isNotEmpty()) {
                val portfolio = Portfolio(
                    id = id,
                    judul = judul,
                    kategori = sharedPref.getString("portfolio_${id}_kategori", "") ?: "",
                    lokasi = sharedPref.getString("portfolio_${id}_lokasi", "") ?: "",
                    deskripsi = sharedPref.getString("portfolio_${id}_deskripsi", "") ?: "",
                    gambarUri = sharedPref.getString("portfolio_${id}_gambar", "") ?: "",
                    techStack = sharedPref.getString("portfolio_${id}_techstack", "") ?: ""
                )
                portfolioList.add(portfolio)
            }
        }

        // Update UI
        if (portfolioList.isEmpty()) {
            binding.rvPortfolio.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvPortfolio.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            portfolioAdapter.notifyDataSetChanged()
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
        val sharedPref = getSharedPreferences("PortfolioPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Remove portfolio data
        editor.remove("portfolio_${portfolio.id}_judul")
        editor.remove("portfolio_${portfolio.id}_kategori")
        editor.remove("portfolio_${portfolio.id}_lokasi")
        editor.remove("portfolio_${portfolio.id}_deskripsi")
        editor.remove("portfolio_${portfolio.id}_gambar")
        editor.remove("portfolio_${portfolio.id}_techstack")

        editor.apply()

        // Reload list
        loadPortfolios()

        Toast.makeText(this, "Portfolio dihapus", Toast.LENGTH_SHORT).show()
    }

    // Technology Methods
    private fun loadTechnologies() {
        technologyList.clear()

        val sharedPref = getSharedPreferences("TechnologyPrefs", MODE_PRIVATE)
        val allPrefs = sharedPref.all
        val technologyIds = mutableSetOf<String>()

        // Extract unique technology IDs
        for (key in allPrefs.keys) {
            if (key.startsWith("technology_") && key.endsWith("_nama")) {
                val id = key.removePrefix("technology_").removeSuffix("_nama")
                technologyIds.add(id)
            }
        }

        // Load each technology
        for (id in technologyIds) {
            val nama = sharedPref.getString("technology_${id}_nama", "") ?: ""
            if (nama.isNotEmpty()) {
                val technology = Technology(
                    id = id,
                    nama = nama,
                    iconUri = sharedPref.getString("technology_${id}_icon", "") ?: ""
                )
                technologyList.add(technology)
            }
        }

        // Update UI
        if (technologyList.isEmpty()) {
            binding.rvPortfolio.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvPortfolio.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            technologyAdapter.notifyDataSetChanged()
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
        val sharedPref = getSharedPreferences("TechnologyPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Remove technology data
        editor.remove("technology_${technology.id}_nama")
        editor.remove("technology_${technology.id}_icon")

        editor.apply()

        // Reload list
        loadTechnologies()

        Toast.makeText(this, "Teknologi dihapus", Toast.LENGTH_SHORT).show()
    }
}
