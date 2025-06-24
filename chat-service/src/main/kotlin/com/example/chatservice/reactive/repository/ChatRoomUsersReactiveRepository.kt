package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatRoomUsers
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ChatRoomUsersReactiveRepository: ReactiveCrudRepository<ChatRoomUsers, Long> {
}