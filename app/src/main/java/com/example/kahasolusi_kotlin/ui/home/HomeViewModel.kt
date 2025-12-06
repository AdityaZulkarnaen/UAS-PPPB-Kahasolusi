package com.example.kahasolusi_kotlin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = AppRepository.getInstance()

    private val _portfolios = MutableLiveData<List<Portfolio>>()
    val portfolios: LiveData<List<Portfolio>> = _portfolios

    private val _technologies = MutableLiveData<List<Technology>>()
    val technologies: LiveData<List<Technology>> = _technologies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPortfolios()
        loadTechnologies()
    }

    private fun loadPortfolios() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPortfolios().collect { portfolios ->
                _portfolios.value = portfolios
                _isLoading.value = false
            }
        }
    }

    private fun loadTechnologies() {
        viewModelScope.launch {
            repository.getTechnologies().collect { technologies ->
                _technologies.value = technologies
            }
        }
    }

    fun refreshPortfolios() {
        loadPortfolios()
    }

    fun refreshTechnologies() {
        loadTechnologies()
    }
}