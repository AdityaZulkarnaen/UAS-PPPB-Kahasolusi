package com.example.kahasolusi_kotlin.firebase

import com.example.kahasolusi_kotlin.data.model.Portfolio
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebasePortfolioRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private val portfolioCollection = db.collection("portfolios")
    
    // Add new portfolio
    suspend fun addPortfolio(portfolio: Portfolio): Result<String> {
        return try {
            val docRef = portfolioCollection.add(portfolio).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update existing portfolio
    suspend fun updatePortfolio(portfolioId: String, portfolio: Portfolio): Result<Unit> {
        return try {
            portfolioCollection.document(portfolioId).set(portfolio).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete portfolio
    suspend fun deletePortfolio(portfolioId: String): Result<Unit> {
        return try {
            portfolioCollection.document(portfolioId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all portfolios
    suspend fun getAllPortfolios(): Result<List<Portfolio>> {
        return try {
            val snapshot = portfolioCollection
                .orderBy("judul", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val portfolios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Portfolio::class.java)?.copy(id = doc.id)
            }
            Result.success(portfolios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get portfolio by ID
    suspend fun getPortfolioById(portfolioId: String): Result<Portfolio?> {
        return try {
            val doc = portfolioCollection.document(portfolioId).get().await()
            val portfolio = doc.toObject(Portfolio::class.java)?.copy(id = doc.id)
            Result.success(portfolio)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get portfolios by category
    suspend fun getPortfoliosByCategory(kategori: String): Result<List<Portfolio>> {
        return try {
            val snapshot = portfolioCollection
                .whereEqualTo("kategori", kategori)
                .orderBy("judul", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val portfolios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Portfolio::class.java)?.copy(id = doc.id)
            }
            Result.success(portfolios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Search portfolios by keyword
    suspend fun searchPortfolios(keyword: String): Result<List<Portfolio>> {
        return try {
            val snapshot = portfolioCollection.get().await()
            
            val portfolios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Portfolio::class.java)?.copy(id = doc.id)
            }.filter { portfolio ->
                portfolio.judul.contains(keyword, ignoreCase = true) ||
                portfolio.deskripsi.contains(keyword, ignoreCase = true) ||
                portfolio.kategori.contains(keyword, ignoreCase = true)
            }
            Result.success(portfolios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
