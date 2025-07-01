package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageRequest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MessageBroadcastService(
    private val kafkaTemplate: KafkaTemplate<Long, ChatMessageBroadcast>,
) {

    suspend fun broadcastToChatRoom(chatMessageRequest: ChatMessageRequest) {
        val broadcast = convertRequestToBroadcast(chatMessageRequest)
        kafkaTemplate.executeInTransaction {
            kafkaTemplate.send("chat-message-broadcast", chatMessageRequest.chatRoomId, broadcast)
        }
    }

    private fun convertRequestToBroadcast(request: ChatMessageRequest): ChatMessageBroadcast {
        return ChatMessageBroadcast.newBuilder()
            .setMessage(request.content)
            .setChatRoomId(request.chatRoomId)
            .setFrom(request.fromUserId)
            .build()
    }
}