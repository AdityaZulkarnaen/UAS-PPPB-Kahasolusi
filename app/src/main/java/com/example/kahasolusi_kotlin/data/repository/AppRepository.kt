package com.example.kahasolusi_kotlin.data.repository

import com.example.kahasolusi_kotlin.R
import com.example.kahasolusi_kotlin.data.model.AppItem
import com.example.kahasolusi_kotlin.data.model.TechnologyItem
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
    fun getTechnologyItems(): Flow<List<TechnologyItem>> = flow {
        val techList = listOf(
            TechnologyItem(1, "Kotlin", R.drawable.ic_kotlin),
            TechnologyItem(2, "Java", R.drawable.ic_java),
            TechnologyItem(3, "Firebase", R.drawable.ic_firebase),
            TechnologyItem(4, "MySQL", R.drawable.ic_mysql),
            TechnologyItem(5, "Git", R.drawable.ic_git),
            TechnologyItem(6, "Android", R.drawable.ic_android)
        )
        emit(techList)
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
