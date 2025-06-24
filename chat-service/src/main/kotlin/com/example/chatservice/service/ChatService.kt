package com.example.chatservice.service

import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.repository.ChatRoomRepository
import com.example.chatservice.repository.UserRepository
import com.example.chatservice.repository.ChatMessageRepository
import com.example.chatservice.repository.ChatRoomUsersRepository
import com.example.chatservice.supports.MachineIdGenerator
import com.example.chatservice.supports.SnowflakeIdGenerator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatMessageDtoConverter: ChatMessageDtoConverter,
    private val chatRoomUsersRepository: ChatRoomUsersRepository,
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository
) {
    companion object {
        val log = KotlinLogging.logger { }
        val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())
    }

    fun insertChat(chatMessageRequest: ChatMessageRequest): Int {
        val chatMessage = chatMessageDtoConverter.convertRequestToModel(chatMessageRequest)
            .also { chatMessage -> chatMessage.id = snowflakeIdGenerator.nextId() }

        log.info { "insert chat message: $chatMessage" }

        chatMessage.checked = chatRoomUsersRepository.countChatRoomUsersByChatRoomId(chatMessageRequest.chatRoomId)

        chatMessageRepository.save(chatMessage)

        return chatMessage.checked
    }

    suspend fun saveChatMessage(chatMessageRequest: ChatMessageRequest) = withContext(Dispatchers.IO) {
        val chatMessage = chatMessageDtoConverter.convertRequestToModel(chatMessageRequest)
            .also { chatMessage -> chatMessage.id = snowflakeIdGenerator.nextId() }

        log.info { "insert chat message: $chatMessage" }

        withContext(Dispatchers.IO) {
            chatMessage.checked = chatRoomUsersRepository.countChatRoomUsersByChatRoomId(chatMessageRequest.chatRoomId)

            chatMessageRepository.save(chatMessage)
        }
    }

//    fun selectAllChats(getAllChatMessagesRequest: GetAllChatMessagesRequest): List<ChatMessage> {
//
//    }
}