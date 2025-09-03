package com.example.chatservice.dto

import org.springframework.http.HttpStatus

data class DirectChatSendResponse(
    val status: HttpStatus,
    val timestamp: String,
)