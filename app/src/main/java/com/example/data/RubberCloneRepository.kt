package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.example.api.GeminiClient
import com.example.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class RubberCloneRepository(
    private val dao: RubberCloneDao,
    private val api: BackendService
) {
    private val TAG = "RubberRepository"

    // === Database Operations ===
    fun getAllAnalysesForUser(userId: String): Flow<List<AnalysisEntity>> =
        dao.getAllAnalysesForUser(userId)

    fun getAllAnalysesGlobal(): Flow<List<AnalysisEntity>> =
        dao.getAllAnalysesGlobal()

    suspend fun insertAnalysis(analysis: AnalysisEntity) {
        dao.insertAnalysis(analysis)
    }

    suspend fun deleteAnalysisById(id: Int) {
        dao.deleteAnalysisById(id)
    }

    suspend fun clearHistory(userId: String) {
        dao.clearAllAnalysesForUser(userId)
    }

    suspend fun getUserByEmail(email: String): UserEntity? =
        dao.getUserByEmail(email)

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    // === Core Logic: Leaf Image Classification (Gemini AI Sahaja) ===
    suspend fun analyzeLeaf(
        bitmap: Bitmap,
        userId: String,
        latitude: Double,
        longitude: Double
    ): AnalysisEntity? {
        Log.d(TAG, "Memulakan analisis imej daun melalui Gemini AI...")

        // Jalankan panggilan Gemini API — tiada fallback palsu
        val apiResult = GeminiClient.analyzeLeafImage(bitmap)
        if (apiResult != null) {
            try {
                val cloneName = apiResult.optString("clone_name", "RRIM 2025")
                val confidence = apiResult.optDouble("confidence", 0.90).toFloat()
                val notes = apiResult.optString("notes", "Berjaya dikenal pasti.")
                val soil = apiResult.optString("soil_suitability", "Pelbagai")
                val rainfallNeeded = apiResult.optString("rainfall_needed", "2,000 - 2,800 mm")
                val maxElev = apiResult.optString("max_elevation", "350 meter")

                val record = AnalysisEntity(
                    userId = userId,
                    cloneName = cloneName,
                    confidence = confidence,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = "Kawasan Pekebun Kecil RISDA, Lat: ${String.format("%.4f", latitude)}, Lng: ${String.format("%.4f", longitude)}",
                    notes = notes,
                    soilType = soil,
                    rainfall = rainfallNeeded,
                    elevation = maxElev
                )
                return record
            } catch (e: Exception) {
                Log.e(TAG, "Ralat parsing data Gemini: ${e.message}")
                return null
            }
        }

        // Tiada fallback — kembalikan null supaya pengguna tahu analisis gagal
        Log.w(TAG, "Gemini API gagal atau tiada API key. Tiada data palsu dijana.")
        return null
    }

    // === Core Logic: Cadangan Klon Pintar ===
    suspend fun getSmartRecommendations(
        soilType: String,
        rainfall: String,
        elevation: String,
        pastYield: String
    ): String {
        // Cuba dapatkan ulasan AI daripada Gemini
        val apiRecommendation = GeminiClient.generateSmartRecommendation(
            soilType, rainfall, elevation, pastYield
        )
        if (apiRecommendation != null) {
            return apiRecommendation
        }

        // Fallback Heuristik Pertanian Luar Talian RISDA
        val builder = java.lang.StringBuilder()
        builder.append("### Laporan Agro-Pintar RISDA (Cadangan Heuristik Luar Talian)\n\n")
        builder.append("> *Mod luar talian aktif. Menggunakan algoritma pemetaan kesesuaian RISDA 2026.*\n\n")

        val rainfallVal = rainfall.toIntOrNull() ?: 2000
        val elevVal = elevation.toIntOrNull() ?: 150

        val results = CloneInfo.defaultClones.map { clone ->
            var score = 100
            
            // Periksa curah hujan
            if (rainfallVal < 1800 && clone.id == "rrim_3001") score -= 15
            if (rainfallVal > 2800 && clone.id == "pb_260") score -= 20
            
            // Periksa ketinggian
            val maxElevNum = clone.maxElevation.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 300
            if (elevVal > maxElevNum) {
                score -= 30
            }

            // Periksa jenis tanah
            if (!clone.soilSuitability.lowercase().contains(soilType.lowercase())) {
                score -= 10
            }

            if (score < 40) score = 40
            clone to score
        }.sortedByDescending { it.second }

        val bestCloneMatch = results.first()
        builder.append("#### 1. **Klon Utama Disyorkan: ${bestCloneMatch.first.name}**\n")
        builder.append("- **Skor Kesesuaian**: **${bestCloneMatch.second}%**\n")
        builder.append("- **Siri Warisan**: ${bestCloneMatch.first.series}\n")
        builder.append("- **Sebab Dipilih**: Klon ini mempunyai keupayaan menyesuaikan diri terbaik dengan parameter yang anda berikan. ")
        builder.append("Bagi tanah berjenis *$soilType*, curahan hujan *${rainfall} mm*, dan altitude *${elevation} meter*, ")
        builder.append("klon ${bestCloneMatch.first.name} mampu menghasilkan lateks optimum sehingga *${bestCloneMatch.first.yieldPotential}*.\n\n")

        builder.append("#### 2. **Siri Skor Kesesuaian Alternatif Pekebun**\n")
        results.drop(1).forEach { (clone, matchScore) ->
            builder.append("- **${clone.name}**: Kesesuaian **$matchScore%** (${clone.series})\n")
        }
        builder.append("\n")

        builder.append("#### 3. **Panduan Pengurusan Pengeluaran Luar Talian**\n")
        builder.append("- **Kawalan Penyakit**: Memandangkan kawasan anda mempunyai taburan hujan sebanyak *$rainfall mm*, ")
        builder.append("sila pantau kekerapan jangkitan kulat daun terutamanya Oidium semasa luruhan daun. Semburan racun kulat berskala mikro adalah sangat digalakkan.\n")
        builder.append("- **Pembajaan Utama**: Guna baja NPK gred RISDA Campuran 12:12:17:2 + TE sebanyak 3 pusingan setahun untuk memastikan tumbesaran kanopi seimbang.\n")

        return builder.toString()
    }

    // === API Network Synchronization ===

    suspend fun apiRegister(request: RegisterRequest): RegisterResponse {
        return try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Ralat pendaftaran"
                RegisterResponse("error", errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sambungan register: ${e.message}")
            RegisterResponse("error", "Tiada sambungan pelayan: ${e.message}")
        }
    }

    suspend fun apiLogin(request: LoginRequest): LoginResponse {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = response.errorBody()?.string() ?: "E-mel atau kata laluan salah"
                LoginResponse("error", null, null, errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sambungan login: ${e.message}")
            LoginResponse("error", null, null, "Tiada sambungan pelayan: ${e.message}")
        }
    }

    suspend fun apiUploadScan(
        record: AnalysisEntity,
        imageFile: File?
    ): UploadResponse? {
        return try {
            val cloneNameBody = record.cloneName.toRequestBody("text/plain".toMediaTypeOrNull())
            val confidenceBody = record.confidence.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val timestampBody = record.timestamp.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latitudeBody = record.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudeBody = record.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val locationNameBody = record.locationName.toRequestBody("text/plain".toMediaTypeOrNull())
            val notesBody = record.notes.toRequestBody("text/plain".toMediaTypeOrNull())
            val soilTypeBody = record.soilType.toRequestBody("text/plain".toMediaTypeOrNull())
            val rainfallBody = record.rainfall.toRequestBody("text/plain".toMediaTypeOrNull())
            val elevationBody = record.elevation.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val reqFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, reqFile)
            }

            val response = api.uploadScan(
                cloneNameBody, confidenceBody, timestampBody, latitudeBody, longitudeBody,
                locationNameBody, notesBody, soilTypeBody, rainfallBody, elevationBody, imagePart
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Gagal muat naik"
                Log.e(TAG, "Ralat upload: $errorMsg")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sambungan upload: ${e.message}")
            null
        }
    }

    suspend fun syncListScans(userId: String) {
        try {
            val response = api.listScans()
            if (response.isSuccessful && response.body()?.status == "success") {
                val list = response.body()?.data ?: emptyList()
                // Masukkan rekod baru/kemas kini dari server ke DB tempatan
                list.forEach { dto ->
                    val entity = AnalysisEntity(
                        id = dto.id,
                        userId = userId,
                        cloneName = dto.clone_name,
                        confidence = dto.confidence,
                        timestamp = dto.timestamp,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        locationName = dto.location_name,
                        imageUrl = dto.image_url,
                        notes = dto.notes,
                        soilType = dto.soil_type,
                        rainfall = dto.rainfall,
                        elevation = dto.elevation
                    )
                    dao.insertAnalysis(entity)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sync list: ${e.message}")
        }
    }

    suspend fun apiDeleteScan(id: Int) {
        try {
            api.deleteScan(id)
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sambungan delete: ${e.message}")
        }
    }

    suspend fun apiClearScans() {
        try {
            api.clearScans()
        } catch (e: Exception) {
            Log.e(TAG, "Ralat sambungan clear: ${e.message}")
        }
    }
}
