package com.example.chatservice.service.integration

import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.redis.entity.WebSocketSession
import com.example.chatservice.service.WebSocketManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "eureka.client.enabled=true",
        "eureka.client.register-with-eureka=true",
        "eureka.client.fetch-registry=true",
        "eureka.client.service-url.defaultZone=http://localhost:8070/eureka",
        "eureka.instance.lease-expiration-duration-in-seconds=2",
        "eureka.instance.lease-renewal-interval-in-seconds=1"
    ]
)
@Import(TestcontainersConfiguration::class)
@Testcontainers
@ActiveProfiles("test")
class WebSocketManagerIntegrationTest {
    @Autowired
    private lateinit var webSocketManager: WebSocketManager

    @Autowired
    private lateinit var discoveryClient: DiscoveryClient

    @Autowired
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, WebSocketSession>

    @LocalServerPort
    private var port: Int = 0

    @Test
    @DisplayName("사용자 연결 시, 실제 Eureka 서버에서 조회한 인스턴스 ID가 포함된 세션이 Redis에 저장되어야 한다")
    fun `userConnection should use real Eureka instanceId and save to Redis`() = runTest {
        val userId = 1L
        val sessionKey = "session:$userId"

        await.atMost(15, TimeUnit.SECONDS)
            .withPollInterval(Duration.ofSeconds(1))
            .until {
                discoveryClient.getInstances("CHAT-SERVICE").isNotEmpty()
            }

        webSocketManager.userConnection(userId)

        val savedSession = redisTemplate.opsForValue().get(sessionKey).awaitSingle()
        val ttl = redisTemplate.getExpire(sessionKey).awaitSingle()

        assertThat(savedSession).isNotNull
        assertThat(savedSession.userId).isEqualTo(userId)

        assertThat(savedSession.websocketServer).isNotNull.containsIgnoringCase("CHAT-SERVICE")

        assertThat(ttl).isCloseTo(Duration.ofMinutes(30), Duration.ofSeconds(2))
    }
}