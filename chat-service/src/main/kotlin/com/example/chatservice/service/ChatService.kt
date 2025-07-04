package com.example.chatservice.service

import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.dto.ChatMessageResponse
import com.example.chatservice.dto.GetAllChatMessagesResponse
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatMessageReactiveRepository
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.ChatroomUsersReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import com.example.chatservice.supports.MachineIdGenerator
import com.example.chatservice.supports.SnowflakeIdGenerator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageReactiveRepository,
    private val chatMessageDtoConverter: ChatMessageDtoConverter,
    private val chatRoomUsersRepository: ChatroomUsersReactiveRepository,
    private val userRepository: UserReactiveRepository,
    private val chatRoomRepository: ChatroomReactiveRepository,
    private val r2dbcTemplate: R2dbcEntityTemplate,
    private val userRedisOperations: ReactiveRedisOperations<String, User>
) {
    companion object {
        val log = KotlinLogging.logger { }
        val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())
    }

    suspend fun insertChat(chatMessageRequest: ChatMessageRequest): ChatMessageResponse {
        val chatMessage = chatMessageDtoConverter.convertRequestToModel(chatMessageRequest)
            .also { chatMessage ->
                chatMessage.id = snowflakeIdGenerator.nextId()
                chatMessage.checked = chatRoomUsersRepository.countChatroomUsersByChatroomId(chatMessageRequest.chatRoomId)
            }

        log.info { "insert chat message: $chatMessage" }

        val savedChat = r2dbcTemplate.insert(chatMessage).awaitSingle()

        return chatMessageDtoConverter.convertModelToResponse(savedChat)
            .also { chat ->
                val user = getUserFromCacheIfMissedFromDB(chat.userId)
                chat.nickname = user.nickname
                chat.profileImage = user.profileImage
            }
    }

    private suspend fun getUserFromCacheIfMissedFromDB(userId: Long): User {
        val userIdKey = userId.toString()
        var userFromCache = userRedisOperations.opsForValue().get(userIdKey).awaitSingleOrNull()

        if(userFromCache == null) {
            userFromCache = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
            userRedisOperations.opsForValue().set("$userId", userFromCache).awaitSingle()
        }

        return userFromCache
    }

    suspend fun selectAllChats(chatRoomId: Long, userId: Long, pageOffSet: Int): Flow<GetAllChatMessagesResponse> {
        val messages = chatMessageRepository.findAllByChatRoomIdOrderByCreatedDateDesc(
            chatRoomId = chatRoomId,
            pageable = PageRequest.of(pageOffSet, 30)
        ).toList()

        val senderIds = messages.map { it.fromUserId }.distinct()

        val sendersMap = getUsersFromCacheAndDBByIds(senderIds)

        return messages.asFlow()
            .map { message ->
                val sender = sendersMap[message.fromUserId]

                chatMessageDtoConverter.convertModelToGetAllChatMessagesResponse(message)
                    .also { dto ->
                        dto.nickname = sender?.nickname ?: "(알 수 없음)"
                        dto.profileImage = sender?.profileImage ?: "Default_IMG"
                    }
            }
    }

    private suspend fun getUsersFromCacheAndDBByIds(ids: List<Long>): Map<Long, User> {
        val cacheKeys = ids.map { "$it" }
        val cachedUsers = userRedisOperations.opsForValue().multiGet(cacheKeys)
            .awaitSingleOrNull() ?: emptyList()

        val hitUsersMap = cachedUsers.filterNotNull().associateBy { it.id }
        val missedIds = cacheKeys.filter { id -> !hitUsersMap.containsKey(id.toLong()) }.map { it.toLong() }

        val dbUsersMap = if(missedIds.isNotEmpty()) {
            val usersFromDB = userRepository.findAllById(missedIds).toList()

            if(usersFromDB.isNotEmpty()) {
                val newCacheData = usersFromDB.associateBy { "${it.id}" }
                userRedisOperations.opsForValue().multiSet(newCacheData).awaitSingle()
            }

            usersFromDB.associateBy { it.id }
        } else {
            emptyMap()
        }

        return hitUsersMap + dbUsersMap
    }
}