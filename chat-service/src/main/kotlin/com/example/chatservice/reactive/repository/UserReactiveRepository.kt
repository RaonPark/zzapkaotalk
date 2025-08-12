package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserReactiveRepository: CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(userEmail: String): User
}