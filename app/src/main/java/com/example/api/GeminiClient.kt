package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    // Sesuai dengan spesifikasi model gemini-3.5-flash dalam garis panduan
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeLeafImage(bitmap: Bitmap): JSONObject? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder! Skipping live Gemini API call.")
            return@withContext null
        }

        val prompt = """
            You are an expert rubber tree agronomist specialized in Hevea brasiliensis (pokok getah) for RISDA Malaysia.
            Analyze the provided image of a rubber tree leaf and identify which of these standard RISDA clones it matches best:
            1. RRIM 2025 (RRIM 2000 Series, yield 2500-3000 kg/ha/yr)
            2. PB 260 (Prang Besar Series, yield 2000-2500 kg/ha/yr)
            3. RRIM 3001 (RRIM 3000 Series, yield 2800-3200 kg/ha/yr)
            4. RRIM 600 (Modern Heritage, yield 1500-1800 kg/ha/yr)
            5. PR 255 (PR Series, yield 1800-2200 kg/ha/yr)
            
            Return your response STRICTLY as a single valid JSON object. Do not wrap in markdown or anything else.
            Syntax template:
            {
               "clone_id": "rrim_2025", 
               "clone_name": "RRIM 2025",
               "series": "RRIM 2000 Series",
               "confidence": 0.94,
               "notes": "Analisis imej menunjukkan urat daun tebal beralun tipikal bagi klon ini dengan darjah kilauan sederhana...",
               "disease_status": "Sangat Tinggi (Rintang Corynespora & Oidium)",
               "soil_suitability": "Tanah Lempung Pasir (Sandy Clay) & Tanah Lateral Kelikir",
               "rainfall_needed": "2,000 - 2,800 mm",
               "max_elevation": "350 meter"
            }
            Do not include any text other than the raw JSON object. Use Bahasa Melayu for the notes/description.
        """.trimIndent()

        try {
            val base64Image = bitmap.toBase64()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObject = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            put(JSONObject().put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            }))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObject)
                }
                put("contents", contentsArray)
                
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", "You are a professional rubber-tree agronomist. Output only parseable JSON.")))
                })

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} / ${response.message}")
                    return@withContext null
                }
                val bodyText = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Response body raw: $bodyText")

                val responseJson = JSONObject(bodyText)
                val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
                if (candidates.length() == 0) return@withContext null
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: return@withContext null
                val parts = content.optJSONArray("parts") ?: return@withContext null
                if (parts.length() == 0) return@withContext null
                val textResponse = parts.getJSONObject(0).optString("text", "")

                var trimmed = textResponse.trim()
                if (trimmed.startsWith("```json")) {
                    trimmed = trimmed.removePrefix("```json")
                }
                if (trimmed.endsWith("```")) {
                    trimmed = trimmed.removeSuffix("```")
                }
                trimmed = trimmed.trim()

                return@withContext JSONObject(trimmed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun generateSmartRecommendation(
        soilType: String,
        rainfall: String,
        elevation: String,
        pastYield: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty! Skipping recommendation Gemini API call.")
            return@withContext null
        }

        val prompt = """
            Anda adalah pakar penasihat pertanian pintar RISDA Malaysia. 
            Sila berikan cadangan klon getah terbaik (pilih kombinasi terbaik daripada RRIM 2025, PB 260, RRIM 3001, RRIM 600) untuk parameter berikut:
            - Jenis Tanah: ${soilType}
            - Purata Hujan Tahunan: ${rainfall} mm
            - Ketinggian Kawasan dari paras laut: ${elevation} meter
            - Sejarah Hasil Terdahulu (kg/ha/tahun): ${pastYield}
            
            Sila sediakan laporan cadangan dalam Bahasa Melayu yang tersusun rapi. 
            Formatkan output dalam sintaks Markdown ringkas dengan seksyen:
            1. **Klon Utama Disyorkan** (sertakan jangkaan % kecocokan, e.g. 94%, dan penerangan ringkas sebab dipilih).
            2. **Analisis Kesesuaian Rupa Bumi** (tanah, hujan, ketinggian).
            3. **Panduan Pengurusan & Input** (cadangan pembajaan dan kawalan penyakit untuk klon ini).
            Sila pastikan ulasan anda profesional, bermaklumat, dan membina untuk meningkatkan hasil getah pekebun kecil RISDA.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObject = JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    }
                    put(contentObject)
                }
                put("contents", contentsArray)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyText = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyText)
                val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
                if (candidates.length() == 0) return@withContext null
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: return@withContext null
                val parts = content.optJSONArray("parts") ?: return@withContext null
                if (parts.length() == 0) return@withContext null
                return@withContext parts.getJSONObject(0).optString("text", "No text provided")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling recommendation Gemini API: ${e.message}")
            return@withContext null
        }
    }
}
