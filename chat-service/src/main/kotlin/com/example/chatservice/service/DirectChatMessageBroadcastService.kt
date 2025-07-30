package com.example.chatservice.service

import com.chatservice.DirectChatMessageBroadcast
import com.example.chatservice.dto.DirectChatMessageRequest
import com.example.chatservice.redis.service.RedisService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class DirectChatMessageBroadcastService(
    private val directChatMessageBroadcastKafkaTemplate: KafkaTemplate<Long, DirectChatMessageBroadcast>,
    private val redisService: RedisService,
    private val chatMessageBroadcaster: MessageBroadcaster,
    private val webSocketManager: WebSocketManager
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
    suspend fun listensBroadcast(record: ConsumerRecord<Long, DirectChatMessageBroadcast>, acknowledgment: Acknowledgment) {
        val message = record.value()

        log.info { "Received message $message" }

        if(webSocketManager.userConnected(message.toUserId)) {
            chatMessageBroadcaster.broadcastToDirect(message)
        } else {
            // TODO(DLQ를 사용하여 메세지를 재전송해본다.)
            // TODO(이후에 푸시알람쪽으로 넘어간다.)
        }

        acknowledgment.acknowledge()
    }
}