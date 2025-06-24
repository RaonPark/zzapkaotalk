package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatRoom
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ChatRoomReactiveRepository: CoroutineCrudRepository<ChatRoom, Long> {
    suspend fun findChatRoomById(id: Long): ChatRoom
}