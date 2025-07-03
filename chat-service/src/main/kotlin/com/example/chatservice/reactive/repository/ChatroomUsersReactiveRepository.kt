package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatroomUsers
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatroomUsersReactiveRepository: CoroutineCrudRepository<ChatroomUsers, Long> {
    suspend fun countChatroomUsersByChatroomId(chatRoomId: Long): Int
}