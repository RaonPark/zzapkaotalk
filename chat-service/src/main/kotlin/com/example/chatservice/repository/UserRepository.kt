package com.example.chatservice.repository

import com.example.chatservice.entity.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<Users, Long> {
    fun findUserById(id: Long): Users?
}