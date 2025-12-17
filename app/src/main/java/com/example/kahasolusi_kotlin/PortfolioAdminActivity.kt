package com.example.kahasolusi_kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import com.example.kahasolusi_kotlin.firebase.CloudflareR2Manager
import com.example.kahasolusi_kotlin.config.R2Config
import kotlinx.coroutines.launch

class PortfolioAdminActivity : ComponentActivity() {

    private lateinit var storageManager: CloudflareR2Manager
    private val portfolioRepo = FirebasePortfolioRepository()
    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        storageManager = CloudflareR2Manager(this)

        val editMode = intent.getBooleanExtra("edit_mode", false)
        val portfolioId = intent.getStringExtra("portfolio_id")
        val portfolioJudul = intent.getStringExtra("portfolio_judul") ?: ""
        val portfolioKategori = intent.getStringExtra("portfolio_kategori") ?: ""
        val portfolioLokasi = intent.getStringExtra("portfolio_lokasi") ?: ""
        val portfolioDeskripsi = intent.getStringExtra("portfolio_deskripsi") ?: ""
        val portfolioGambar = intent.getStringExtra("portfolio_gambar") ?: ""
        val portfolioTechStack = intent.getStringArrayListExtra("portfolio_techstack") ?: arrayListOf()

        setContent {
            MaterialTheme {
                PortfolioAdminScreen(
                    editMode = editMode,
                    portfolioId = portfolioId,
                    initialJudul = portfolioJudul,
                    initialKategori = portfolioKategori,
                    initialLokasi = portfolioLokasi,
                    initialDeskripsi = portfolioDeskripsi,
                    initialGambar = portfolioGambar,
                    initialTechStack = portfolioTechStack,
                    onBack = { finish() },
                    onSave = { judul, kategori, lokasi, deskripsi, imageUri, techStackIds ->
                        savePortfolio(
                            editMode, portfolioId, judul, kategori, lokasi, 
                            deskripsi, imageUri, portfolioGambar, techStackIds
                        )
                    },
                    technologyRepo = technologyRepo
                )
            }
        }
    }

    private fun savePortfolio(
        editMode: Boolean,
        portfolioId: String?,
        judul: String,
        kategori: String,
        lokasi: String,
        deskripsi: String,
        imageUri: Uri?,
        oldImageUri: String,
        techStackIds: List<String>
    ) {
        lifecycleScope.launch {
            try {
                // Cek apakah imageUri adalah URL lama (tidak berubah) atau URI baru dari picker
                val isUrlNotChanged = imageUri?.toString() == oldImageUri
                
                val imageResult = if (editMode && isUrlNotChanged) {
                    // Tidak ada perubahan gambar, pakai URL lama
                    Result.success(oldImageUri)
                } else if (imageUri != null && !isUrlNotChanged) {
                    // Upload gambar baru dan hapus gambar lama (jika ada)
                    if (editMode && oldImageUri.isNotEmpty()) {
                        storageManager.updateImage(oldImageUri, imageUri, R2Config.PORTFOLIO_FOLDER)
                    } else {
                        storageManager.uploadPortfolioImage(imageUri)
                    }
                } else if (imageUri != null) {
                    // Fallback: ada imageUri tapi cek gagal, coba upload
                    storageManager.uploadPortfolioImage(imageUri)
                } else {
                    Result.failure(Exception("No image selected"))
                }

                imageResult.onSuccess { savedImageUri ->
                    val portfolio = Portfolio(
                        id = portfolioId ?: "",
                        judul = judul,
                        kategori = kategori.ifEmpty { "Uncategorized" },
                        lokasi = lokasi,
                        deskripsi = deskripsi,
                        gambarUri = savedImageUri,
                        techStack = techStackIds
                    )

                    val result = if (editMode && portfolioId != null) {
                        portfolioRepo.updatePortfolio(portfolioId, portfolio)
                    } else {
                        portfolioRepo.addPortfolio(portfolio)
                    }

                    result.onSuccess {
                        val message = if (editMode) "Portfolio berhasil diupdate!" else "Portfolio berhasil disimpan!"
                        Toast.makeText(this@PortfolioAdminActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { e ->
                        Toast.makeText(this@PortfolioAdminActivity, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }.onFailure { e ->
                    Toast.makeText(this@PortfolioAdminActivity, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PortfolioAdminActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioAdminScreen(
    editMode: Boolean,
    portfolioId: String?,
    initialJudul: String,
    initialKategori: String,
    initialLokasi: String,
    initialDeskripsi: String,
    initialGambar: String,
    initialTechStack: List<String>,
    onBack: () -> Unit,
    onSave: (String, String, String, String, Uri?, List<String>) -> Unit,
    technologyRepo: FirebaseTechnologyRepository
) {
    var judul by remember { mutableStateOf(initialJudul) }
    var kategori by remember { mutableStateOf(initialKategori) }
    var lokasi by remember { mutableStateOf(initialLokasi) }
    var deskripsi by remember { mutableStateOf(initialDeskripsi) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(if (initialGambar.isNotEmpty()) Uri.parse(initialGambar) else null) }
    var imageChanged by remember { mutableStateOf(false) } // Track if image was changed
    var allTechnologies by remember { mutableStateOf<List<Technology>>(emptyList()) }
    var selectedTechStacks by remember { mutableStateOf<List<Technology>>(emptyList()) }
    var showTechDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            selectedImageUri = it
            imageChanged = true // Mark image as changed when new one selected
        }
    }

    LaunchedEffect(Unit) {
        technologyRepo.getAllTechnologies().onSuccess { technologies ->
            allTechnologies = technologies
            selectedTechStacks = technologies.filter { initialTechStack.contains(it.id) }
        }
    }

    val kategoriList = listOf(
        "Website Development",
        "Mobile Development",
        "Desktop Application",
        "System Integration",
        "E-Commerce",
        "CMS Development"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.logo_kahasolusi),
                            contentDescription = "Kahasolusi Logo",
                            modifier = Modifier.height(40.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
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
            // Upload Image
            item {
                Text(
                    text = "Gambar Project",
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
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
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
                                Text("Pilih Gambar", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Judul
            item {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Project") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Kategori Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        kategoriList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    kategori = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Lokasi
            item {
                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Deskripsi
            item {
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
            }

            // Tech Stack
            item {
                Text(
                    text = "Tech Stack",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showTechDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Tech Stack")
                }

                if (selectedTechStacks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedTechStacks.forEach { tech ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tech.nama)
                                IconButton(onClick = {
                                    selectedTechStacks = selectedTechStacks.filter { it.id != tech.id }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
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
                            // Validasi dengan feedback
                            when {
                                judul.isEmpty() -> {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Judul tidak boleh kosong",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                deskripsi.isEmpty() -> {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Deskripsi tidak boleh kosong",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                selectedImageUri == null -> {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Gambar tidak boleh kosong",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                selectedTechStacks.isEmpty() -> {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Pilih minimal 1 teknologi",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                            }
                            isLoading = true
                            onSave(judul, kategori, lokasi, deskripsi, selectedImageUri, selectedTechStacks.map { it.id })
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

    if (showTechDialog) {
        TechStackDialog(
            allTechnologies = allTechnologies,
            selectedTechnologies = selectedTechStacks,
            onDismiss = { showTechDialog = false },
            onConfirm = { selected ->
                selectedTechStacks = selected
                showTechDialog = false
            }
        )
    }
}

@Composable
fun TechStackDialog(
    allTechnologies: List<Technology>,
    selectedTechnologies: List<Technology>,
    onDismiss: () -> Unit,
    onConfirm: (List<Technology>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedTechnologies) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTech = if (searchQuery.isEmpty()) {
        allTechnologies
    } else {
        allTechnologies.filter { it.nama.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Tech Stack") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(filteredTech) { tech ->
                        val isSelected = tempSelected.any { it.id == tech.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelected = if (isSelected) {
                                        tempSelected.filter { it.id != tech.id }
                                    } else {
                                        tempSelected + tech
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tech.nama)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempSelected) }) {
                Text("Selesai")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
