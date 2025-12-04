package com.example.kahasolusi_kotlin.firebase

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * LocalStorageManager - Manages image storage locally without Firebase Storage
 * Images are copied to app's internal storage and URI paths are saved to Firestore
 */
class LocalStorageManager(private val context: Context) {
    
    // Get app's internal images directory
    private fun getImagesDirectory(folderName: String): File {
        val directory = File(context.filesDir, folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }
    
    // Copy image to internal storage and return file URI
    suspend fun saveImageLocally(imageUri: Uri, folderName: String): Result<String> {
        return try {
            // Generate unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val directory = getImagesDirectory(folderName)
            val destinationFile = File(directory, fileName)
            
            // Copy file from URI to internal storage
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Return file URI as string
            Result.success(Uri.fromFile(destinationFile).toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Save portfolio image
    suspend fun savePortfolioImage(imageUri: Uri): Result<String> {
        return saveImageLocally(imageUri, "portfolio_images")
    }
    
    // Save technology icon
    suspend fun saveTechnologyIcon(imageUri: Uri): Result<String> {
        return saveImageLocally(imageUri, "technology_icons")
    }
    
    // Delete image from local storage
    suspend fun deleteImage(imageUriString: String): Result<Unit> {
        return try {
            val uri = Uri.parse(imageUriString)
            val file = File(uri.path ?: "")
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update image (delete old and save new)
    suspend fun updateImage(oldImageUri: String?, newImageUri: Uri, folderName: String): Result<String> {
        return try {
            // Delete old image if exists
            if (!oldImageUri.isNullOrEmpty()) {
                try {
                    deleteImage(oldImageUri)
                } catch (e: Exception) {
                    // Ignore if delete fails
                }
            }
            
            // Save new image
            saveImageLocally(newImageUri, folderName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check if image file exists
    fun imageExists(imageUriString: String): Boolean {
        return try {
            val uri = Uri.parse(imageUriString)
            val file = File(uri.path ?: "")
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    // Get file from URI string
    fun getImageFile(imageUriString: String): File? {
        return try {
            val uri = Uri.parse(imageUriString)
            File(uri.path ?: "")
        } catch (e: Exception) {
            null
        }
    }
}
