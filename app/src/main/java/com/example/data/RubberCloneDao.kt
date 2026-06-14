package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RubberCloneDao {
    // === Queries untuk Analisis Klon ===
    @Query("SELECT * FROM analysis_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllAnalysesForUser(userId: String): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analysis_records ORDER BY timestamp DESC")
    fun getAllAnalysesGlobal(): Flow<List<AnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)

    @Query("DELETE FROM analysis_records WHERE id = :id")
    suspend fun deleteAnalysisById(id: Int)

    @Query("DELETE FROM analysis_records WHERE userId = :userId")
    suspend fun clearAllAnalysesForUser(userId: String)

    // === Queries untuk Pengguna ===
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}
