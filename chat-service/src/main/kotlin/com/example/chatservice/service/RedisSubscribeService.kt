package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class RedisSubscribeService(
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
                    val typeRef = object : TypeReference<ChatMessageBroadcast>() {}
                    objectMapper.readValue(message.message, typeRef)
                } catch (e: JsonProcessingException) {
                    log.error { "Json Processing Error: ${e.message}" }
                    null
                }
            }
            .filter { it != null }
            .subscribe { chatMessageBroadcast ->
                CoroutineScope(Dispatchers.Default).launch {
                    log.info { "Subscribing to chat: $chatMessageBroadcast" }
                    messageBroadcastService.redisBroadcast(chatMessageBroadcast!!)
                }
            }
    }
}