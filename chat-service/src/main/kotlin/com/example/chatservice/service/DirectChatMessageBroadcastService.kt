package com.example.chatservice.service

import com.chatservice.DirectChatMessageBroadcast
import com.example.chatservice.dto.DirectChatMessageRequest
import com.example.chatservice.dto.DirectChatMessageResponse
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.redis.service.RedisService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

@Service
class DirectChatMessageBroadcastService(
    private val directChatMessageBroadcastKafkaTemplate: KafkaTemplate<Long, DirectChatMessageBroadcast>,
    private val redisService: RedisService,
    private val chatMessageBroadcaster: MessageBroadcaster
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    fun directChatMessageBroadcast(directChatMessageRequest: DirectChatMessageRequest) {
        val avro = convertDtoToAvro(directChatMessageRequest)

        directChatMessageBroadcastKafkaTemplate.executeInTransaction {
            it.send("direct-chat-broadcast", avro.toUserId, avro)
        }
    }

    private fun convertDtoToAvro(dto: DirectChatMessageRequest): DirectChatMessageBroadcast {
        return DirectChatMessageBroadcast.newBuilder()
            .setMessage(dto.message)
            .setCreatedTime(dto.timestamp.toInstant(ZoneOffset.of("+9")))
            .setToUserId(dto.toUserId)
            .setFromUserId(dto.fromUserId)
            .build()
    }

    @KafkaListener(
        topics = ["direct-chat-broadcast"],
        groupId = "direct-chat-message-broadcast",
        containerFactory = "directChatMessageBroadcastKafkaListenerContainerFactory",
        concurrency = "3"
    )
    suspend fun listensBroadcast(record: ConsumerRecord<Long, DirectChatMessageBroadcast>) {
        val message = record.value()

        log.info { "Received message $message" }

        chatMessageBroadcaster.broadcastToDirect(message)
    }
}