package com.example.kahasolusi_kotlin.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.PortfolioDetailActivity
import com.example.kahasolusi_kotlin.databinding.FragmentHomeBinding
import com.example.kahasolusi_kotlin.ui.home.adapter.PortfolioHorizontalAdapter
import com.example.kahasolusi_kotlin.ui.home.adapter.TechnologyHorizontalAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var portfolioAdapter: PortfolioHorizontalAdapter
    private lateinit var technologyAdapter: TechnologyHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerViews()
        observeViewModel()

        return root
    }

    private fun setupRecyclerViews() {
        // Portfolio RecyclerView
        portfolioAdapter = PortfolioHorizontalAdapter { portfolio ->
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
        
        binding.rvPortfolioHome.apply {
            adapter = portfolioAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // Technology RecyclerView
        technologyAdapter = TechnologyHorizontalAdapter { technology ->
            Toast.makeText(
                requireContext(),
                "Technology: ${technology.nama}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvTechnologyHome.apply {
            adapter = technologyAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun observeViewModel() {
        homeViewModel.portfolios.observe(viewLifecycleOwner) { portfolios ->
            portfolioAdapter.submitList(portfolios)
        }
        
        homeViewModel.technologies.observe(viewLifecycleOwner) { technologies ->
            technologyAdapter.submitList(technologies)
        }
        
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}