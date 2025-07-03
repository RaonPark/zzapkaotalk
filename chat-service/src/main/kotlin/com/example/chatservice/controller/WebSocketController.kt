package com.example.chatservice.controller

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.service.ChatService
import com.example.chatservice.service.MessageBroadcastService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller

@Controller
class WebSocketController(
    private val chatService: ChatService,
    private val messageBroadcastService: MessageBroadcastService
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    /**
     * chat flow:
     * 1. Chat Message from client delivers to channel {chat.room.chatRoomId}
     * 2. @MessageMapping is handling requests
     * 2-1. insert chat message in DB
     * 2-2. send kafka that inbound message has delivered.
     * 3. @KafkaListener publish a message using redis pub/sub channel
     * 4. redis subscriber listens message and emit to MutableSharedFlow
     * 5. RSocket Controller emits message to clients which subscribe to channel {chat.stream.chatRoomId}
     *
     * why use both of kafka and redis?
     * ->
     */

    @MessageMapping("chat.room.{chatRoomId}")
    suspend fun chatting(
        @Payload chatMessageRequest: ChatMessageRequest,
        @DestinationVariable chatRoomId: String,
    ) {
        log.info { "Received chat message: $chatMessageRequest" }

        // insert chat in DB
        val chatMessageResponse = chatService.insertChat(chatMessageRequest)

        // send to kafka that inbound message comes.
        messageBroadcastService.broadcastToChatRoom(chatMessageResponse)
    }

    @MessageMapping("chat.stream.{chatRoomId}")
    suspend fun broadcastChat(
        @DestinationVariable chatRoomId: String,
    ): Flow<ChatMessageBroadcast> {
        log.info { "Broadcast chat message: $chatRoomId" }
        return messageBroadcastService.stream(chatRoomId = chatRoomId.toLong())
            .onStart {
                emitAll(messageBroadcastService.getLatestMessages(chatRoomId = chatRoomId.toLong()))
            }
    }
}