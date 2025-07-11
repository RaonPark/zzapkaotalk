package com.example.chatservice.service

import com.example.chatservice.redis.entity.WebSocketSession
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
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

    fun isSameSession(toUserId: Long): Boolean {
        return localSessions.containsKey(toUserId)
    }

    suspend fun userConnection(userId: Long) {
        val instance = discoveryClient.getInstances("chat-service")[0]
        log.info { "user entered connection: $userId to ${instance.instanceId}" }

        localSessions[userId] = WebSocketSession(userId, instance.instanceId)

        websocketManagerRedisTemplate.opsForValue()
            .set("session:$userId", WebSocketSession(userId, instance.instanceId))
            .awaitSingle()
    }

    suspend fun disconnected(userId: Long) {
        websocketManagerRedisTemplate.delete("session:$userId")
            .awaitSingle()
    }
}