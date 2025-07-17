package com.example.chatservice.controller

import com.chatservice.GroupChatMessageBroadcast
import com.example.chatservice.dto.DirectChatMessageRequest
import com.example.chatservice.dto.DirectChatMessageResponse
import com.example.chatservice.dto.GroupChatMessageRequest
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.service.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class WebSocketController(
    private val chatService: ChatService,
    private val groupChatMessageBroadcastService: GroupChatMessageBroadcastService,
    private val directChatMessageBroadcastService: DirectChatMessageBroadcastService,
    private val messageBroadcaster: MessageBroadcaster,
    private val webSocketManager: WebSocketManager,
    private val userRedisOperations: ReactiveRedisOperations<String, User>
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @ConnectMapping("chat.direct.{userId}")
    suspend fun connect(@DestinationVariable userId: Long) {
        val user = userRedisOperations.opsForValue()["user:$userId"].awaitSingle()

        webSocketManager.userConnection(user.id)

        log.info { "RSocket Connected user $userId" }
    }

    @ConnectMapping("chat.group.{chatroomId}")
    suspend fun groupChatConnection() {

    }

    /**
     * chat flow DM:
     * 1. Chat Message mapped rsocket url chat.direct.{userId}
     * 2. RSocket Controller send Kafka first to make sure message process exactly once and not fly away.
     * 3. Kafka Producer send a message to Consumer.
     * 4. Consumer listens and process message and broadcast to toUserId
     * 5. when toUserId subscribe to chat.direct.stream.{userId} then they can listen messages.
     */

    @MessageMapping("chat.room.{chatRoomId}")
    suspend fun chatting(
        @Payload groupChatMessageRequest: GroupChatMessageRequest,
        @DestinationVariable chatRoomId: String,
    ) {
        log.info { "Received chat message: $groupChatMessageRequest" }

        // insert chat in DB
        val chatMessageResponse = chatService.insertChat(groupChatMessageRequest)

        // send to kafka that inbound message comes.
        groupChatMessageBroadcastService.broadcastToChatRoom(chatMessageResponse)
    }

    @MessageMapping("chat.room.stream.{chatRoomId}")
    suspend fun broadcastChat(
        @DestinationVariable chatRoomId: Long,
    ): Flow<GroupChatMessageBroadcast> {
        log.info { "Broadcast chat message: $chatRoomId" }
        return messageBroadcaster.getGroupChatStream(chatRoomId)
    }

    @MessageMapping("chat.direct.{userId}")
    suspend fun chattingDirect(
        @Payload directChatMessage: DirectChatMessageRequest,
        @DestinationVariable("userId") userId: Long
    ) {
        log.info { "Received direct: $directChatMessage" }

        // insert chat in DB
        val chatMessageResponse = chatService.insertDirectChat(directChatMessage)

        log.info { "Direct Chat Saved: $chatMessageResponse" }

        directChatMessageBroadcastService.directChatMessageBroadcast(directChatMessage)
    }

    @MessageMapping("chat.direct.stream.{userId}")
    suspend fun broadcastDirectChat(@DestinationVariable("userId") userId: Long): Flow<DirectChatMessageResponse> {
        log.info { "subscribe stream : $userId" }

        return messageBroadcaster.getDirectChatMessageStream(userId)
    }
}