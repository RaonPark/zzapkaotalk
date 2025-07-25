package com.example.userservice.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val profileImage: String,
    val nickname: String,
)