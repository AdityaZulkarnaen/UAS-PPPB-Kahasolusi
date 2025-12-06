package com.example.kahasolusi_kotlin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = AppRepository.getInstance()

    private val _portfolios = MutableLiveData<List<Portfolio>>()
    val portfolios: LiveData<List<Portfolio>> = _portfolios

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPortfolios()
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

    fun refreshPortfolios() {
        loadPortfolios()
    }
}