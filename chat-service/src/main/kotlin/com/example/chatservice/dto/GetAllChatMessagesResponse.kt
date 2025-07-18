package com.example.chatservice.dto

import java.time.LocalDateTime

data class GetAllChatMessagesResponse(
    var content: String,
    var createdTime: LocalDateTime,
)