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
        
        // Prefix untuk menyimpan data user yang register
        private const val PREFIX_USER = "user_"
        private const val SUFFIX_FULL_NAME = "_full_name"
        private const val SUFFIX_EMAIL = "_email"
        private const val SUFFIX_PASSWORD = "_password"
    }
    
    // Fungsi untuk register user baru
    fun registerUser(fullName: String, email: String, username: String, password: String): Boolean {
        // Cek apakah username sudah ada
        if (isUserExists(username)) {
            return false
        }
        
        val editor = sharedPreferences.edit()
        editor.putString("$PREFIX_USER$username$SUFFIX_FULL_NAME", fullName)
        editor.putString("$PREFIX_USER$username$SUFFIX_EMAIL", email)
        editor.putString("$PREFIX_USER$username$SUFFIX_PASSWORD", password)
        editor.apply()
        
        return true
    }
    
    // Fungsi untuk cek apakah user sudah ada
    fun isUserExists(username: String): Boolean {
        return sharedPreferences.contains("$PREFIX_USER$username$SUFFIX_FULL_NAME")
    }
    
    // Fungsi untuk login
    fun loginUser(username: String, password: String, rememberMe: Boolean): Boolean {
        val storedPassword = sharedPreferences.getString("$PREFIX_USER$username$SUFFIX_PASSWORD", null)
        
        if (storedPassword != null && storedPassword == password) {
            // Login berhasil, simpan session
            val editor = sharedPreferences.edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putString(KEY_USERNAME, username)
            editor.putString(KEY_EMAIL, sharedPreferences.getString("$PREFIX_USER$username$SUFFIX_EMAIL", ""))
            editor.putString(KEY_FULL_NAME, sharedPreferences.getString("$PREFIX_USER$username$SUFFIX_FULL_NAME", ""))
            editor.putBoolean(KEY_REMEMBER_ME, rememberMe)
            
            if (rememberMe) {
                editor.putString(KEY_PASSWORD, password)
            } else {
                editor.remove(KEY_PASSWORD)
            }
            
            editor.apply()
            return true
        }
        
        return false
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