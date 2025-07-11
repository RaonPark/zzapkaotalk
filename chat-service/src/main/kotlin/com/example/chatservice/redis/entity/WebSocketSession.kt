package com.example.chatservice.redis.entity

data class WebSocketSession(
    var userId: Long,
    var websocketServer: String,
)