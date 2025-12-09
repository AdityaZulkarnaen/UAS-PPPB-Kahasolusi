package com.example.kahasolusi_kotlin

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class PortfolioDetailActivity : ComponentActivity() {

    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val portfolioId = intent.getStringExtra("portfolio_id") ?: ""
        val portfolioJudul = intent.getStringExtra("portfolio_judul") ?: ""
        val portfolioKategori = intent.getStringExtra("portfolio_kategori") ?: ""
        val portfolioLokasi = intent.getStringExtra("portfolio_lokasi") ?: ""
        val portfolioDeskripsi = intent.getStringExtra("portfolio_deskripsi") ?: ""
        val portfolioGambar = intent.getStringExtra("portfolio_gambar") ?: ""
        val portfolioTechStack = intent.getStringArrayListExtra("portfolio_techstack") ?: arrayListOf()

        setContent {
            MaterialTheme {
                PortfolioDetailScreen(
                    portfolioJudul = portfolioJudul,
                    portfolioKategori = portfolioKategori,
                    portfolioLokasi = portfolioLokasi,
                    portfolioDeskripsi = portfolioDeskripsi,
                    portfolioGambar = portfolioGambar,
                    portfolioTechStack = portfolioTechStack,
                    technologyRepo = technologyRepo,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioDetailScreen(
    portfolioJudul: String,
    portfolioKategori: String,
    portfolioLokasi: String,
    portfolioDeskripsi: String,
    portfolioGambar: String,
    portfolioTechStack: List<String>,
    technologyRepo: FirebaseTechnologyRepository,
    onBack: () -> Unit
) {
    var techStackDetails by remember { mutableStateOf<List<Technology>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(portfolioTechStack) {
        if (portfolioTechStack.isEmpty()) {
            isLoading = false
            techStackDetails = emptyList()
            Log.d("PortfolioDetail", "No tech stack IDs provided")
            return@LaunchedEffect
        }
        
        isLoading = true
        Log.d("PortfolioDetail", "Loading ${portfolioTechStack.size} tech stack items: $portfolioTechStack")
        
        try {
            // First, get all technologies to match by name if needed
            val allTechsResult = technologyRepo.getAllTechnologies()
            val allTechs = allTechsResult.getOrNull() ?: emptyList()
            
            // Load all technologies in parallel using async/await
            val deferredTechs = portfolioTechStack.map { techIdentifier ->
                async {
                    try {
                        Log.d("PortfolioDetail", "Fetching tech identifier: $techIdentifier")
                        
                        // Try to get by ID first
                        val result = technologyRepo.getTechnologyById(techIdentifier)
                        val tech = result.getOrNull()
                        
                        if (tech != null) {
                            Log.d("PortfolioDetail", "Successfully loaded by ID: ${tech.nama}")
                            tech
                        } else {
                            // If not found by ID, try to match by name (for legacy data)
                            val techByName = allTechs.find { 
                                it.nama.equals(techIdentifier, ignoreCase = true) 
                            }
                            if (techByName != null) {
                                Log.d("PortfolioDetail", "Found by name: ${techByName.nama}")
                                techByName
                            } else {
                                Log.w("PortfolioDetail", "Tech not found for identifier: $techIdentifier")
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PortfolioDetail", "Error loading tech $techIdentifier: ${e.message}", e)
                        null
                    }
                }
            }
            
            // Wait for all results and filter out nulls
            val loadedTechs = deferredTechs.awaitAll().filterNotNull()
            techStackDetails = loadedTechs
            Log.d("PortfolioDetail", "Total techs loaded: ${loadedTechs.size}")
        } catch (e: Exception) {
            Log.e("PortfolioDetail", "Error loading tech stack: ${e.message}", e)
            techStackDetails = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Portfolio") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Portfolio Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (portfolioGambar.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(portfolioGambar)),
                        contentDescription = portfolioJudul,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", color = Color.Gray)
                    }
                }
            }

            // Content with white background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Location icon and text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = portfolioLokasi.ifEmpty { "Location not specified" },
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Title
                Text(
                    text = portfolioJudul,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Description
                Text(
                    text = portfolioDeskripsi,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Tech Stack Section
                Text(
                    text = "Tech Stack:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (techStackDetails.isEmpty()) {
                    Text(
                        text = "No tech stack available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else {
                    // Tech stack grid - 3 columns
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        techStackDetails.chunked(3).forEach { rowTechs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowTechs.forEach { tech ->
                                    TechBadgeItem(
                                        technology = tech,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill empty spaces if row is not complete
                                repeat(3 - rowTechs.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechBadgeItem(
    technology: Technology,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with light blue background
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (technology.iconUri.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(technology.iconUri)),
                    contentDescription = technology.nama,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = technology.nama,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF2196F3)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tech name
        Text(
            text = technology.nama,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
