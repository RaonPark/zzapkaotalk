package com.example.chatservice.service

import com.chatservice.DirectChatMessageBroadcast
import com.chatservice.GroupChatMessageBroadcast
import com.example.chatservice.dto.DirectChatMessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

@Service
class MessageBroadcaster {
    companion object {
        val log = KotlinLogging.logger { }
        val directChatFlow = ConcurrentHashMap<Long, MutableSharedFlow<DirectChatMessageResponse>>()
        val groupChatFlow = ConcurrentHashMap<Long, MutableSharedFlow<GroupChatMessageBroadcast>>()
    }

    fun getDirectChatMessageStream(toUserId: Long): Flow<DirectChatMessageResponse> {
        return directChatFlow.computeIfAbsent(toUserId) {
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 128,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )
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