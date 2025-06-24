package com.example.chatservice.converter

import com.example.chatservice.repository.ChatRoomRepository
import com.example.chatservice.repository.UserRepository
import com.example.chatservice.entity.ChatRoom
import com.example.chatservice.entity.Users
import com.example.chatservice.exception.ChatRoomNotFoundException
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.repository.ChatMessageRepository
import org.springframework.stereotype.Component

@Component
class IdMapper (
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository
) {
    fun getChatRoom(chatRoomId: Long): ChatRoom {
        return chatRoomRepository.findChatRoomById(chatRoomId)
            ?: throw ChatRoomNotFoundException(chatRoomId)
    }

    fun getUser(userId: Long): Users {
        return userRepository.findUserById(userId)
            ?: throw UserNotFoundException(userId)
    }
}