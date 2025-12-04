package com.example.kahasolusi_kotlin.firebase

import com.example.kahasolusi_kotlin.data.model.Technology
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseTechnologyRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private val technologyCollection = db.collection("technologies")
    
    // Add new technology
    suspend fun addTechnology(technology: Technology): Result<String> {
        return try {
            val docRef = technologyCollection.add(technology).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update existing technology
    suspend fun updateTechnology(technologyId: String, technology: Technology): Result<Unit> {
        return try {
            technologyCollection.document(technologyId).set(technology).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete technology
    suspend fun deleteTechnology(technologyId: String): Result<Unit> {
        return try {
            technologyCollection.document(technologyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all technologies
    suspend fun getAllTechnologies(): Result<List<Technology>> {
        return try {
            val snapshot = technologyCollection
                .orderBy("nama", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val technologies = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Technology::class.java)?.copy(id = doc.id)
            }
            Result.success(technologies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get technology by ID
    suspend fun getTechnologyById(technologyId: String): Result<Technology?> {
        return try {
            val doc = technologyCollection.document(technologyId).get().await()
            val technology = doc.toObject(Technology::class.java)?.copy(id = doc.id)
            Result.success(technology)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Search technologies by keyword
    suspend fun searchTechnologies(keyword: String): Result<List<Technology>> {
        return try {
            val snapshot = technologyCollection.get().await()
            
            val technologies = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Technology::class.java)?.copy(id = doc.id)
            }.filter { technology ->
                technology.nama.contains(keyword, ignoreCase = true)
            }
            Result.success(technologies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
