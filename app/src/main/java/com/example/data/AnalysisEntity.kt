package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_records")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Menghubungkan imbasan dengan akaun pengguna semasa
    val cloneName: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val locationName: String = "Stesen RISDA, Malaysia",
    val imageUrl: String? = null,
    val notes: String = "",
    val soilType: String = "Tiada Maklumat",
    val rainfall: String = "Tiada Maklumat",
    val elevation: String = "Tiada Maklumat"
)
