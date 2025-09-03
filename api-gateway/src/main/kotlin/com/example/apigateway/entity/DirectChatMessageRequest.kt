package com.example.apigateway.entity

import java.time.LocalDateTime

data class DirectChatMessageRequest(
    val message: String,
    var fromUserEmail: String,
    val toUserEmail: String,
    val timestamp: LocalDateTime,
)