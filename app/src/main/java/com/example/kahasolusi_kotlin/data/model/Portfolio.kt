package com.example.kahasolusi_kotlin.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Portfolio(
    val id: String = "",
    val judul: String = "",
    val kategori: String = "",
    val lokasi: String = "",
    val deskripsi: String = "",
    val gambarUri: String = "",
    val techStack: Any? = emptyList<String>()
) {
    // Helper function to get techStack as List<String>
    fun getTechStackList(): List<String> {
        return when (techStack) {
            is String -> {
                val str = techStack as String
                if (str.isEmpty()) emptyList()
                else if (str.contains(",")) str.split(",").map { it.trim() }
                else listOf(str)
            }
            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                (techStack as? List<String>) ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
