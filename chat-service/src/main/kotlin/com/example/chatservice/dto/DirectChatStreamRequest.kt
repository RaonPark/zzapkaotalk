package com.example.chatservice.dto

data class DirectChatStreamRequest(
    val fromUserEmail: String,
    val toUserEmail: String,
)