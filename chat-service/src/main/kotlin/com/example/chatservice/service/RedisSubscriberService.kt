package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageRequest
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RedisSubscriberService(
    private val connectionFactory: ReactiveRedisConnectionFactory,
    private val objectMapper: ObjectMapper,
    private val messageBroadcastService: MessageBroadcastService,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

//    @PostConstruct
//    fun subscribeToChatRoom() {
//        redisMessageListenerContainer.receive()
//    }
}