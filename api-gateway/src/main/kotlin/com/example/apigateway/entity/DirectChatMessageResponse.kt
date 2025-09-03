package com.example.apigateway.entity

import java.time.LocalDateTime

data class DirectChatMessageResponse(
    var fromUserEmail: String,
    var toUserEmail: String,
    var message: String,
    var createdTime: LocalDateTime,
)