package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.Chatroom
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatroomReactiveRepository: CoroutineCrudRepository<Chatroom, Long> {
    suspend fun findChatroomById(id: Long): Chatroom?
}