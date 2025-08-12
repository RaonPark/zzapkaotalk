package com.example.apigateway.entity

import java.time.LocalDateTime

data class DirectChatMessageRequest(
    val message: String,
    val fromUserId: Long,
    val toUserId: Long,
    val timestamp: LocalDateTime,
)