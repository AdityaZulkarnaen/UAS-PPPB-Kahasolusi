# KAHASOLUSI - Android Application

Aplikasi Android yang dibangun dengan Kotlin menggunakan arsitektur MVVM (Model-View-ViewModel) yang proper dan modern.

## 📱 Fitur Utama

- Tampilan Home dengan list aplikasi berbasis card
- Bottom Navigation dengan 3 tab: Home, Portofolio, Technology
- Toolbar dengan ikon profil
- Arsitektur MVVM yang clean dan scalable

## 🏗️ Arsitektur

Aplikasi ini menggunakan **MVVM (Model-View-ViewModel)** architecture pattern dengan struktur sebagai berikut:

```
app/
├── data/
│   ├── model/          # Data classes
│   │   └── AppItem.kt
│   └── repository/     # Data repository layer
│       └── AppRepository.kt
├── ui/
│   ├── home/
│   │   ├── adapter/
│   │   │   └── AppItemAdapter.kt
│   │   ├── HomeFragment.kt        # View
│   │   ├── HomeViewModel.kt       # ViewModel
│   │   └── HomeViewModelFactory.kt
│   ├── dashboard/
│   └── notifications/
└── MainActivity.kt
```

### Layer Explanation:

#### 1. **Model Layer** (`data/model/`)
- Berisi data classes yang merepresentasikan struktur data aplikasi
- `AppItem.kt`: Model untuk item aplikasi dengan properties: id, title, subtitle, imageUrl, category

#### 2. **Repository Layer** (`data/repository/`)
- Abstraksi untuk sumber data (API, Database, Cache)
- `AppRepository.kt`: Mengelola data menggunakan Kotlin Flow untuk reactive programming
- Menggunakan Singleton pattern untuk single instance

#### 3. **View Layer** (`ui/`)
- **Fragment**: Menampilkan UI dan berinteraksi dengan user
- **Adapter**: RecyclerView adapter dengan ListAdapter dan DiffUtil untuk performa optimal
- Menggunakan ViewBinding untuk type-safe view access

#### 4. **ViewModel Layer**
- `HomeViewModel.kt`: Mengelola UI state dan business logic
- Menggunakan LiveData untuk observable data
- Menggunakan Coroutines untuk asynchronous operations
- `HomeViewModelFactory.kt`: Factory pattern untuk dependency injection

## 🎨 Design Patterns Used

1. **MVVM (Model-View-ViewModel)**
   - Separation of concerns
   - Testable code
   - Lifecycle aware components

2. **Repository Pattern**
   - Abstraction layer untuk data sources
   - Single source of truth

3. **Factory Pattern**
   - ViewModelFactory untuk dependency injection

4. **Singleton Pattern**
   - AppRepository instance management

5. **Observer Pattern**
   - LiveData untuk reactive UI updates
   - Flow untuk data streams

## 🛠️ Technologies & Libraries

- **Language**: Kotlin
- **UI**: XML Layouts with Material Design Components
- **Architecture Components**:
  - ViewModel
  - LiveData
  - ViewBinding
- **Coroutines**: Untuk asynchronous programming
- **Flow**: Untuk reactive data streams
- **Navigation Component**: Untuk navigasi antar fragment
- **RecyclerView**: Dengan ListAdapter dan DiffUtil

## 📦 Project Structure Details

### Key Components:

1. **MainActivity.kt**
   - Host activity yang mengelola navigation
   - Setup bottom navigation
   - Handle toolbar menu (profile icon)

2. **HomeFragment.kt**
   - Menampilkan list aplikasi
   - Setup RecyclerView dengan adapter
   - Observe ViewModel data

3. **HomeViewModel.kt**
   - Manage UI state (appItems, isLoading)
   - Communicate dengan Repository
   - Expose LiveData untuk Fragment

4. **AppItemAdapter.kt**
   - RecyclerView.Adapter dengan ListAdapter
   - DiffUtil untuk efficient list updates
   - Handle item click events

5. **AppRepository.kt**
   - Provide data menggunakan Flow
   - Singleton instance management
   - Ready untuk integration dengan API/Database

## 🎯 Best Practices Implemented

1. ✅ **Separation of Concerns**: Setiap class memiliki single responsibility
2. ✅ **Dependency Injection**: Menggunakan Factory pattern untuk inject dependencies
3. ✅ **Lifecycle Awareness**: ViewModel survive configuration changes
4. ✅ **Memory Leak Prevention**: Proper binding cleanup di onDestroyView
5. ✅ **Type Safety**: ViewBinding untuk menghindari findViewById
6. ✅ **Reactive Programming**: LiveData dan Flow untuk reactive updates
7. ✅ **Efficient Lists**: ListAdapter dengan DiffUtil untuk optimal performance
8. ✅ **Material Design**: Menggunakan Material Design Components

## 🚀 Future Improvements

1. **Dependency Injection**: Implementasi Hilt/Dagger untuk proper DI
2. **Image Loading**: Integrasi Glide/Coil untuk load images
3. **Network Layer**: Implementasi Retrofit untuk API calls
4. **Local Database**: Room database untuk offline support
5. **Unit Tests**: ViewModel dan Repository testing
6. **UI Tests**: Espresso untuk UI testing

## 📝 How to Run

1. Clone repository
2. Open di Android Studio
3. Sync Gradle
4. Run aplikasi di emulator atau device

## 🔄 Data Flow

```
User Action → View (Fragment) → ViewModel → Repository → Data Source
                ↑                   ↓
                └── LiveData/Flow ──┘
```

## 📄 License

This project is part of UAS-PPPB-Kahasolusi course project.
