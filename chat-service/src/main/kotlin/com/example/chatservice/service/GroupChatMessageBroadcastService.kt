package com.example.chatservice.service

import com.chatservice.GroupChatMessageBroadcast
import com.example.chatservice.dto.GroupChatMessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

@Service
class GroupChatMessageBroadcastService(
    private val kafkaTemplate: KafkaTemplate<Long, GroupChatMessageBroadcast>,
    private val messageBroadcaster: MessageBroadcaster
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    suspend fun broadcastToChatRoom(groupChatMessageResponse: GroupChatMessageResponse) {
        log.info { "Broadcast chat message: $groupChatMessageResponse" }

        val chatMessageBroadcast = convertResponseToBroadcast(groupChatMessageResponse)
        kafkaTemplate.executeInTransaction {
            kafkaTemplate.send("chat-message-broadcast", chatMessageBroadcast.chatRoomId, chatMessageBroadcast)
        }
    }

    fun convertResponseToBroadcast(groupChatMessageResponse: GroupChatMessageResponse): GroupChatMessageBroadcast {
        return GroupChatMessageBroadcast.newBuilder()
            .setContent(groupChatMessageResponse.content)
            .setChatRoomId(groupChatMessageResponse.chatRoomId)
            .setUserId(groupChatMessageResponse.userId)
            .setCreatedTime(groupChatMessageResponse.createdTime.toInstant(ZoneOffset.of("+9")))
            .build()
    }

    @KafkaListener(
        topics = ["group-chat-message-broadcast"],
        containerFactory = "groupChatMessageBroadcastKafkaListenerContainerFactory",
        concurrency = "3"
    )
    suspend fun groupChatBroadcastListener(record: ConsumerRecord<Long, GroupChatMessageBroadcast>, ack: Acknowledgment) {
        val message = record.value()

        log.info { "Broadcast to Redis : $message" }

        messageBroadcaster.broadcastToGroup(message.chatRoomId, message)

        ack.acknowledge()
    }

}