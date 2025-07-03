package com.example.chatservice.service

import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.dto.ChatMessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class MessageBroadcastService(
    private val kafkaTemplate: KafkaTemplate<Long, ChatMessageBroadcast>,
    private val redisMessagePublishService: RedisMessagePublishService
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    private val chatMessageSharedFlow = ConcurrentHashMap<Long, MutableSharedFlow<ChatMessageBroadcast>>()

    suspend fun broadcastToChatRoom(chatMessageResponse: ChatMessageResponse) {
        log.info { "Broadcast chat message: $chatMessageResponse" }

        val chatMessageBroadcast = convertResponseToBroadcast(chatMessageResponse)
        kafkaTemplate.executeInTransaction {
            kafkaTemplate.send("chat-message-broadcast", chatMessageBroadcast.chatRoomId, chatMessageBroadcast)
        }
    }

    fun convertResponseToBroadcast(chatMessageResponse: ChatMessageResponse): ChatMessageBroadcast {
        return ChatMessageBroadcast.newBuilder()
            .setContent(chatMessageResponse.content)
            .setChatRoomId(chatMessageResponse.chatRoomId)
            .setNickname(chatMessageResponse.nickname)
            .setUserId(chatMessageResponse.userId)
            .setCreatedTime(chatMessageResponse.createdTime)
            .setProfileImage(chatMessageResponse.profileImage)
            .build()
    }

    @KafkaListener(
        topics = ["chat-message-broadcast"],
        containerFactory = "chatMessageBroadcastKafkaListenerContainerFactory",
        concurrency = "3"
    )
    suspend fun broadcastListener(record: ConsumerRecord<Long, ChatMessageBroadcast>, ack: Acknowledgment) {
        val message = record.value()

        log.info { "Broadcast to Redis : $message" }

        redisMessagePublishService.publishChatMessageBroadcast(message)

        ack.acknowledge()
    }

    suspend fun redisBroadcast(chatMessageBroadcast: ChatMessageBroadcast) {
        if(chatMessageSharedFlow.containsKey(chatMessageBroadcast.chatRoomId)) {
            log.info { "emit to sharedFlow: $chatMessageBroadcast" }

            chatMessageSharedFlow[chatMessageBroadcast.chatRoomId]!!.emit(chatMessageBroadcast)
        }
    }

    suspend fun stream(chatRoomId: Long): Flow<ChatMessageBroadcast> = chatMessageSharedFlow[chatRoomId] ?: flow {  }

    suspend fun getLatestMessages(chatRoomId: Long): Flow<ChatMessageBroadcast> {
        if(chatMessageSharedFlow.containsKey(chatRoomId)) {
            log.info { "Getting latest messages: ${chatMessageSharedFlow[chatRoomId]}" }
            return chatMessageSharedFlow[chatRoomId]!!
        }

        return flow {  }
    }
}