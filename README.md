# 🐷 BaTabung - AI-Powered Savings Tracker

**BaTabung** adalah aplikasi Android untuk mengelola tabungan pribadi dengan bantuan AI. Dilengkapi dengan asisten AI pintar yang dapat menjawab pertanyaan tentang keuangan Anda.

## ✨ Fitur Utama

### 💬 AI Chat Assistant
- Chat dengan **BaTabung AI** untuk bertanya tentang tabungan
- Powered by **Google Gemini 2.0 Flash**
- Intent detection dengan fuzzy matching
- Konteks keuangan real-time

### 📊 Dashboard
- Lihat saldo dan progress tabungan
- Grafik pemasukan vs pengeluaran
- Target savings tracker

### 💰 Manajemen Transaksi
- Catat pemasukan dan pengeluaran
- Kategorisasi transaksi
- Riwayat transaksi lengkap

### 📈 Analytics
- Analisis keuangan mendalam
- Visualisasi data dengan charts
- Insights tentang pola pengeluaran

## 🛠️ Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| AI | Google Gemini API |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Charts | Vico |

## 📱 Screenshots

> Coming soon

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog atau lebih baru
- JDK 17
- Android SDK 26+ (minSdk)

### Setup

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/BaTabung.git
   cd BaTabung
   ```

2. **Buka di Android Studio**
   ```
   File → Open → pilih folder BaTabung
   ```

3. **Sync Gradle**
   - Tunggu Android Studio sync dependencies

4. **Run aplikasi**
   - Pilih device/emulator
   - Klik Run ▶️

### Konfigurasi API Key (Opsional)

Aplikasi akan meminta API key saat pertama kali membuka Chat. Anda juga bisa set di `local.properties`:

```properties
GEMINI_API_KEY=your_api_key_here
```

> 💡 Dapatkan API key gratis di [Google AI Studio](https://aistudio.google.com/apikey)

## 📁 Project Structure

```
app/src/main/java/com/example/batabung/
├── ai/                    # AI Module
│   ├── AIController.kt    # Gemini AI integration
│   ├── PromptBuilder.kt   # Prompt engineering
│   └── model/             # AI data models
├── data/                  # Data Layer
│   ├── local/             # Room database
│   └── repository/        # Repositories
├── di/                    # Hilt modules
├── navigation/            # Navigation graph
├── ui/                    # UI Layer
│   ├── chat/              # Chat screen
│   ├── dashboard/         # Dashboard screen
│   ├── analytics/         # Analytics screen
│   ├── transaction/       # Transaction screens
│   └── theme/             # App theme
└── util/                  # Utilities
```

## 🔒 Security

- API key tidak disimpan di source code
- `local.properties` ada di `.gitignore`
- API key user disimpan secara lokal dengan DataStore

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

Made with ❤️ using Kotlin & Jetpack Compose
