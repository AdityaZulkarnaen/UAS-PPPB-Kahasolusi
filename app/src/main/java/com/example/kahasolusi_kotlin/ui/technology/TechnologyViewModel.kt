package com.example.kahasolusi_kotlin.ui.technology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.kahasolusi_kotlin.data.repository.AppRepository

class TechnologyViewModel : ViewModel() {

    private val repository = AppRepository.getInstance()

    val technologyItems = repository.getTechnologyItems().asLiveData()
}
