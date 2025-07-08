package com.example.chatservice.dto

import java.time.LocalDateTime

data class DirectChatMessageResponse(
    var fromUserId: Long,
    var toUserId: Long,
    var message: String,
    var createdTime: LocalDateTime,
)