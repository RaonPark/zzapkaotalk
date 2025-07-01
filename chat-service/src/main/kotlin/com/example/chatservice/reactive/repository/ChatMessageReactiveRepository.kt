package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageReactiveRepository: CoroutineSortingRepository<ChatMessage, Long> {
    suspend fun findAllByChatRoomIdOrderByCreatedDateDesc(chatRoomId: Long, pageable: Pageable): Flow<ChatMessage>
    suspend fun save(chatMessage: ChatMessage): ChatMessage
}