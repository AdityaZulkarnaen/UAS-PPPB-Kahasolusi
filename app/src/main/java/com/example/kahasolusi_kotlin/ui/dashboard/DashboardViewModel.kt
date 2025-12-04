package com.example.kahasolusi_kotlin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kahasolusi_kotlin.data.model.AppItem
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: AppRepository = AppRepository.getInstance()
) : ViewModel() {

    private val _appItems = MutableLiveData<List<AppItem>>()
    val appItems: LiveData<List<AppItem>> = _appItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAppItems()
    }

    private fun loadAppItems() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAppItems().collect { items ->
                _appItems.value = items
                _isLoading.value = false
            }
        }
    }

    fun refreshItems() {
        loadAppItems()
    }
}