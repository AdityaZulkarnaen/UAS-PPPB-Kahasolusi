package com.example.kahasolusi_kotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.databinding.ActivityTechnologyAdminBinding

class TechnologyAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTechnologyAdminBinding
    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var technologyId: String? = null

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

        // Check if edit mode
        editMode = intent.getBooleanExtra("edit_mode", false)
        if (editMode) {
            loadTechnologyData()
        }

        setupClickListeners()
    }

    private fun loadTechnologyData() {
        technologyId = intent.getStringExtra("technology_id")
        binding.etNamaTeknologi.setText(intent.getStringExtra("technology_nama"))

        val iconUri = intent.getStringExtra("technology_icon")
        if (!iconUri.isNullOrEmpty()) {
            try {
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
        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

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

        // Buat object Technology
        val id = if (editMode && technologyId != null) {
            technologyId!!
        } else {
            System.currentTimeMillis().toString()
        }

        val technology = Technology(
            id = id,
            nama = nama,
            iconUri = selectedImageUri.toString()
        )

        // Simpan ke SharedPreferences
        saveToStorage(technology)

        val message = if (editMode) "Teknologi berhasil diupdate!" else "Teknologi berhasil disimpan!"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveToStorage(technology: Technology) {
        val sharedPref = getSharedPreferences("TechnologyPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Simpan data technology
        editor.putString("technology_${technology.id}_nama", technology.nama)
        editor.putString("technology_${technology.id}_icon", technology.iconUri)

        editor.apply()
    }
}
