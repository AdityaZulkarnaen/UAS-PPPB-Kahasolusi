package com.example.kahasolusi_kotlin.ui.technology

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kahasolusi_kotlin.adapter.TechnologyAdapter
import com.example.kahasolusi_kotlin.databinding.FragmentTechnologyBinding

class TechnologyFragment : Fragment() {

    private var _binding: FragmentTechnologyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TechnologyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTechnologyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTechnology.layoutManager = GridLayoutManager(requireContext(), 3)

        viewModel.technologyItems.observe(viewLifecycleOwner) { list ->
            binding.rvTechnology.adapter = TechnologyAdapter(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
