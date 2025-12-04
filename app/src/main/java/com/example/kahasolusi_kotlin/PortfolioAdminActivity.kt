package com.example.kahasolusi_kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioAdminBinding
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager
import kotlinx.coroutines.launch

class PortfolioAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioAdminBinding
    private lateinit var storageManager: LocalStorageManager
    private val portfolioRepo = FirebasePortfolioRepository()
    
    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var portfolioId: String? = null
    private var oldImageUri: String? = null

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
        binding.actTechStack.setText(intent.getStringExtra("portfolio_techstack"), false)

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

        // Setup Tech Stack Dropdown
        val techStackList = arrayOf(
            "React JS",
            "Vue JS",
            "Angular",
            "Laravel",
            "Node JS",
            "Python Django",
            "Kotlin Android",
            "Flutter",
            "React Native",
            "Java Spring Boot",
            "PHP",
            ".NET Core"
        )
        val techStackAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, techStackList)
        binding.actTechStack.setAdapter(techStackAdapter)
    }

    private fun setupClickListeners() {
        // Upload gambar card
        binding.cvUploadGambar.setOnClickListener {
            openImagePicker()
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

    private fun savePortfolio() {
        val judul = binding.etJudulProject.text.toString().trim()
        val kategori = binding.actKategori.text.toString().trim()
        val lokasi = binding.etLokasi.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val techStack = binding.actTechStack.text.toString().trim()

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
                    // Create Portfolio object
                    val portfolio = Portfolio(
                        id = portfolioId ?: "", // Will be set by Firestore
                        judul = judul,
                        kategori = kategori.ifEmpty { "Uncategorized" },
                        lokasi = lokasi,
                        deskripsi = deskripsi,
                        gambarUri = imageUri,
                        techStack = techStack
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
