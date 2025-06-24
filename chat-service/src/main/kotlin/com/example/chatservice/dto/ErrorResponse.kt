package com.example.chatservice.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val errorCode: String,
    val statusCode: String,
    val message: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val path: String,
) {
}