package com.example.kahasolusi_kotlin.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kahasolusi_kotlin.databinding.FragmentNotificationsBinding
import com.example.kahasolusi_kotlin.ui.notifications.adapter.TechnologyAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var technologyAdapter: TechnologyAdapter
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeViewModel()

        return root
    }

    private fun setupRecyclerView() {
        technologyAdapter = TechnologyAdapter(emptyList()) { technologyItem ->
            // Handle item click
            Toast.makeText(context, "Clicked: ${technologyItem.name}", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerViewTechnology.apply {
            adapter = technologyAdapter
            layoutManager = GridLayoutManager(context, 3) // 3 columns like in the image
        }
    }

    private fun observeViewModel() {
        notificationsViewModel.technologyList.observe(viewLifecycleOwner) { technologies ->
            technologyAdapter = TechnologyAdapter(technologies) { technologyItem ->
                Toast.makeText(context, "Clicked: ${technologyItem.name}", Toast.LENGTH_SHORT).show()
            }
            binding.recyclerViewTechnology.adapter = technologyAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}