package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.api.*
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed interface Screen {
    object Splash : Screen
    object Login : Screen
    object Register : Screen
    object Dashboard : Screen
    object AnalisisKlon : Screen
    object SejarahAnalisis : Screen
    object PetaLokasi : Screen
    object CadanganPintar : Screen
    object ProfilPengguna : Screen
    object CloneDirectory : Screen
    data class LaporanId(val recordId: Int) : Screen
}

@OptIn(ExperimentalCoroutinesApi::class)
class RubberCloneViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "RubberViewModel"
    private val apiService = RetrofitClient.getApiService(application)
    private val db = RubberCloneDatabase.getDatabase(application)
    private val repository = RubberCloneRepository(db.rubberCloneDao(), apiService)
    private val sessionManager = SessionManager(application)

    // === UI Navigation & Authentication State ===
    var currentScreen by mutableStateOf<Screen>(Screen.Splash)
    var currentUserEmail by mutableStateOf<String?>("ahmad@risda.gov.my")
    var currentUser by mutableStateOf<UserEntity?>(
        UserEntity(
            email = "ahmad@risda.gov.my",
            username = "Ahmad RISDA",
            passwordHash = "123456",
            fullname = "En. Ahmad Bin Ismail",
            agency = "RISDA Daerah Seremban"
        )
    )

    // Form inputs
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var loginError by mutableStateOf("")

    var regEmail by mutableStateOf("")
    var regUsername by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regFullname by mutableStateOf("")
    var regAgency by mutableStateOf("RISDA Pekebun Kecil")
    var regError by mutableStateOf("")

    // === Lists & Statistics ===
    val analysesForUser: StateFlow<List<AnalysisEntity>> = snapshotFlow { currentUserEmail }
        .flatMapLatest { email ->
            if (email != null) {
                repository.getAllAnalysesForUser(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnalysesGlobal: StateFlow<List<AnalysisEntity>> = repository.getAllAnalysesGlobal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Scan State ===
    var isAnalyzing by mutableStateOf(false)
    var predictionResult by mutableStateOf<AnalysisEntity?>(null)
    var scanBitmap by mutableStateOf<Bitmap?>(null)
    var leafScanSource by mutableStateOf<String>("Guna Gambar") // "Guna Gambar", "Sampel"

    // Moking GPS defaults
    var activeLatitude by mutableStateOf(2.9348) // RISDA HQ Putrajaya region/HQ KL
    var activeLongitude by mutableStateOf(101.6911)

    // === Recommendation State ===
    var isRecommending by mutableStateOf(false)
    var smartRecommendationReport by mutableStateOf<String?>(null)
    
    // Inputs for Smart recommendation
    var inputSoilType by mutableStateOf("Tanah Lempung Pasir (Sandy Clay)")
    var inputRainfall by mutableStateOf("2200")
    var inputElevation by mutableStateOf("180")
    var inputPastYield by mutableStateOf("2100")

    // Primary clones list
    val clonesInfo = CloneInfo.defaultClones

    // Selected item for PDF / Share
    var activeReportRecord by mutableStateOf<AnalysisEntity?>(null)

    init {
        // Semak session sedia ada pada permulaan
        val savedEmail = sessionManager.fetchUserEmail()
        val savedToken = sessionManager.fetchAuthToken()
        if (!savedEmail.isNullOrEmpty() && !savedToken.isNullOrEmpty()) {
            currentUserEmail = savedEmail
            viewModelScope.launch {
                currentUser = repository.getUserByEmail(savedEmail)
                // Selaraskan data imbasan terkini dari server
                repository.syncListScans(savedEmail)
            }
            currentScreen = Screen.Dashboard
        } else {
            currentScreen = Screen.Login
            currentUserEmail = null
            currentUser = null
        }

        // Pre-create a demo RISDA user so they can login offline immediately
        viewModelScope.launch {
            val defaultUser = UserEntity(
                email = "ahmad@risda.gov.my",
                username = "Ahmad RISDA",
                passwordHash = "123456",
                fullname = "En. Ahmad Bin Ismail",
                agency = "RISDA Daerah Seremban"
            )
            val existing = repository.getUserByEmail(defaultUser.email)
            if (existing == null) {
                repository.insertUser(defaultUser)
                // Add some initial mock analyses for Ahmad so the history and map has gorgeous sample pins initially!
                val mock1 = AnalysisEntity(
                    userId = defaultUser.email,
                    cloneName = "RRIM 2025",
                    confidence = 0.94f,
                    timestamp = System.currentTimeMillis() - 86400000 * 3, // 3 hari lepas
                    latitude = 2.7258,
                    longitude = 101.9424,
                    locationName = "Kebun Seremban Jaya, Negeri Sembilan",
                    notes = "Analisis siri 2000 menunjukkan vena daun leper dan urat ketara. Kadar pertumbuhan mantap, rintang Corynespora.",
                    soilType = "Tanah Lempung Pasir (Sandy Clay)",
                    rainfall = "2,300 mm",
                    elevation = "120 meter"
                )
                val mock2 = AnalysisEntity(
                    userId = defaultUser.email,
                    cloneName = "PB 260",
                    confidence = 0.89f,
                    timestamp = System.currentTimeMillis() - 86400000 * 1, // Semalam
                    latitude = 3.0125,
                    longitude = 101.7942,
                    locationName = "Kawasan Cheras Jaya, Selangor",
                    notes = "Imej daun runcing tirus. Kulit kayu sesuai untuk sistem torehan d4. Pertumbuhan awal pantas.",
                    soilType = "Tanah Aluvium Rendah",
                    rainfall = "1,950 mm",
                    elevation = "80 meter"
                )
                val mock3 = AnalysisEntity(
                    userId = defaultUser.email,
                    cloneName = "RRIM 3001",
                    confidence = 0.95f,
                    timestamp = System.currentTimeMillis() - 3600000 * 4, // 4 jam lepas
                    latitude = 2.9124,
                    longitude = 101.6231,
                    locationName = "Sektor RISDA Kajang, Selangor",
                    notes = "Trifoliat lebar rontok sekata. Klon susu getah termoden yang mencatat rekod dahan utuh menentang angin ribut kencang.",
                    soilType = "Tanah Campuran Bukit",
                    rainfall = "2,500 mm",
                    elevation = "210 meter"
                )
                repository.insertAnalysis(mock1)
                repository.insertAnalysis(mock2)
                repository.insertAnalysis(mock3)
            }
            if (currentUserEmail == null) {
                // Tiada active session, tapi kita benarkan carian ahmad sbg fallback awal
                currentUserEmail = defaultUser.email
                currentUser = repository.getUserByEmail(defaultUser.email) ?: defaultUser
                currentScreen = Screen.Dashboard
            }
        }
    }

    // === Authentication Business Logic ===
    fun processLogin() {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            loginError = "Sila isi kedua-dua e-mel dan kata laluan."
            return
        }
        loginError = ""
        viewModelScope.launch {
            val response = repository.apiLogin(LoginRequest(loginEmail.trim(), loginPassword))
            if (response.status == "success" && response.token != null && response.user != null) {
                // Simpan token & e-mel session
                sessionManager.saveAuthToken(response.token)
                sessionManager.saveUserEmail(response.user.email)
                
                // Masukkan/kemas kini user ke local database
                val localUser = UserEntity(
                    email = response.user.email,
                    username = response.user.username,
                    passwordHash = loginPassword,
                    fullname = response.user.fullname,
                    agency = response.user.agency
                )
                repository.insertUser(localUser)
                
                // Selaraskan imbasan dari server
                repository.syncListScans(response.user.email)
                
                currentUserEmail = response.user.email
                currentUser = localUser
                currentScreen = Screen.Dashboard
                loginPassword = ""
            } else {
                loginError = response.message ?: "E-mel atau kata laluan tidak sah."
            }
        }
    }

    fun processRegister() {
        if (regEmail.isBlank() || regUsername.isBlank() || regPassword.isBlank() || regFullname.isBlank()) {
            regError = "Sila lengkapkan semua butiran borang."
            return
        }
        regError = ""
        viewModelScope.launch {
            val request = RegisterRequest(
                email = regEmail.trim(),
                username = regUsername.trim(),
                password = regPassword,
                fullname = regFullname.trim(),
                agency = regAgency
            )
            val response = repository.apiRegister(request)
            if (response.status == "success") {
                // Auto login selepas berjaya daftar
                loginEmail = regEmail.trim()
                loginPassword = regPassword
                processLogin()
                
                // Kosongkan form daftar
                regEmail = ""
                regUsername = ""
                regPassword = ""
                regFullname = ""
            } else {
                regError = response.message ?: "Ralat semasa mendaftar"
            }
        }
    }

    fun processLogout() {
        sessionManager.clearSession()
        currentUserEmail = null
        currentUser = null
        currentScreen = Screen.Login
    }

    // === AI Analysis Processing ===
    fun analyzeLeafImage(bitmap: Bitmap, presetCloneId: String? = null) {
        val email = currentUserEmail ?: return
        isAnalyzing = true
        predictionResult = null
        scanBitmap = bitmap
        
        viewModelScope.launch {
            try {
                val randomLat = 2.2 + (0.05 * (0..30).random()) - 0.7
                val randomLng = 101.4 + (0.05 * (0..30).random()) + 0.3
                
                val result = repository.analyzeLeaf(
                    bitmap,
                    email,
                    randomLat,
                    randomLng,
                    presetCloneId
                )
                
                // Simpan fail imej sementara untuk dimuat naik
                val tempFile = File(getApplication<Application>().cacheDir, "temp_scan_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                
                // Muat naik ke backend server
                val apiResponse = repository.apiUploadScan(result, tempFile)
                
                if (tempFile.exists()) tempFile.delete()
                
                val finalRecord = if (apiResponse?.status == "success" && apiResponse.data != null) {
                    val remoteId = apiResponse.data.id
                    val remoteImgUrl = apiResponse.data.image_url
                    
                    val syncedRecord = result.copy(
                        id = remoteId,
                        imageUrl = remoteImgUrl
                    )
                    repository.insertAnalysis(syncedRecord)
                    syncedRecord
                } else {
                    repository.insertAnalysis(result)
                    result
                }
                
                predictionResult = finalRecord
            } catch (e: Exception) {
                Log.e(TAG, "Ralat ketika menganalisis imej: ${e.message}")
            } finally {
                isAnalyzing = false
            }
        }
    }

    // === AI Recommendation Processing ===
    fun processSmartRecommendation() {
        isRecommending = true
        smartRecommendationReport = null
        viewModelScope.launch {
            try {
                val report = repository.getSmartRecommendations(
                    inputSoilType,
                    inputRainfall,
                    inputElevation,
                    inputPastYield
                )
                smartRecommendationReport = report
            } catch (e: Exception) {
                smartRecommendationReport = "Error: Gagal memproses data rupa bumi: ${e.message}"
            } finally {
                isRecommending = false
            }
        }
    }

    // === Action: Delete History Item ===
    fun deleteHistoryRecord(record: AnalysisEntity) {
        viewModelScope.launch {
            repository.apiDeleteScan(record.id)
            repository.deleteAnalysisById(record.id)
            if (activeReportRecord?.id == record.id) {
                activeReportRecord = null
            }
        }
    }

    // === Action: Clear All History ===
    fun clearHistory(email: String) {
        viewModelScope.launch {
            repository.apiClearScans()
            repository.clearHistory(email)
            activeReportRecord = null
        }
    }

    // === PDF Generation & WhatsApp Sharing Simulation ===
    fun generateAndSharePdf(context: Context, record: AnalysisEntity) {
        viewModelScope.launch {
            try {
                // Bina fail PDF sebenar menggunakan android.graphics.pdf.PdfDocument
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Size A4
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val paint = Paint()
                paint.color = Color.BLACK
                paint.textSize = 24.5f
                paint.isFakeBoldText = true

                // Header Tajuk RISDA
                canvas.drawText("LAPORAN RASMI ANALISIS KLON DAUN GETAH", 40f, 60f, paint)
                
                paint.textSize = 14f
                paint.color = Color.DKGRAY
                paint.isFakeBoldText = false
                canvas.drawText("Dikeluarkan Oleh: RubberClone AI - RISDA Smart Agriculture", 40f, 85f, paint)
                
                // Pembahagi garis gold
                paint.color = Color.parseColor("#D4AF37")
                paint.strokeWidth = 3f
                canvas.drawLine(40f, 100f, 555f, 100f, paint)

                // Maklumat Pengguna & Fail
                paint.color = Color.BLACK
                paint.textSize = 12f
                canvas.drawText("Pemeriksa: ${currentUser?.fullname ?: "Pengguna RISDA"}", 40f, 130f, paint)
                canvas.drawText("Agensi/Negeri: ${currentUser?.agency ?: "RISDA Cawangan"}", 40f, 150f, paint)
                canvas.drawText("Tarikh Imbasan: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(record.timestamp))}", 40f, 170f, paint)
                canvas.drawText("Koordinat GPS: ${String.format("%.4f", record.latitude)} N, ${String.format("%.4f", record.longitude)} E", 40f, 190f, paint)

                // Keputusan Utama Box
                paint.color = Color.parseColor("#F5F9F6")
                paint.style = Paint.Style.FILL
                canvas.drawRect(40f, 215f, 555f, 315f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#0F3820")
                paint.strokeWidth = 1.5f
                canvas.drawRect(40f, 215f, 555f, 315f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F3820")
                paint.textSize = 16f
                paint.isFakeBoldText = true
                canvas.drawText("KEPUTUSAN AI: ${record.cloneName}", 60f, 255f, paint)
                
                paint.color = Color.parseColor("#D4AF37")
                canvas.drawText("KADAR KEYAKINAN: ${String.format("%.1f", record.confidence * 100)}%", 60f, 285f, paint)

                // Maklumat Teknikal Klon
                paint.color = Color.BLACK
                paint.textSize = 12f
                paint.isFakeBoldText = true
                canvas.drawText("SPESIFIKASI DAN CADANGAN BOTANI RISDA:", 40f, 345f, paint)

                paint.isFakeBoldText = false
                canvas.drawText("- Anggaran Hasil Getah: ${record.rainfall}", 60f, 375f, paint) // Repurposed fields mapped inside record
                canvas.drawText("- Kesesuaian Rupa Bumi & Ketinggian: < ${record.elevation}", 60f, 395f, paint)
                canvas.drawText("- Jenis Tanah Optimum: ${record.soilType}", 60f, 415f, paint)

                // Catatan Pemeriksaan
                paint.isFakeBoldText = true
                canvas.drawText("CATATAN PEMERIKSAAN LAPANGAN:", 40f, 455f, paint)

                paint.isFakeBoldText = false
                paint.textSize = 11f
                val notesText = if (record.notes.length > 80) record.notes.substring(0, 80) + "..." else record.notes
                canvas.drawText(notesText, 60f, 485f, paint)

                // Footer tanda tangan RISDA Malaysia
                paint.color = Color.LTGRAY
                canvas.drawLine(40f, 740f, 555f, 740f, paint)
                paint.color = Color.GRAY
                paint.textSize = 9f
                canvas.drawText("Dokumen ini dijana secara digital melalui Sistem Kepintaran Buatan RubberClone AI RISDA Malaysia 2026.", 40f, 765f, paint)
                canvas.drawText("Sila bawa bersama peranti ketika rujukan dahan fizikal RISDA berlangsung.", 40f, 780f, paint)

                pdfDocument.finishPage(page)

                // Simpan fail ke storan dalaman cache
                val pdfDir = File(context.cacheDir, "laporan_risda")
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs()
                }
                val pdfFile = File(pdfDir, "Laporan_Klon_${record.cloneName.replace(" ", "_")}_${record.id}.pdf")
                FileOutputStream(pdfFile).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                Log.d(TAG, "Fail PDF berjaya dijana di cache: ${pdfFile.absolutePath}")

                // Kongsi melalui Intent FileProvider
                val pdfUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Laporan Analisis Klon Getah - ${record.cloneName}")
                    putExtra(Intent.EXTRA_TEXT, """
                        *LAPORAN ANALISIS KLON DAUN GETAH RISDA*
                        
                        Sistem RubberClone AI telah mengenal pasti pokok dengan perincian berikut:
                        - *Klon*: ${record.cloneName}
                        - *Keyakinan*: ${String.format("%.1f", record.confidence * 100)}%
                        - *Pemeriksa*: ${currentUser?.fullname}
                        - *Koordinat*: ${record.latitude}, ${record.longitude}
                        
                        Laporan teknikal lengkap berformat PDF dilampirkan bersama ini. Dijana oleh RISDA Malaysia Inovasi 2026.
                    """.trimIndent())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Melancarkan pemilih perkongsian (boleh pilih WhatsApp)
                val chooserIntent = Intent.createChooser(shareIntent, "Hantar Laporan Ke WhatsApp/Kongsi")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

            } catch (e: Exception) {
                Log.e(TAG, "Gagal menjana/berkongsi PDF: ${e.message}", e)
                Toast.makeText(context, "Sila pastikan kelayakan fail lengkap: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
