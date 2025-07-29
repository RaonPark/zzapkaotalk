package com.example.userservice.repository

import com.example.userservice.entity.VerificationCode
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface VerificationCodeRepository: CoroutineSortingRepository<VerificationCode, Long> {
    suspend fun findByEmail(email: String): VerificationCode
}