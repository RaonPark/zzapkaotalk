package com.example.chatservice.redis.service

import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.UserReactiveRepository
import com.example.chatservice.service.ChatService.Companion.log
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val userRedisOperations: ReactiveRedisOperations<String, User>,
    private val userRepository: UserReactiveRepository
) {
    suspend fun getUserFromCacheIfMissFromDB(userId: Long): User {
        return userRedisOperations.opsForValue().get("user:$userId").awaitSingleOrNull()
            ?: userRepository.findById(userId) ?: throw UserNotFoundException(userId)
    }

    suspend fun getUsersFromCacheIfMissFromDB(userIds: List<Long>): Map<Long, User> {
        val cacheKeys = userIds.map { "user:$it" }
        val cachedUsers = userRedisOperations.opsForValue().multiGet(cacheKeys)
            .awaitSingleOrNull() ?: emptyList()

        val hitUsersMap = cachedUsers.filterNotNull().associateBy { it.id }
        val missedIds = userIds.filter { it !in hitUsersMap.keys }

        val dbUsersMap = if(missedIds.isNotEmpty()) {
            val usersFromDB = userRepository.findAllById(missedIds).toList()

            if(usersFromDB.isNotEmpty()) {
                val newCacheData = usersFromDB.associateBy { "user:${it.id}" }
                val cacheSavedResult = userRedisOperations.opsForValue().multiSet(newCacheData).awaitSingle()

                if(!cacheSavedResult) {
                    log.warn { "Failed to cache users : ${newCacheData.keys}" }
                    throw RuntimeException("Cache Save Error!")
                }
            }

            usersFromDB.associateBy { it.id }
        } else {
            emptyMap()
        }

        return hitUsersMap + dbUsersMap
    }
}