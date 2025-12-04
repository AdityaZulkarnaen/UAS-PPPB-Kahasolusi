package com.example.kahasolusi_kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityTechnologyAdminBinding
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager
import kotlinx.coroutines.launch

class TechnologyAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTechnologyAdminBinding
    private lateinit var storageManager: LocalStorageManager
    private val technologyRepo = FirebaseTechnologyRepository()
    
    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var technologyId: String? = null
    private var oldIconUri: String? = null

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
        binding = ActivityTechnologyAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize storage manager
        storageManager = LocalStorageManager(this)

        // Setup ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Technology Admin"

        // Check if edit mode
        editMode = intent.getBooleanExtra("edit_mode", false)
        if (editMode) {
            loadTechnologyData()
            supportActionBar?.title = "Edit Technology"
        }

        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadTechnologyData() {
        technologyId = intent.getStringExtra("technology_id")
        binding.etNamaTeknologi.setText(intent.getStringExtra("technology_nama"))

        val iconUri = intent.getStringExtra("technology_icon")
        if (!iconUri.isNullOrEmpty()) {
            try {
                oldIconUri = iconUri
                selectedImageUri = Uri.parse(iconUri)
                binding.ivPreview.setImageURI(selectedImageUri)
                binding.tvUploadHint.text = "Gambar dipilih"
                binding.tvUploadSubhint.text = "Klik untuk ganti gambar"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupClickListeners() {
        // Upload gambar card
        binding.cvUploadGambar.setOnClickListener {
            openImagePicker()
        }

        // Simpan button
        binding.btnSimpan.setOnClickListener {
            saveTechnology()
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

    private fun saveTechnology() {
        val nama = binding.etNamaTeknologi.text.toString().trim()

        // Validasi
        if (nama.isEmpty()) {
            binding.etNamaTeknologi.error = "Nama teknologi harus diisi"
            binding.etNamaTeknologi.requestFocus()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Silakan pilih icon teknologi", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Save icon to local storage
                val iconResult = if (editMode && selectedImageUri.toString() == oldIconUri) {
                    // Icon not changed, use old URI
                    Result.success(oldIconUri!!)
                } else {
                    // New icon selected
                    storageManager.saveTechnologyIcon(selectedImageUri!!)
                }

                iconResult.onSuccess { iconUri ->
                    // Create Technology object
                    val technology = Technology(
                        id = technologyId ?: "", // Will be set by Firestore
                        nama = nama,
                        iconUri = iconUri
                    )

                    // Save to Firestore
                    val result = if (editMode && technologyId != null) {
                        technologyRepo.updateTechnology(technologyId!!, technology)
                    } else {
                        technologyRepo.addTechnology(technology)
                    }

                    result.onSuccess {
                        showLoading(false)
                        val message = if (editMode) "Teknologi berhasil diupdate!" else "Teknologi berhasil disimpan!"
                        Toast.makeText(this@TechnologyAdminActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { e ->
                        showLoading(false)
                        Toast.makeText(this@TechnologyAdminActivity, 
                            "Gagal menyimpan: ${e.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                }.onFailure { e ->
                    showLoading(false)
                    Toast.makeText(this@TechnologyAdminActivity, 
                        "Gagal menyimpan icon: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@TechnologyAdminActivity, 
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

    private fun saveToStorage(technology: Technology) {
        // Deprecated - now using Firebase
    }
}
