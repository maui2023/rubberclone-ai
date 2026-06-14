package com.example.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface BackendService {
    @POST("index.php?url=api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("index.php?url=api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("index.php?url=api/analysis/upload")
    suspend fun uploadScan(
        @Part("clone_name") cloneName: RequestBody,
        @Part("confidence") confidence: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("location_name") locationName: RequestBody,
        @Part("notes") notes: RequestBody,
        @Part("soil_type") soilType: RequestBody,
        @Part("rainfall") rainfall: RequestBody,
        @Part("elevation") elevation: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<UploadResponse>

    @GET("index.php?url=api/analysis/list")
    suspend fun listScans(): Response<ScansResponse>

    @DELETE("index.php?url=api/analysis/delete")
    suspend fun deleteScan(@Query("id") id: Int): Response<DeleteResponse>

    @POST("index.php?url=api/analysis/clear")
    suspend fun clearScans(): Response<ClearResponse>
}

// Request & Response Data Classes
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val fullname: String,
    val agency: String
)

data class RegisterResponse(
    val status: String,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginUserDto(
    val id: Int,
    val email: String,
    val username: String,
    val fullname: String,
    val agency: String,
    val role: String,
    val status: String
)

data class LoginResponse(
    val status: String,
    val token: String?,
    val user: LoginUserDto?,
    val message: String?
)

data class UploadDataDto(
    val id: Int,
    val clone_name: String,
    val image_url: String?
)

data class UploadResponse(
    val status: String,
    val message: String,
    val data: UploadDataDto?
)

data class ScanDto(
    val id: Int,
    val username: String,
    val fullname: String?,
    val clone_name: String,
    val confidence: Float,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val location_name: String,
    val notes: String,
    val soil_type: String,
    val rainfall: String,
    val elevation: String,
    val image_url: String?
)

data class ScansResponse(
    val status: String,
    val data: List<ScanDto>
)

data class DeleteResponse(
    val status: String,
    val message: String
)

data class ClearResponse(
    val status: String,
    val message: String
)
