package com.example.kahasolusi_kotlin.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kahasolusi_kotlin.data.model.Technology
import com.example.kahasolusi_kotlin.data.repository.AppRepository
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val repository = AppRepository.getInstance()

    private val _technologyList = MutableLiveData<List<Technology>>()
    val technologyList: LiveData<List<Technology>> = _technologyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadTechnologies()
    }

    private fun loadTechnologies() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTechnologies().collect { technologies ->
                _technologyList.value = technologies
                _isLoading.value = false
            }
        }
    }

    fun refreshTechnologies() {
        loadTechnologies()
    }
}