package com.example.chatservice.service

import com.example.chatservice.redis.entity.WebSocketSession
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketManager(
    private val websocketManagerRedisTemplate: ReactiveRedisTemplate<String, WebSocketSession>,
    private val discoveryClient: DiscoveryClient
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    private val localSessions = ConcurrentHashMap<Long, WebSocketSession>()

    private val currentInstanceId: String by lazy {
        discoveryClient.getInstances("CHAT-SERVICE")[0].instanceId
            ?: throw IllegalStateException("Current instance ID is missing")
    }

    fun isSameSession(toUserId: Long): Boolean {
        return localSessions.containsKey(toUserId)
    }

    suspend fun userConnection(userId: Long) {
        log.info { "user entered connection: $userId to $currentInstanceId" }

        localSessions[userId] = WebSocketSession(userId, currentInstanceId)

        websocketManagerRedisTemplate.opsForValue()
            .set("session:$userId", WebSocketSession(userId, currentInstanceId))
            .awaitSingle()
    }

    suspend fun disconnected(userId: Long) {
        localSessions.remove(userId)

        websocketManagerRedisTemplate.delete("session:$userId")
            .awaitSingle()
    }
}