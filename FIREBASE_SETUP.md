# Firebase Integration Guide

## Setup Firebase Backend untuk Kahasolusi App

Backend sistem sudah dibuat menggunakan Firebase dengan komponen:
- **Firebase Authentication** - User login/register (GRATIS - Spark Plan)
- **Cloud Firestore** - Database untuk portfolio & technology (GRATIS - Spark Plan)
- **Local Storage** - Simpan gambar di internal storage device (GRATIS - tanpa Firebase Storage)

**CATATAN:** Sistem ini TIDAK menggunakan Firebase Storage untuk menghindari kebutuhan upgrade plan. Gambar disimpan secara lokal di device dan URI path-nya disimpan di Firestore.

## 1. Setup Firebase Project

### Langkah 1: Buat Firebase Project
1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Klik **Add Project** atau **Create a project**
3. Masukkan nama project: `Kahasolusi` atau nama lain
4. Disable Google Analytics (opsional)
5. Klik **Create Project**

### Langkah 2: Tambahkan Android App
1. Di Firebase Console, klik ikon Android
2. Masukkan package name: `com.example.kahasolusi_kotlin`
3. App nickname (opsional): `Kahasolusi Android`
4. Debug signing certificate SHA-1 (opsional untuk development)
5. Klik **Register app**

### Langkah 3: Download google-services.json
1. Download file `google-services.json`
2. Pindahkan file ke: `app/google-services.json`
3. **PENTING**: File ini sudah di-gitignore, jangan commit ke repository

### Langkah 4: Enable Firebase Services

#### A. Authentication
1. Di Firebase Console, buka **Authentication**
2. Klik **Get Started**
3. Tab **Sign-in method**
4. Enable **Email/Password**
5. Save

#### B. Cloud Firestore
1. Di Firebase Console, buka **Firestore Database**
2. Klik **Create database**
3. Pilih **Start in test mode** (untuk development)
4. Pilih region (asia-southeast2 untuk Jakarta)
5. Klik **Enable**

**Security Rules untuk Development:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**Security Rules untuk Production:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Portfolios - authenticated users can CRUD
    match /portfolios/{portfolioId} {
      allow read: if true; // Public read
      allow create, update, delete: if request.auth != null;
    }
    
    // Technologies - authenticated users can CRUD
    match /technologies/{technologyId} {
      allow read: if true; // Public read
      allow create, update, delete: if request.auth != null;
    }
  }
}
```

**CATATAN:** Firebase Storage TIDAK digunakan dalam project ini. Gambar disimpan di internal storage device.

## 2. Struktur Firebase Classes

### FirebaseAuthManager
**Path:** `firebase/FirebaseAuthManager.kt`

**Methods:**
- `registerUser(email, password, fullName)` - Daftar user baru
- `loginUser(email, password)` - Login user
- `logoutUser()` - Logout
- `getCurrentUser()` - Get current user
- `isUserLoggedIn()` - Check login status
- `getUserEmail()` - Get user email
- `getUserDisplayName()` - Get display name
- `getUserId()` - Get user ID

### FirebasePortfolioRepository
**Path:** `firebase/FirebasePortfolioRepository.kt`

**Collection:** `portfolios`

**Methods:**
- `addPortfolio(portfolio)` - Tambah portfolio baru
- `updatePortfolio(id, portfolio)` - Update portfolio
- `deletePortfolio(id)` - Hapus portfolio
- `getAllPortfolios()` - Get semua portfolio
- `getPortfolioById(id)` - Get portfolio by ID
- `getPortfoliosByCategory(kategori)` - Filter by kategori
- `searchPortfolios(keyword)` - Search portfolio

### FirebaseTechnologyRepository
**Path:** `firebase/FirebaseTechnologyRepository.kt`

**Collection:** `technologies`

**Methods:**
- `addTechnology(technology)` - Tambah technology
- `updateTechnology(id, technology)` - Update technology
- `deleteTechnology(id)` - Hapus technology
- `getAllTechnologies()` - Get semua technology
- `getTechnologyById(id)` - Get technology by ID
- `searchTechnologies(keyword)` - Search technology

### LocalStorageManager
**Path:** `firebase/LocalStorageManager.kt`

**Folders (Internal Storage):**
- `portfolio_images/` - Gambar portfolio
- `technology_icons/` - Icon technology

**Methods:**
- `savePortfolioImage(uri)` - Simpan gambar portfolio ke internal storage
- `saveTechnologyIcon(uri)` - Simpan icon technology ke internal storage
- `deleteImage(uriString)` - Hapus gambar dari internal storage
- `updateImage(oldUri, newUri, folder)` - Update gambar (hapus lama, simpan baru)
- `imageExists(uriString)` - Cek apakah gambar ada
- `getImageFile(uriString)` - Get File object dari URI

**CATATAN:** Gambar disimpan di `/data/data/com.example.kahasolusi_kotlin/files/` dan URI-nya (bukan download URL) disimpan di Firestore.

## 3. Cara Menggunakan di Activity

### Contoh: Login dengan Firebase

```kotlin
// Di LoginActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kahasolusi_kotlin.firebase.FirebaseAuthManager

class LoginActivity : AppCompatActivity() {
    private val authManager = FirebaseAuthManager()
    
    private fun performLogin() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        lifecycleScope.launch {
            val result = authManager.loginUser(email, password)
            result.onSuccess { user ->
                Toast.makeText(this@LoginActivity, 
                    "Login berhasil: ${user.email}", 
                    Toast.LENGTH_SHORT).show()
                // Navigate to main activity
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure { exception ->
                Toast.makeText(this@LoginActivity, 
                    "Login gagal: ${exception.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### Contoh: CRUD Portfolio dengan Firestore

```kotlin
// Di PortfolioAdminActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kahasolusi_kotlin.firebase.FirebasePortfolioRepository
import com.example.kahasolusi_kotlin.firebase.LocalStorageManager

class PortfolioAdminActivity : AppCompatActivity() {
    private val portfolioRepo = FirebasePortfolioRepository()
    private lateinit var storageManager: LocalStorageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageManager = LocalStorageManager(this)
    }
    
    private fun savePortfolio() {
        lifecycleScope.launch {
            // 1. Simpan gambar ke internal storage dulu
            val imageResult = storageManager.savePortfolioImage(selectedImageUri)
            
            imageResult.onSuccess { imageUri ->
                // 2. Simpan portfolio dengan URI lokal
                val portfolio = Portfolio(
                    judul = binding.etJudul.text.toString(),
                    kategori = binding.spinnerKategori.selectedItem.toString(),
                    lokasi = binding.etLokasi.text.toString(),
                    deskripsi = binding.etDeskripsi.text.toString(),
                    gambarUri = imageUri, // URI lokal (file://)
                    techStack = selectedTechStack
                )
                
                val result = portfolioRepo.addPortfolio(portfolio)
                result.onSuccess { portfolioId ->
                    Toast.makeText(this@PortfolioAdminActivity,
                        "Portfolio berhasil disimpan",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@PortfolioAdminActivity,
                        "Gagal menyimpan: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(this@PortfolioAdminActivity,
                    "Gagal simpan gambar: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadPortfolios() {
        lifecycleScope.launch {
            val result = portfolioRepo.getAllPortfolios()
            result.onSuccess { portfolios ->
                // Update RecyclerView
                portfolioAdapter.updateData(portfolios)
            }.onFailure { e ->
                Toast.makeText(this@PortfolioAdminActivity,
                    "Gagal load data: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### Contoh: Load Image dari URI Lokal

Gambar disimpan lokal, jadi bisa langsung load dengan URI tanpa library tambahan:

```kotlin
// Di Adapter atau Activity
imageView.setImageURI(Uri.parse(portfolio.gambarUri))

// Atau dengan Glide (lebih smooth, ada caching)
Glide.with(context)
    .load(portfolio.gambarUri) // URI lokal (file://)
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error_image)
    .into(imageView)
```

## 4. Data Models (Sudah Compatible)

Portfolio dan Technology models sudah siap untuk Firebase:

```kotlin
data class Portfolio(
    var id: String = "",
    val judul: String = "",
    val kategori: String = "",
    val lokasi: String = "",
    val deskripsi: String = "",
    val gambarUri: String = "", // Local file URI (file://...)
    val techStack: String = ""
)

data class Technology(
    var id: String = "",
    val nama: String = "",
    val iconUri: String = "" // Local file URI (file://...)
)
```

## 5. Migration dari SharedPreferences

### Before (SharedPreferences):
```kotlin
// Save
sharedPreferences.savePortfolio(portfolio)

// Load
val portfolios = sharedPreferences.getPortfolios()
```

### After (Firebase):
```kotlin
// Save
lifecycleScope.launch {
    portfolioRepo.addPortfolio(portfolio)
}

// Load
lifecycleScope.launch {
    val result = portfolioRepo.getAllPortfolios()
    result.onSuccess { portfolios ->
        // Use data
    }
}
```

## 6. Testing

### Test Authentication:
1. Register user baru dengan email & password
2. Login dengan credential
3. Check `FirebaseAuth.getInstance().currentUser`

### Test Firestore:
1. Add portfolio/technology
2. Check di Firebase Console > Firestore Database
3. Load data di app, pastikan muncul

### Test Local Storage:
1. Upload gambar
2. Check di Device File Explorer: `/data/data/com.example.kahasolusi_kotlin/files/`
3. Load image dengan URI di app

## 7. Important Notes

⚠️ **Security:**
- Jangan commit `google-services.json` ke Git
- Update Security Rules sebelum production
- Implement proper error handling

⚠️ **Performance:**
- Gambar disimpan di internal storage, tidak perlu download
- Cache images dengan Glide untuk performa lebih baik
- Use offline persistence: `FirebaseFirestore.getInstance().enableNetwork()`

⚠️ **Limitations:**
- Gambar TIDAK shared antar device (hanya lokal)
- Jika user uninstall app, gambar hilang
- Setiap device punya copy gambar sendiri
- Untuk production dengan multi-device sync, pertimbangkan Firebase Storage berbayar atau cloud hosting lain

⚠️ **Best Practices:**
- Gunakan `lifecycleScope` untuk coroutines
- Handle loading states (show/hide progress)
- Validate input sebelum save
- Delete old images saat update

## 8. Next Steps

1. **Download google-services.json** dari Firebase Console
2. **Enable Authentication & Firestore** di Firebase Console (SKIP Storage)
3. **Sync Gradle** - Build > Rebuild Project
4. **Update Activities** untuk gunakan Firebase classes
5. **Test** register, login, CRUD operations
6. **Deploy** security rules untuk production

## Troubleshooting

**Error: google-services.json not found**
- Download dari Firebase Console
- Letakkan di `app/google-services.json`
- Sync Gradle

**Error: Default FirebaseApp is not initialized**
- Pastikan google-services.json ada
- Check applicationId di build.gradle sesuai
- Clean & Rebuild project

**Error: Permission denied (Firestore/Storage)**
- Update Security Rules di Firebase Console
- Pastikan user sudah login (`request.auth != null`)

**Images tidak muncul:**
- Check URI valid (file://...)
- Pastikan file ada di internal storage
- Check read permission
- Use Glide untuk handle URI dengan lebih baik

**Images hilang setelah reinstall app:**
- Normal behavior untuk local storage
- Data di internal storage terhapus saat uninstall
- Firestore metadata tetap ada, tapi file image hilang
- Solusi: Backup/restore mechanism atau gunakan cloud storage berbayar
