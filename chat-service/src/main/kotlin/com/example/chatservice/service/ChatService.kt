package com.example.chatservice.service

import com.example.chatservice.converter.GroupChatMessageDtoConverter
import com.example.chatservice.dto.*
import com.example.chatservice.reactive.entity.DirectChatMessage
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.ChatroomUsersReactiveRepository
import com.example.chatservice.reactive.repository.GroupChatMessageReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import com.example.chatservice.redis.service.RedisService
import com.example.chatservice.supports.MachineIdGenerator
import com.example.chatservice.supports.SnowflakeIdGenerator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatMessageRepository: GroupChatMessageReactiveRepository,
    private val groupChatMessageDtoConverter: GroupChatMessageDtoConverter,
    private val chatRoomUsersRepository: ChatroomUsersReactiveRepository,
    private val userRepository: UserReactiveRepository,
    private val chatRoomRepository: ChatroomReactiveRepository,
    private val r2dbcTemplate: R2dbcEntityTemplate,
    private val redisService: RedisService
) {
    companion object {
        val log = KotlinLogging.logger { }
        val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())
    }

    suspend fun insertChat(groupChatMessageRequest: GroupChatMessageRequest): GroupChatMessageResponse {
        val chatMessage = groupChatMessageDtoConverter.convertRequestToModel(groupChatMessageRequest)
            .also { chatMessage ->
                chatMessage.id = snowflakeIdGenerator.nextId()
                chatMessage.checked =
                    chatRoomUsersRepository.countChatroomUsersByChatroomId(groupChatMessageRequest.chatRoomId)
            }

        log.info { "insert chat message: $chatMessage" }

        val savedChat = r2dbcTemplate.insert(chatMessage).awaitSingle()

        return groupChatMessageDtoConverter.convertModelToResponse(savedChat)
    }

    suspend fun selectAllChats(chatRoomId: Long, userId: Long, pageOffSet: Int): Flow<GetAllChatMessagesResponse> {
        val messages = chatMessageRepository.findAllByChatRoomIdOrderByCreatedDateDesc(
            chatRoomId = chatRoomId,
            pageable = PageRequest.of(pageOffSet, 30)
        ).toList()

        return messages.asFlow()
            .map { message ->
                groupChatMessageDtoConverter.convertModelToGetAllChatMessagesResponse(message)
            }
    }

    suspend fun insertDirectChat(directChatMessageRequest: DirectChatMessageRequest): DirectChatMessageResponse {
        val chatMessage = DirectChatMessage(
            id = snowflakeIdGenerator.nextId(),
            checked = false,
            createdAt = directChatMessageRequest.timestamp,
            message = directChatMessageRequest.message,
            fromUserId = directChatMessageRequest.fromUserId,
            toUserId = directChatMessageRequest.toUserId,
            lastModifiedAt = directChatMessageRequest.timestamp,
        )

        log.info { "insert chat message: $chatMessage" }

        val savedChat = r2dbcTemplate.insert(chatMessage).awaitSingle()

        log.info { "inserted chat message: $savedChat" }

        return DirectChatMessageResponse(
            fromUserId = savedChat.fromUserId,
            toUserId = savedChat.toUserId,
            message = savedChat.message,
            createdTime = savedChat.createdAt,
        )
    }
}