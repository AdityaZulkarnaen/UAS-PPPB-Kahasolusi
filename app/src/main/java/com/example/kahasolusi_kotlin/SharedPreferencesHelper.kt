package com.example.kahasolusi_kotlin

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "kahasolusi_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_ME = "remember_me"
    }
    
    // Fungsi untuk login (menggunakan Firebase Auth, SharedPreferences hanya menyimpan session)
    fun saveLoginSession(email: String, fullName: String, rememberMe: Boolean): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_FULL_NAME, fullName)
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe)
        editor.apply()
        return true
    }
    
    // Fungsi untuk logout
    fun logoutUser() {
        val editor = sharedPreferences.edit()
        
        // Hapus session data tapi pertahankan remember me jika diperlukan
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        val rememberedUsername = sharedPreferences.getString(KEY_USERNAME, "")
        val rememberedPassword = sharedPreferences.getString(KEY_PASSWORD, "")
        
        editor.clear()
        
        // Restore remember me data jika ada
        if (rememberMe && !rememberedUsername.isNullOrEmpty() && !rememberedPassword.isNullOrEmpty()) {
            editor.putString(KEY_USERNAME, rememberedUsername)
            editor.putString(KEY_PASSWORD, rememberedPassword)
            editor.putBoolean(KEY_REMEMBER_ME, true)
        }
        
        editor.apply()
    }
    
    // Fungsi untuk cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // Fungsi untuk mendapatkan data user yang sedang login
    fun getLoggedInUser(): UserData? {
        if (!isLoggedIn()) return null
        
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
        val fullName = sharedPreferences.getString(KEY_FULL_NAME, "") ?: ""
        
        return UserData(fullName, email, username)
    }
    
    // Fungsi untuk mendapatkan remembered credentials
    fun getRememberedCredentials(): Pair<String, String>? {
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        if (rememberMe) {
            val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
            val password = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
            
            if (username.isNotEmpty() && password.isNotEmpty()) {
                return Pair(username, password)
            }
        }
        return null
    }
    
    // Data class untuk user
    data class UserData(
        val fullName: String,
        val email: String,
        val username: String
    )
}