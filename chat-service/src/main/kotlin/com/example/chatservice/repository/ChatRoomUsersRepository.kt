package com.example.chatservice.repository

import com.example.chatservice.entity.ChatRoomUsers
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomUsersRepository: JpaRepository<ChatRoomUsers, Long> {
    fun findByChatRoomId(chatRoomId: Long): ChatRoomUsers?
    fun countChatRoomUsersByChatRoomId(chatRoomId: Long): Int
}