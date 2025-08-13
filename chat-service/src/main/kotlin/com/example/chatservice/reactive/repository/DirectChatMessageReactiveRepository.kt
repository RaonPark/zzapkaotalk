package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.DirectChatMessage
import com.example.chatservice.reactive.entity.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import reactor.core.publisher.Mono

interface DirectChatMessageReactiveRepository: CoroutineSortingRepository<DirectChatMessage, Long> {
    suspend fun findByFromUserId(fromUserId: Long): Flow<DirectChatMessage>
    suspend fun findByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long): Flow<DirectChatMessage>

    @Query("""
        SELECT DISTINCT u.*
        FROM users u
        LEFT JOIN direct_chat_message d ON u.id = d.to_user_id
        WHERE d.from_user_id = :fromUserId
    """)
    suspend fun findDirectChatBuddy(fromUserId: Long): Flow<User>
}