package com.example.chatservice.config

import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.service.ChatService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Configuration
class ChatWebSocketHandler(
    private val chatService: ChatService
): WebSocketHandler {
    companion object {
        val log = KotlinLogging.logger { }
        val objectMapper = ObjectMapper()
    }

    /**
     * inbound 와 outbound message를 처리한다.
     */
    override fun handle(session: WebSocketSession): Mono<Void> {
        val input = session.receive()
            .doOnSubscribe {
                log.info { "Websocket connection started: ${session.id}" }
            }
            .concatMap {
                mono {
                    val message = it.payloadAsText
                    val chatMessageRequest = objectMapper.readValue(message, ChatMessageRequest::class.java)

                    log.info { "insert chat message: $chatMessageRequest" }

                    chatService.saveChatMessage(chatMessageRequest)
                    chatMessageRequest
                }
            }
            .then()

        return Mono.empty()
    }
}