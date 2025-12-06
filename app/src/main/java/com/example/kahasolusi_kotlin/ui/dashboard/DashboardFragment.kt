package com.example.kahasolusi_kotlin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import com.example.kahasolusi_kotlin.databinding.FragmentDashboardBinding
import com.example.kahasolusi_kotlin.ui.dashboard.adapter.PortfolioItemAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var portfolioItemAdapter: PortfolioItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel with Factory
        val repository = AppRepository.getInstance()
        val factory = DashboardViewModelFactory(repository)
        dashboardViewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        portfolioItemAdapter = PortfolioItemAdapter { portfolio ->
            // Handle item click
            Toast.makeText(
                requireContext(),
                "Clicked: ${portfolio.judul}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvAppItems.apply {
            adapter = portfolioItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        dashboardViewModel.portfolios.observe(viewLifecycleOwner) { portfolios ->
            portfolioItemAdapter.submitList(portfolios)
        }
        
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a loading indicator here
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}