package com.example.chatservice.dto

data class ChatMessageRequest(
    val content: String,
    val fromUserId: Long,
    val chatRoomId: Long,
) {
}
