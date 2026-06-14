<div align="center">
  <img src="assets/banner.png" width="1200" height="475" alt="Rubber Clone AI Banner" onError="this.style.display='none';" />
  <h1>Rubber Clone AI</h1>
  <p>Aplikasi Pengecaman & Analisis Klon Getah RISDA Malaysia Menggunakan Google Gemini AI</p>
</div>

---

**Rubber Clone AI** ialah aplikasi mudah alih pintar Android yang direka khusus untuk membantu pekebun kecil RISDA dan pegawai lapangan mengenalpasti klon pokok getah secara automatik melalui imbasan imej daun getah. Sistem ini memanfaatkan kuasa **Google Gemini AI** (melalui Gemini API) untuk menganalisis morfologi daun dan memberikan maklumat agronomi klon getah yang tepat.

## Ciri-ciri Utama

- **Imbasan Kamera Pintar**: Ambil gambar daun pokok getah secara langsung menggunakan kamera peranti atau muat naik dari galeri.
- **Pengecaman Berasaskan AI**: Menganalisis corak, urat, bentuk, dan ciri fizikal daun menggunakan model multimodal Google Gemini AI.
- **Pangkalan Data Klon Getah RISDA**: Paparan maklumat lengkap agronomi klon seperti potensi hasil susu, ketahanan penyakit, dan kesesuaian kawasan penanaman.
- **Sejarah Imbasan Tempatan**: Menyimpan sejarah imbasan terdahulu pada peranti untuk rujukan pantas.

---

## Prasyarat Pembangunan

Sebelum menjalankan projek ini, pastikan persekitaran pembangunan anda mempunyai:

1. **Android Studio** (Versi terkini disyorkan)
2. **JDK 17** (Pastikan `JAVA_HOME` dikonfigurasikan dengan betul)
3. **Android SDK (API 36 / 36.1)**
4. **Kunci API Gemini (Gemini API Key)** dari Google AI Studio

---

## Cara Menjalankan Projek Secara Tempatan

### 1. Klon Repositori
Dapatkan kod sumber ke komputer tempatan anda.

### 2. Konfigurasi Fail `.env`
Bina fail bernama `.env` di dalam direktori utama projek ini dan masukkan kunci API Gemini anda:
```env
GEMINI_API_KEY=KUNCI_API_GEMINI_ANDA_DI_SINI
```

### 3. Sediakan Pengkompilan (Build)
Pastikan anda membuang atau menyahaktifkan konfigurasi tandatangan debug tersuai dalam `app/build.gradle.kts` jika anda tidak mempunyai fail `.keystore` fizikal di direktori utama:
```kotlin
// Buang atau ulas baris ini di dalam blok buildTypes -> debug:
// signingConfig = signingConfigs.getByName("debugConfig")
```

### 4. Kompilasi APK Menggunakan Gradle
Buka terminal di direktori utama projek dan jalankan arahan berikut untuk membina APK debug:
```bash
# Untuk Linux/macOS
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebug

# Untuk Windows
set JAVA_HOME=C:\path\to\jdk-17
gradlew.bat assembleDebug
```
APK yang dihasilkan akan berada di: `app/build/outputs/apk/debug/app-debug.apk`.

### 5. Pasang dan Jalankan pada Emulator/Peranti
Jika anda mempunyai peranti atau emulator yang aktif (contoh: `emulator-5554`), pasang APK dengan arahan adb:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Hubungi & Maklum Balas
Untuk maklumat lanjut mengenai pengurusan klon getah dan panduan RISDA, sila layari portal rasmi [RISDA Malaysia](https://www.risda.gov.my).
