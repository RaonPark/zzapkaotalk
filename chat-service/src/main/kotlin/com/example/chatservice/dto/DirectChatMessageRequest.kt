package com.example.chatservice.dto

import java.time.LocalDateTime

data class DirectChatMessageRequest(
    val message: String,
    val fromUserEmail: String,
    val toUserEmail: String,
    val timestamp: LocalDateTime,
)