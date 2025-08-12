package com.example.chatservice.service

import com.chatservice.DirectChatMessageBroadcast
import com.chatservice.GroupChatMessageBroadcast
import com.example.chatservice.converter.DirectChatMessageConverter
import com.example.chatservice.dto.DirectChatMessageResponse
import com.example.chatservice.reactive.repository.DirectChatMessageReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

@Service
class MessageBroadcaster(
    private val userRepository: UserReactiveRepository,
    private val directChatMessageReactiveRepository: DirectChatMessageReactiveRepository,
    private val directChatMessageConverter: DirectChatMessageConverter
) {
    companion object {
        val log = KotlinLogging.logger { }
        val directChatFlow = ConcurrentHashMap<Long, MutableSharedFlow<DirectChatMessageResponse>>()
        val groupChatFlow = ConcurrentHashMap<Long, MutableSharedFlow<GroupChatMessageBroadcast>>()
    }

    suspend fun getDirectChatMessageStream(userEmail: String): Flow<DirectChatMessageResponse> {
        val userId = userRepository.findByEmail(userEmail).id
        return directChatFlow.computeIfAbsent(userId) {
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 128,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
        }
    }

    suspend fun getPreviousDirectChatMessage(userEmail: String): Flow<DirectChatMessageResponse> {
        val userId = userRepository.findByEmail(userEmail).id
        return directChatMessageReactiveRepository.findByFromUserId(userId)
            .map { directChatMessage ->
                directChatMessageConverter.modelToResponse(directChatMessage)
            }
    }

    suspend fun broadcastToDirect(broadcast: DirectChatMessageBroadcast) {
        directChatFlow[broadcast.toUserId]?.emit(
            DirectChatMessageResponse(
                fromUserId = broadcast.fromUserId,
                toUserId = broadcast.toUserId,
                message = broadcast.message,
                createdTime = LocalDateTime.ofInstant(broadcast.createdTime, ZoneOffset.of("+9"))
            )
        )
    }

    suspend fun getGroupChatStream(chatRoomId: Long): Flow<GroupChatMessageBroadcast> {
        return groupChatFlow.computeIfAbsent(chatRoomId) {
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 128,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
        }
    }

    suspend fun broadcastToGroup(chatRoomId: Long, groupChatMessage: GroupChatMessageBroadcast) {
        groupChatFlow[chatRoomId]?.emit(groupChatMessage)
    }

}