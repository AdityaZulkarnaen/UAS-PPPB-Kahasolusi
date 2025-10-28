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
import com.example.kahasolusi_kotlin.ui.dashboard.adapter.AppItemAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var appItemAdapter: AppItemAdapter

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
        appItemAdapter = AppItemAdapter { appItem ->
            // Handle item click
            Toast.makeText(
                requireContext(),
                "Clicked: ${appItem.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvAppItems.apply {
            adapter = appItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        dashboardViewModel.appItems.observe(viewLifecycleOwner) { items ->
            appItemAdapter.submitList(items)
        }
        
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a loading indicator here
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}