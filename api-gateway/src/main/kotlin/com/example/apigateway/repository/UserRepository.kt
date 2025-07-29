package com.example.apigateway.repository

import com.example.apigateway.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository: CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User
}