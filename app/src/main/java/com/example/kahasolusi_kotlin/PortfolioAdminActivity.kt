package com.example.kahasolusi_kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.databinding.ActivityPortfolioAdminBinding

class PortfolioAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioAdminBinding
    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var portfolioId: String? = null

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

        // Buat object Portfolio
        val id = if (editMode && portfolioId != null) {
            portfolioId!!
        } else {
            System.currentTimeMillis().toString()
        }

        val portfolio = Portfolio(
            id = id,
            judul = judul,
            kategori = kategori.ifEmpty { "Uncategorized" },
            lokasi = lokasi,
            deskripsi = deskripsi,
            gambarUri = selectedImageUri.toString(),
            techStack = techStack
        )

        // Simpan ke SharedPreferences atau Database (implementasi nanti)
        saveToStorage(portfolio)

        val message = if (editMode) "Portfolio berhasil diupdate!" else "Portfolio berhasil disimpan!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveToStorage(portfolio: Portfolio) {
        // TODO: Implementasi save ke SharedPreferences atau Room Database
        // Untuk sementara, hanya log data
        println("Portfolio saved: $portfolio")
        
        // Simpan ke SharedPreferences sebagai contoh sederhana
        val sharedPref = getSharedPreferences("PortfolioPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        
        // Simpan counter untuk tracking jumlah portfolio
        val currentCount = sharedPref.getInt("portfolio_count", 0)
        editor.putInt("portfolio_count", currentCount + 1)
        
        // Simpan data portfolio (simplified - untuk production gunakan Room Database)
        editor.putString("portfolio_${portfolio.id}_judul", portfolio.judul)
        editor.putString("portfolio_${portfolio.id}_kategori", portfolio.kategori)
        editor.putString("portfolio_${portfolio.id}_lokasi", portfolio.lokasi)
        editor.putString("portfolio_${portfolio.id}_deskripsi", portfolio.deskripsi)
        editor.putString("portfolio_${portfolio.id}_gambar", portfolio.gambarUri)
        editor.putString("portfolio_${portfolio.id}_techstack", portfolio.techStack)
        
        editor.apply()
    }
}
