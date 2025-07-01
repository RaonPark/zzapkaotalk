package com.example.chatservice.service

import com.example.chatservice.dto.ChatMessageRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class RedisSubscriberService(
    private val objectMapper: ObjectMapper,
    private val messageBroadcastService: MessageBroadcastService,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @PostConstruct
    fun subscribeToChattingChannel() {
        redisMessageListenerContainer.receive(PatternTopic.of("chat:room:*"))
            .mapNotNull { message ->
                try {
                    val typeRef = object : TypeReference<ChatMessageRequest>() {}
                    objectMapper.readValue(message.message, typeRef)
                } catch (e: JsonProcessingException) {
                    log.error { "Json Processing Error: ${e.message}" }
                    null
                }
            }
            .filter { it != null }
            .subscribe { chatMessage ->
                CoroutineScope(Dispatchers.Default).launch {
                    log.info { "Subscribing to chat: $chatMessage" }
                    messageBroadcastService.broadcastToChatRoom(chatMessage!!)
                }
            }
    }
}