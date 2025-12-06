package com.example.kahasolusi_kotlin

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioDetailBinding
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import kotlinx.coroutines.launch

class PortfolioDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioDetailBinding
    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Portfolio"

        // Get portfolio data from intent
        loadPortfolioData()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadPortfolioData() {
        val portfolioId = intent.getStringExtra("portfolio_id") ?: ""
        val portfolioJudul = intent.getStringExtra("portfolio_judul") ?: ""
        val portfolioKategori = intent.getStringExtra("portfolio_kategori") ?: ""
        val portfolioLokasi = intent.getStringExtra("portfolio_lokasi") ?: ""
        val portfolioDeskripsi = intent.getStringExtra("portfolio_deskripsi") ?: ""
        val portfolioGambar = intent.getStringExtra("portfolio_gambar") ?: ""
        val portfolioTechStack = intent.getStringArrayListExtra("portfolio_techstack") ?: arrayListOf()

        Log.d("PortfolioDetail", "=== Portfolio Detail Debug ===")
        Log.d("PortfolioDetail", "Portfolio: $portfolioJudul")
        Log.d("PortfolioDetail", "Tech Stack Raw: $portfolioTechStack")
        Log.d("PortfolioDetail", "Tech Stack Size: ${portfolioTechStack.size}")
        portfolioTechStack.forEachIndexed { index, id ->
            Log.d("PortfolioDetail", "  [$index] = '$id' (length: ${id.length}, trimmed: '${id.trim()}')")
        }

        // Set data to views
        binding.apply {
            tvTitle.text = portfolioJudul
            tvLocation.text = portfolioLokasi.ifEmpty { "Location not specified" }
            chipCategory.text = portfolioKategori.ifEmpty { "Uncategorized" }
            tvDescription.text = portfolioDeskripsi

            // Load image
            if (portfolioGambar.isNotEmpty()) {
                try {
                    val uri = Uri.parse(portfolioGambar)
                    ivPortfolioPreview.setImageURI(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Load tech stack details
        loadTechStackDetails(portfolioTechStack)
    }

    private fun loadTechStackDetails(techStackIds: List<String>) {
        Log.d("PortfolioDetail", "Tech Stack IDs: $techStackIds")
        
        if (techStackIds.isEmpty()) {
            Log.d("PortfolioDetail", "Tech stack list is empty")
            Toast.makeText(this, "No tech stack available", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var loadedCount = 0
            techStackIds.forEach { techId ->
                Log.d("PortfolioDetail", "Loading tech ID: $techId")
                
                // Try to get by ID first
                val result = technologyRepo.getTechnologyById(techId)
                result.onSuccess { technology ->
                    if (technology != null) {
                        Log.d("PortfolioDetail", "Loaded tech by ID: ${technology.nama}")
                        addTechStackBadge(technology.nama, technology.iconUri)
                        loadedCount++
                    } else {
                        // Fallback: try to find by name (for old data)
                        Log.d("PortfolioDetail", "ID not found, searching by name: $techId")
                        findTechnologyByName(techId)?.let { tech ->
                            Log.d("PortfolioDetail", "Found tech by name: ${tech.nama}")
                            addTechStackBadge(tech.nama, tech.iconUri)
                            loadedCount++
                        } ?: run {
                            Log.w("PortfolioDetail", "Technology not found by ID or name: $techId")
                        }
                    }
                }.onFailure { e ->
                    Log.e("PortfolioDetail", "Error loading tech $techId: ${e.message}", e)
                    // Fallback: try to find by name
                    findTechnologyByName(techId)?.let { tech ->
                        Log.d("PortfolioDetail", "Found tech by name (after error): ${tech.nama}")
                        addTechStackBadge(tech.nama, tech.iconUri)
                        loadedCount++
                    }
                }
            }
            
            if (loadedCount == 0) {
                Toast.makeText(this@PortfolioDetailActivity, 
                    "Failed to load tech stack details", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun findTechnologyByName(name: String): Technology? {
        return try {
            val result = technologyRepo.getAllTechnologies()
            var foundTech: Technology? = null
            result.onSuccess { technologies ->
                foundTech = technologies.find { 
                    it.nama.equals(name, ignoreCase = true) 
                }
            }
            foundTech
        } catch (e: Exception) {
            Log.e("PortfolioDetail", "Error finding by name: ${e.message}", e)
            null
        }
    }

    private fun addTechStackBadge(techName: String, iconUri: String) {
        Log.d("PortfolioDetail", "Adding badge: $techName with icon: $iconUri")
        
        runOnUiThread {
            try {
                val badgeView = LayoutInflater.from(this)
                    .inflate(R.layout.item_tech_stack_badge, binding.gridTechStack, false)

                val techIcon = badgeView.findViewById<ImageView>(R.id.iv_tech_icon)
                val techNameView = badgeView.findViewById<TextView>(R.id.tv_tech_name)

                techNameView.text = techName

                // Load icon
                if (iconUri.isNotEmpty()) {
                    try {
                        techIcon.setImageURI(Uri.parse(iconUri))
                    } catch (e: Exception) {
                        Log.w("PortfolioDetail", "Failed to load icon: ${e.message}")
                        techIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    techIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                binding.gridTechStack.addView(badgeView)
                Log.d("PortfolioDetail", "Badge added successfully")
            } catch (e: Exception) {
                Log.e("PortfolioDetail", "Error adding badge: ${e.message}", e)
            }
        }
    }
}
