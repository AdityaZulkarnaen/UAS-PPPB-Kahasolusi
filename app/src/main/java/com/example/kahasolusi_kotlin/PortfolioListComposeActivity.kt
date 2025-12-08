package com.example.kahasolusi_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.FirebaseTechnologyRepository
import kotlinx.coroutines.launch

class PortfolioListComposeActivity : ComponentActivity() {

    private val portfolioRepo = FirebasePortfolioRepository()
    private val technologyRepo = FirebaseTechnologyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var portfolioList by remember { mutableStateOf<List<Portfolio>>(emptyList()) }
                var technologyList by remember { mutableStateOf<List<Technology>>(emptyList()) }
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(selectedTab) {
                    isLoading = true
                    if (selectedTab == 0) {
                        portfolioRepo.getAllPortfolios().onSuccess {
                            portfolioList = it
                        }
                    } else {
                        technologyRepo.getAllTechnologies().onSuccess {
                            technologyList = it
                        }
                    }
                    isLoading = false
                }

                CMSScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    portfolioList = portfolioList,
                    technologyList = technologyList,
                    isLoading = isLoading,
                    onBackClick = { finish() },
                    onAddClick = {
                        if (selectedTab == 0) {
                            startActivity(Intent(this, PortfolioAdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, TechnologyAdminActivity::class.java))
                        }
                    },
                    onEditPortfolio = { portfolio ->
                        val intent = Intent(this, PortfolioAdminActivity::class.java).apply {
                            putExtra("edit_mode", true)
                            putExtra("portfolio_id", portfolio.id)
                            putExtra("portfolio_judul", portfolio.judul)
                            putExtra("portfolio_kategori", portfolio.kategori)
                            putExtra("portfolio_lokasi", portfolio.lokasi)
                            putExtra("portfolio_deskripsi", portfolio.deskripsi)
                            putExtra("portfolio_gambar", portfolio.gambarUri)
                            putStringArrayListExtra("portfolio_techstack", ArrayList(portfolio.getTechStackList()))
                        }
                        startActivity(intent)
                    },
                    onDeletePortfolio = { portfolio ->
                        lifecycleScope.launch {
                            portfolioRepo.deletePortfolio(portfolio.id).onSuccess {
                                Toast.makeText(this@PortfolioListComposeActivity, "Portfolio berhasil dihapus", Toast.LENGTH_SHORT).show()
                                // Reload data
                                portfolioRepo.getAllPortfolios().onSuccess {
                                    portfolioList = it
                                }
                            }
                        }
                    },
                    onEditTechnology = { technology ->
                        val intent = Intent(this, TechnologyAdminActivity::class.java).apply {
                            putExtra("edit_mode", true)
                            putExtra("technology_id", technology.id)
                            putExtra("technology_nama", technology.nama)
                            putExtra("technology_icon", technology.iconUri)
                        }
                        startActivity(intent)
                    },
                    onDeleteTechnology = { technology ->
                        lifecycleScope.launch {
                            technologyRepo.deleteTechnology(technology.id).onSuccess {
                                Toast.makeText(this@PortfolioListComposeActivity, "Teknologi berhasil dihapus", Toast.LENGTH_SHORT).show()
                                // Reload data
                                technologyRepo.getAllTechnologies().onSuccess {
                                    technologyList = it
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMSScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    portfolioList: List<Portfolio>,
    technologyList: List<Technology>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditPortfolio: (Portfolio) -> Unit,
    onDeletePortfolio: (Portfolio) -> Unit,
    onEditTechnology: (Technology) -> Unit,
    onDeleteTechnology: (Technology) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "KAHASOLUSI",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0E2144),
                                    Color(0xFF1E2642),
                                    Color(0xFFF16724)
                                )
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Profile click */ }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFF4FC3F7),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedTab == 0) "Tambah Portfolio" else "Tambah Teknologi")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    text = { Text("Portofolio", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Business, "Portfolio") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    text = { Text("Technology", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Code, "Technology") }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (selectedTab == 0) {
                    PortfolioListContent(
                        portfolioList = portfolioList,
                        onEdit = onEditPortfolio,
                        onDelete = onDeletePortfolio
                    )
                } else {
                    TechnologyListContent(
                        technologyList = technologyList,
                        onEdit = onEditTechnology,
                        onDelete = onDeleteTechnology
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioListContent(
    portfolioList: List<Portfolio>,
    onEdit: (Portfolio) -> Unit,
    onDelete: (Portfolio) -> Unit
) {
    if (portfolioList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada portfolio", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(portfolioList) { portfolio ->
                PortfolioItemCard(
                    portfolio = portfolio,
                    onEdit = { onEdit(portfolio) },
                    onDelete = { onDelete(portfolio) }
                )
            }
            // Extra space for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun PortfolioItemCard(
    portfolio: Portfolio,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Image
            if (portfolio.gambarUri.isNotEmpty()) {
                AsyncImage(
                    model = portfolio.gambarUri,
                    contentDescription = portfolio.judul,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, "No image", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = portfolio.judul,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = portfolio.deskripsi,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TechnologyListContent(
    technologyList: List<Technology>,
    onEdit: (Technology) -> Unit,
    onDelete: (Technology) -> Unit
) {
    if (technologyList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada teknologi", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(technologyList) { technology ->
                TechnologyItemCard(
                    technology = technology,
                    onEdit = { onEdit(technology) },
                    onDelete = { onDelete(technology) }
                )
            }
            // Extra space for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun TechnologyItemCard(
    technology: Technology,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (technology.iconUri.isNotEmpty()) {
                AsyncImage(
                    model = technology.iconUri,
                    contentDescription = technology.nama,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Code, "No icon", tint = Color.Gray, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name
            Text(
                text = technology.nama,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
