package com.example.chatservice.dto

data class GetAllChatMessagesRequest(
    val chatRoomId: Long,
    val userId: Long,
) {
}