package com.example.kahasolusi_kotlin.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kahasolusi_kotlin.R
import com.example.kahasolusi_kotlin.data.model.TechnologyItem

class NotificationsViewModel : ViewModel() {

    private val _technologyList = MutableLiveData<List<TechnologyItem>>()
    val technologyList: LiveData<List<TechnologyItem>> = _technologyList

    init {
        loadTechnologyData()
    }

    private fun loadTechnologyData() {
        val technologies = listOf(
            TechnologyItem(1, "VS Code", R.drawable.vs_code),
            TechnologyItem(2, "VS Code", R.drawable.vs_code),
            TechnologyItem(3, "VS Code", R.drawable.vs_code),
            TechnologyItem(4, "VS Code", R.drawable.vs_code),
            TechnologyItem(5, "VS Code", R.drawable.vs_code),
            TechnologyItem(6, "VS Code", R.drawable.vs_code),
            TechnologyItem(7, "VS Code", R.drawable.vs_code),
            TechnologyItem(8, "VS Code", R.drawable.vs_code),
            TechnologyItem(9, "VS Code", R.drawable.vs_code)
        )
        _technologyList.value = technologies
    }
}