package com.example.kahasolusi_kotlin.data.repository

import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppRepository {

    private val portfolioRepo = FirebasePortfolioRepository()

    // Fetch portfolios from Firebase
    fun getPortfolios(): Flow<List<Portfolio>> = flow {
        val result = portfolioRepo.getAllPortfolios()
        result.onSuccess { portfolios ->
            emit(portfolios)
        }.onFailure {
            // Emit empty list on failure
            emit(emptyList())
        }
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
