package com.example.chatservice.dto

data class GetAllChatMessagesResponse(
    var content: String,
    var nickname: String? = null,
    var createdTime: String,
    var profileImage: String? = null
)