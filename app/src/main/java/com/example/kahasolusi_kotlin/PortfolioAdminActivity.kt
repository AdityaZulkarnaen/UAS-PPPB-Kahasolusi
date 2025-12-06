package com.example.kahasolusi_kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioAdminBinding
import com.example.kahasolusi_kotlin.databinding.DialogSelectTechStackBinding
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager
import com.example.kahasolusi_kotlin.ui.portfolio.adapter.TechStackSelectableAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class PortfolioAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioAdminBinding
    private lateinit var storageManager: LocalStorageManager
    private val portfolioRepo = FirebasePortfolioRepository()
    private val technologyRepo = FirebaseTechnologyRepository()
    
    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var portfolioId: String? = null
    private var oldImageUri: String? = null
    
    private val selectedTechStacks = mutableListOf<Technology>()
    private var allTechnologies = listOf<Technology>()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivPreview.setImageURI(uri)
                binding.tvUploadHint.text = "Gambar dipilih"
                binding.tvUploadSubhint.text = "Klik untuk ganti gambar"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize storage manager
        storageManager = LocalStorageManager(this)

        // Setup ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Portfolio Admin"

        // Check if edit mode
        editMode = intent.getBooleanExtra("edit_mode", false)
        if (editMode) {
            loadPortfolioData()
            supportActionBar?.title = "Edit Portfolio"
        }

        setupUI()
        setupClickListeners()
        loadTechnologies()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadPortfolioData() {
        portfolioId = intent.getStringExtra("portfolio_id")
        binding.etJudulProject.setText(intent.getStringExtra("portfolio_judul"))
        binding.actKategori.setText(intent.getStringExtra("portfolio_kategori"), false)
        binding.etLokasi.setText(intent.getStringExtra("portfolio_lokasi"))
        binding.etDeskripsi.setText(intent.getStringExtra("portfolio_deskripsi"))
        
        // Load tech stacks - now expecting ArrayList<String>
        val techStackIds = intent.getStringArrayListExtra("portfolio_techstack") ?: arrayListOf()
        // Will be populated after technologies are loaded
        
        val gambarUri = intent.getStringExtra("portfolio_gambar")
        if (!gambarUri.isNullOrEmpty()) {
            try {
                oldImageUri = gambarUri
                selectedImageUri = Uri.parse(gambarUri)
                binding.ivPreview.setImageURI(selectedImageUri)
                binding.tvUploadHint.text = "Gambar dipilih"
                binding.tvUploadSubhint.text = "Klik untuk ganti gambar"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupUI() {
        // Setup Kategori Dropdown
        val kategoriList = arrayOf(
            "Website Development",
            "Mobile Development",
            "Desktop Application",
            "System Integration",
            "E-Commerce",
            "CMS Development"
        )
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriList)
        binding.actKategori.setAdapter(kategoriAdapter)
    }

    private fun setupClickListeners() {
        // Upload gambar card
        binding.cvUploadGambar.setOnClickListener {
            openImagePicker()
        }

        // Add tech stack button
        binding.btnAddTechStack.setOnClickListener {
            showTechStackDialog()
        }

        // Simpan button
        binding.btnSimpan.setOnClickListener {
            savePortfolio()
        }

        // Batal button
        binding.btnBatal.setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadTechnologies() {
        lifecycleScope.launch {
            val result = technologyRepo.getAllTechnologies()
            result.onSuccess { technologies ->
                allTechnologies = technologies
                
                // Load selected tech stacks if in edit mode
                if (editMode) {
                    val techStackIds = intent.getStringArrayListExtra("portfolio_techstack") ?: arrayListOf()
                    selectedTechStacks.clear()
                    selectedTechStacks.addAll(
                        allTechnologies.filter { techStackIds.contains(it.id) }
                    )
                    updateTechStackChips()
                }
            }.onFailure {
                Toast.makeText(this@PortfolioAdminActivity, 
                    "Gagal memuat technology: ${it.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTechStackDialog() {
        val dialogBinding = DialogSelectTechStackBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        // Setup RecyclerView
        val adapter = TechStackSelectableAdapter { selectedTechs ->
            // Update will be handled when user clicks "Selesai"
        }
        
        dialogBinding.rvTechStack.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvTechStack.adapter = adapter
        
        // Set currently selected technologies
        adapter.setSelectedTechnologies(selectedTechStacks.map { it.id })
        adapter.submitList(allTechnologies)
        
        // Search functionality
        dialogBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    adapter.submitList(allTechnologies)
                } else {
                    val filtered = allTechnologies.filter { 
                        it.nama.contains(query, ignoreCase = true) 
                    }
                    adapter.submitList(filtered)
                }
            }
        })

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Done button
        dialogBinding.btnDone.setOnClickListener {
            selectedTechStacks.clear()
            selectedTechStacks.addAll(adapter.getSelectedTechnologies())
            updateTechStackChips()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateTechStackChips() {
        binding.chipGroupTechStack.removeAllViews()
        
        selectedTechStacks.forEach { tech ->
            val chip = Chip(this).apply {
                text = tech.nama
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    selectedTechStacks.remove(tech)
                    updateTechStackChips()
                }
            }
            binding.chipGroupTechStack.addView(chip)
        }
    }

    private fun savePortfolio() {
        val judul = binding.etJudulProject.text.toString().trim()
        val kategori = binding.actKategori.text.toString().trim()
        val lokasi = binding.etLokasi.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        // Validasi
        if (judul.isEmpty()) {
            binding.etJudulProject.error = "Judul project harus diisi"
            binding.etJudulProject.requestFocus()
            return
        }

        if (deskripsi.isEmpty()) {
            binding.etDeskripsi.error = "Deskripsi harus diisi"
            binding.etDeskripsi.requestFocus()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Silakan pilih gambar project", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedTechStacks.isEmpty()) {
            Toast.makeText(this, "Silakan pilih minimal satu tech stack", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Save image to local storage
                val imageResult = if (editMode && selectedImageUri.toString() == oldImageUri) {
                    // Image not changed, use old URI
                    Result.success(oldImageUri!!)
                } else {
                    // New image selected
                    storageManager.savePortfolioImage(selectedImageUri!!)
                }

                imageResult.onSuccess { imageUri ->
                    // Create Portfolio object with tech stack IDs
                    val techStackIds = selectedTechStacks.map { it.id }
                    
                    val portfolio = Portfolio(
                        id = portfolioId ?: "", // Will be set by Firestore
                        judul = judul,
                        kategori = kategori.ifEmpty { "Uncategorized" },
                        lokasi = lokasi,
                        deskripsi = deskripsi,
                        gambarUri = imageUri,
                        techStack = techStackIds
                    )

                    // Save to Firestore
                    val result = if (editMode && portfolioId != null) {
                        portfolioRepo.updatePortfolio(portfolioId!!, portfolio)
                    } else {
                        portfolioRepo.addPortfolio(portfolio)
                    }

                    result.onSuccess {
                        showLoading(false)
                        val message = if (editMode) "Portfolio berhasil diupdate!" else "Portfolio berhasil disimpan!"
                        Toast.makeText(this@PortfolioAdminActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { e ->
                        showLoading(false)
                        Toast.makeText(this@PortfolioAdminActivity, 
                            "Gagal menyimpan: ${e.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                }.onFailure { e ->
                    showLoading(false)
                    Toast.makeText(this@PortfolioAdminActivity, 
                        "Gagal menyimpan gambar: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@PortfolioAdminActivity, 
                    "Error: ${e.message}", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnBatal.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "Simpan"
    }

    private fun saveToStorage(portfolio: Portfolio) {
        // Deprecated - now using Firebase
    }
}
