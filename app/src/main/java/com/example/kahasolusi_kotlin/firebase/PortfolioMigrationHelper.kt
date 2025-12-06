package com.example.kahasolusi_kotlin.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility class untuk migrasi data Portfolio dari format lama ke format baru
 * Format lama: techStack as String
 * Format baru: techStack as List<String>
 */
class PortfolioMigrationHelper {
    
    private val db = FirebaseFirestore.getInstance()
    private val portfolioCollection = db.collection("portfolios")
    
    /**
     * Migrasi semua portfolio dari format lama ke format baru
     * Jalankan function ini sekali saja untuk mengupdate semua data
     */
    suspend fun migrateAllPortfolios(): Result<Int> {
        return try {
            val snapshot = portfolioCollection.get().await()
            var migratedCount = 0
            
            snapshot.documents.forEach { doc ->
                try {
                    val techStackValue = doc.get("techStack")
                    
                    // Check if it's old format (String)
                    if (techStackValue is String && techStackValue.isNotEmpty()) {
                        // Convert string to list
                        val techStackList = if (techStackValue.contains(",")) {
                            techStackValue.split(",").map { it.trim() }
                        } else {
                            listOf(techStackValue)
                        }
                        
                        // Update document
                        doc.reference.update("techStack", techStackList).await()
                        migratedCount++
                        println("Migrated portfolio: ${doc.id}")
                    }
                } catch (e: Exception) {
                    println("Error migrating document ${doc.id}: ${e.message}")
                }
            }
            
            Result.success(migratedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check berapa banyak portfolio yang masih menggunakan format lama
     */
    suspend fun checkOldFormatCount(): Result<Int> {
        return try {
            val snapshot = portfolioCollection.get().await()
            var oldFormatCount = 0
            
            snapshot.documents.forEach { doc ->
                val techStackValue = doc.get("techStack")
                if (techStackValue is String) {
                    oldFormatCount++
                }
            }
            
            Result.success(oldFormatCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
