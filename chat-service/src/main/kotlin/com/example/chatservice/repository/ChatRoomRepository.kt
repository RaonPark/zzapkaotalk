package com.example.chatservice.repository

import com.example.chatservice.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository: JpaRepository<ChatRoom, Long> {
    fun findChatRoomById(id: Long): ChatRoom?
}