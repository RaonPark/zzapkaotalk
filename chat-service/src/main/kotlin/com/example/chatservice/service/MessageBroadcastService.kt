package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MessageBroadcastService(
    private val kafkaTemplate: KafkaTemplate<Long, ChatMessageBroadcast>,
) {

    suspend fun broadcastToChatRoom(chatMessageRequest: ChatMessageRequest) {

    }
}