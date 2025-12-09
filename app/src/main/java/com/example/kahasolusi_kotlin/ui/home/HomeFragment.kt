package com.example.kahasolusi_kotlin.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.kahasolusi_kotlin.PortfolioDetailActivity
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onPortfolioClick = { portfolio ->
                            val intent = Intent(requireContext(), PortfolioDetailActivity::class.java).apply {
                                putExtra("portfolio_id", portfolio.id)
                                putExtra("portfolio_judul", portfolio.judul)
                                putExtra("portfolio_kategori", portfolio.kategori)
                                putExtra("portfolio_lokasi", portfolio.lokasi)
                                putExtra("portfolio_deskripsi", portfolio.deskripsi)
                                putExtra("portfolio_gambar", portfolio.gambarUri)
                                putStringArrayListExtra("portfolio_techstack", ArrayList(portfolio.getTechStackList()))
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onPortfolioClick: (Portfolio) -> Unit
) {
    val portfolios by homeViewModel.portfolios.observeAsState(emptyList())
    val technologies by homeViewModel.technologies.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCCCCCC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hero Image",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }

        // About Kahasolusi Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "About Kahasolusi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Kahasolusi menyediakan Software House yang dapat yang berbeda pada kebutuhan bisnis Anda. Kami memiliki tim profesional yang berpengalaman dalam mengembangkan solusi teknologi inovatif. Kahasolusi mengintegrasikan perangkat dan layanan teknologi terkini untuk membantu bisnis Anda tumbuh dengan efisien dan efektif dengan efektif.",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }

        // Proyek Kami Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Proyek Kami",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(portfolios.size) { index ->
                    PortfolioCard(portfolio = portfolios[index], onClick = { onPortfolioClick(portfolios[index]) })
                }
            }
        }

        // Teknologi Kami Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Teknologi Kami",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(technologies.size) { index ->
                    TechnologyCard(technology = technologies[index])
                }
            }
        }

        // Lokasi Kami Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 64.dp)
        ) {
            Text(
                text = "Lokasi Kami",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE8F4F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Map View",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioCard(portfolio: Portfolio, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Portfolio Image
            if (portfolio.gambarUri.isNotEmpty()) {
                AsyncImage(
                    model = portfolio.gambarUri,
                    contentDescription = portfolio.judul,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }
            
            // Portfolio Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = portfolio.judul,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = portfolio.kategori,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 1
                )
                Text(
                    text = portfolio.lokasi,
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TechnologyCard(technology: Technology) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (technology.iconUri.isNotEmpty()) {
                AsyncImage(
                    model = technology.iconUri,
                    contentDescription = technology.nama,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tech", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = technology.nama,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                maxLines = 1
            )
        }
    }
}