package com.example.kahasolusi_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kahasolusi_kotlin.adapter.TechnologyAdapter
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import com.example.kahasolusi_kotlin.databinding.ActivityTechnologyBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TechnologyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTechnologyBinding
    private val repository = AppRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTechnologyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Layout Manager recyclerview
        binding.rvTechnology.layoutManager = GridLayoutManager(this, 3)

        lifecycleScope.launch {
            repository.getTechnologyItems().collectLatest { list ->
                binding.rvTechnology.adapter = TechnologyAdapter(list)
            }
        }
    }
}
