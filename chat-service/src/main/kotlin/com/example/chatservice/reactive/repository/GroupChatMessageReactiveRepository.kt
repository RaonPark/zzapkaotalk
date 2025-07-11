package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.GroupChatMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupChatMessageReactiveRepository: CoroutineSortingRepository<GroupChatMessage, Long> {
    suspend fun findAllByChatRoomIdOrderByCreatedDateDesc(chatRoomId: Long, pageable: Pageable): Flow<GroupChatMessage>
    suspend fun save(groupChatMessage: GroupChatMessage): GroupChatMessage
}