package com.example.chatservice.supports

import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.UserReactiveRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component

@Component
class InitializeCacheListener(
    private val userReactiveRepository: UserReactiveRepository,
    private val userRedisOperations: ReactiveRedisOperations<String, User>
) {
    @EventListener(ApplicationReadyEvent::class)
    suspend fun loadUserCache() {
        val users = userReactiveRepository.findAll().toList()

        if(users.isEmpty()) {
            return
        }

        val userCacheMap = users.associateBy { "user:${it.id}" }

        userRedisOperations.opsForValue()
            .multiSet(userCacheMap)
            .awaitSingleOrNull()
    }
}