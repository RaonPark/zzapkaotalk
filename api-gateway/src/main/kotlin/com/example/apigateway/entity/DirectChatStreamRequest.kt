package com.example.apigateway.entity

data class DirectChatStreamRequest(
    val fromUserEmail: String,
    val toUserEmail: String,
)