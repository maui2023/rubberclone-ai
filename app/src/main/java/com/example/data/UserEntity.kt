package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String,
    val fullname: String,
    val agency: String = "RISDA Pekebun Kecil",
    val registrationDate: Long = System.currentTimeMillis()
)
