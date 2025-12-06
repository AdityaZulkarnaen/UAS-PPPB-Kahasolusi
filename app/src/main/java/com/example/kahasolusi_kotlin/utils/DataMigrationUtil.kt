package com.example.kahasolusi_kotlin.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.kahasolusi_kotlin.firebase.PortfolioMigrationHelper
import kotlinx.coroutines.launch

/**
 * Utility untuk menjalankan migrasi data portfolio
 * Gunakan ini untuk sekali jalan saja saat update aplikasi
 */
object DataMigrationUtil {
    
    /**
     * Tampilkan dialog untuk migrasi data portfolio
     * Panggil ini dari Activity yang perlu melakukan migrasi
     */
    fun showMigrationDialog(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        onComplete: () -> Unit = {}
    ) {
        AlertDialog.Builder(context)
            .setTitle("Migrasi Data")
            .setMessage("Ditemukan format data lama. Apakah Anda ingin melakukan migrasi data ke format baru?")
            .setPositiveButton("Ya, Migrasi") { _, _ ->
                performMigration(context, lifecycleScope, onComplete)
            }
            .setNegativeButton("Nanti") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Jalankan migrasi data
     */
    private fun performMigration(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        onComplete: () -> Unit
    ) {
        val migrationHelper = PortfolioMigrationHelper()
        
        lifecycleScope.launch {
            Toast.makeText(context, "Migrasi data dimulai...", Toast.LENGTH_SHORT).show()
            
            val result = migrationHelper.migrateAllPortfolios()
            
            result.onSuccess { count ->
                Toast.makeText(
                    context,
                    "Migrasi selesai! $count portfolio berhasil diupdate.",
                    Toast.LENGTH_LONG
                ).show()
                onComplete()
            }.onFailure { e ->
                Toast.makeText(
                    context,
                    "Migrasi gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Check apakah ada data yang perlu dimigrasi
     */
    fun checkNeedMigration(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        onNeedMigration: (Int) -> Unit
    ) {
        val migrationHelper = PortfolioMigrationHelper()
        
        lifecycleScope.launch {
            val result = migrationHelper.checkOldFormatCount()
            
            result.onSuccess { count ->
                if (count > 0) {
                    onNeedMigration(count)
                }
            }.onFailure { e ->
                Toast.makeText(
                    context,
                    "Error checking migration: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
