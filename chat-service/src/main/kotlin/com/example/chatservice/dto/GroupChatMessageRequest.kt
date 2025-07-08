package com.example.chatservice.dto

import java.time.LocalDateTime

data class GroupChatMessageRequest(
    val content: String,
    val fromUserId: Long,
    val chatRoomId: Long,
    val createdTime: LocalDateTime,
)
