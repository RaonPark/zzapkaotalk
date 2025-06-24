package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserReactiveRepository: CoroutineCrudRepository<User, Long> {
}