package com.example.userservice.dto

data class LoginRequest(
    val email: String,
    val password: String
)