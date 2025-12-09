package com.example.kahasolusi_kotlin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager
import kotlinx.coroutines.launch

class TechnologyAdminActivity : ComponentActivity() {

    private lateinit var storageManager: LocalStorageManager
    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        storageManager = LocalStorageManager(this)

        val editMode = intent.getBooleanExtra("edit_mode", false)
        val technologyId = intent.getStringExtra("technology_id")
        val technologyNama = intent.getStringExtra("technology_nama") ?: ""
        val technologyIcon = intent.getStringExtra("technology_icon") ?: ""

        setContent {
            MaterialTheme {
                TechnologyAdminScreen(
                    editMode = editMode,
                    technologyId = technologyId,
                    initialNama = technologyNama,
                    initialIcon = technologyIcon,
                    onBack = { finish() },
                    onSave = { nama, iconUri ->
                        saveTechnology(editMode, technologyId, nama, iconUri, technologyIcon)
                    }
                )
            }
        }
    }

    private fun saveTechnology(
        editMode: Boolean,
        technologyId: String?,
        nama: String,
        iconUri: Uri?,
        oldIconUri: String
    ) {
        lifecycleScope.launch {
            try {
                val iconResult = if (editMode && iconUri.toString() == oldIconUri) {
                    Result.success(oldIconUri)
                } else if (iconUri != null) {
                    storageManager.saveTechnologyIcon(iconUri)
                } else {
                    Result.failure(Exception("No icon selected"))
                }

                iconResult.onSuccess { savedIconUri ->
                    val technology = Technology(
                        id = technologyId ?: "",
                        nama = nama,
                        iconUri = savedIconUri
                    )

                    val result = if (editMode && technologyId != null) {
                        technologyRepo.updateTechnology(technologyId, technology)
                    } else {
                        technologyRepo.addTechnology(technology)
                    }

                    result.onSuccess {
                        val message = if (editMode) "Teknologi berhasil diupdate!" else "Teknologi berhasil disimpan!"
                        Toast.makeText(this@TechnologyAdminActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { e ->
                        Toast.makeText(this@TechnologyAdminActivity, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }.onFailure { e ->
                    Toast.makeText(this@TechnologyAdminActivity, "Gagal menyimpan icon: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TechnologyAdminActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnologyAdminScreen(
    editMode: Boolean,
    technologyId: String?,
    initialNama: String,
    initialIcon: String,
    onBack: () -> Unit,
    onSave: (String, Uri?) -> Unit
) {
    var nama by remember { mutableStateOf(initialNama) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(if (initialIcon.isNotEmpty()) Uri.parse(initialIcon) else null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedIconUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "Edit Teknologi" else "Tambah Teknologi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFF4F5),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Icon
            item {
                Text(
                    text = "Icon Teknologi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedIconUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedIconUri),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Text("Pilih Icon", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Nama Teknologi
            item {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Teknologi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (nama.isEmpty() || selectedIconUri == null) {
                                return@Button
                            }
                            isLoading = true
                            onSave(nama, selectedIconUri)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "Menyimpan..." else "Simpan")
                    }
                }
            }
        }
    }
}
