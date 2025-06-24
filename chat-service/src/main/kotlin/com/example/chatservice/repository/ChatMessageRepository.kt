package com.example.chatservice.repository

import com.example.chatservice.entity.ChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Window
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository: JpaRepository<ChatMessage, Long> {
    fun findAllChatsByChatRoomId(chatRoomId: Long, pageable: Pageable, position: ScrollPosition): Window<ChatMessage>
}