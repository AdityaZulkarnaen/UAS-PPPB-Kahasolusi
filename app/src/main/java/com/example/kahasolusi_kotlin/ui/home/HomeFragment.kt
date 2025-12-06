package com.example.kahasolusi_kotlin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.databinding.FragmentHomeBinding
import com.example.kahasolusi_kotlin.ui.home.adapter.PortfolioHorizontalAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var portfolioAdapter: PortfolioHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeViewModel()

        return root
    }

    private fun setupRecyclerView() {
        portfolioAdapter = PortfolioHorizontalAdapter { portfolio ->
            // Handle item click
            Toast.makeText(
                requireContext(),
                "Portfolio: ${portfolio.judul}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvPortfolioHome.apply {
            adapter = portfolioAdapter
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
        
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}