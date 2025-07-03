package com.example.chatservice.dto

data class ChatMessageResponse(
    var chatRoomId: Long,
    var userId: Long,
    var profileImage: String? = null,
    var nickname: String? = null,
    var content: String,
    var createdTime: String? = null
)