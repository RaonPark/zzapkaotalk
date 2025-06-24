package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ChatMessageReactiveRepository: CoroutineSortingRepository<ChatMessage, Long> {

    suspend fun findAllByChatRoomId(chatRoomId: Long, pageable: Pageable): Flow<ChatMessage>
}