package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.DirectChatMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import reactor.core.publisher.Mono

interface DirectChatMessageReactiveRepository: CoroutineSortingRepository<DirectChatMessage, Long> {
    fun findByFromUserId(fromUserId: Long): Flow<DirectChatMessage>
}