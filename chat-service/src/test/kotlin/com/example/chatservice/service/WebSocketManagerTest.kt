package com.example.chatservice.service

import com.example.chatservice.redis.entity.WebSocketSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.cloud.client.ServiceInstance
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class WebSocketManagerTest {
    @InjectMockKs
    private lateinit var webSocketManager: WebSocketManager

    @MockK
    private lateinit var mockEurekaClient: org.springframework.cloud.client.discovery.DiscoveryClient

    @MockK
    private lateinit var mockRedisTemplate: ReactiveRedisTemplate<String, WebSocketSession>

    @MockK
    private lateinit var redisValueOperations: ReactiveValueOperations<String, WebSocketSession>

    @BeforeEach
    fun setup() {
        val mockInstance = mockk<ServiceInstance>()
        every { mockInstance.instanceId } returns "test-instance-1"
        every { mockEurekaClient.getInstances("chat-service") } returns mutableListOf(mockInstance)

        every { mockRedisTemplate.opsForValue() } returns redisValueOperations
    }

    @Test
    fun `should cache client's instance id`() = runTest {
        val user1 = 1L
        val sessionKey = "session:1"
        val expectedSession = WebSocketSession(1L, websocketServer = "test-instance-1")

        coEvery {
            redisValueOperations.set(any(), any())
        } returns Mono.just(true)

        webSocketManager.userConnection(1L)

        verify { redisValueOperations.set(eq(sessionKey), eq(expectedSession)) }
    }

    @Test
    fun `should delete session when user is disconnected`() = runTest {
        val user1 = 1L
        val sessionKey = "session:$user1"
        val expectedSession = WebSocketSession(1L, websocketServer = "test-instance-1")

        coEvery {
            redisValueOperations.set(any(), any())
        } returns Mono.just(true)

        coEvery {
            mockRedisTemplate.delete(sessionKey)
        } returns Mono.just(user1)

        webSocketManager.userConnection(user1)

        verify { redisValueOperations.set(eq(sessionKey), eq(expectedSession)) }

        webSocketManager.disconnected(user1)

        assertEquals(false, webSocketManager.isSameSession(user1))

        verify { mockRedisTemplate.delete(eq(sessionKey)) }
    }
}