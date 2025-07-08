package com.example.chatservice.dto

import java.time.LocalDateTime

data class GroupChatMessageResponse(
    var chatRoomId: Long,
    var userId: Long,
    var content: String,
    var createdTime: LocalDateTime,
)