package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisMessagePublishService(
    private val chatRedisTemplate: ReactiveRedisTemplate<String, ChatMessageBroadcast>
) {
    suspend fun publishChatMessageBroadcast(chatMessageBroadcast: ChatMessageBroadcast) {
        chatRedisTemplate.convertAndSend("chat:room:${chatMessageBroadcast.chatRoomId}", chatMessageBroadcast)
            .awaitSingle()
    }
}