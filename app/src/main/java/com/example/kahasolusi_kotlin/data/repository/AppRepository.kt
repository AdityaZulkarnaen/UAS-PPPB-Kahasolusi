package com.example.kahasolusi_kotlin.data.repository

import com.example.kahasolusi_kotlin.data.model.AppItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppRepository {

    // Simulate fetching data from a remote source or database
    fun getAppItems(): Flow<List<AppItem>> = flow {
        // In a real app, this would be an API call or database query
        val items = listOf(
            AppItem(
                id = 1,
                title = "Aplikasi Jogja Center",
                subtitle = "Daerah Istimewa Yogyakarta",
                imageUrl = "",
                category = "Pemerintahan"
            ),
            AppItem(
                id = 2,
                title = "Aplikasi Jogja Center",
                subtitle = "Daerah Istimewa Yogyakarta",
                imageUrl = "",
                category = "Pemerintahan"
            ),
            AppItem(
                id = 3,
                title = "Aplikasi Jogja Center",
                subtitle = "Daerah Istimewa Yogyakarta",
                imageUrl = "",
                category = "Pemerintahan"
            )
        )
        emit(items)
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository().also { instance = it }
            }
        }
    }
}
