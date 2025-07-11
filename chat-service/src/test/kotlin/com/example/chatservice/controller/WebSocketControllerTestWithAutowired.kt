package com.example.chatservice.controller

import app.cash.turbine.test
import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.dto.DirectChatMessageRequest
import com.example.chatservice.reactive.entity.DirectChatMessage
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.UserReactiveRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@Testcontainers
class WebSocketControllerTestWithAutowired {
    @Autowired
    private lateinit var webSocketController: WebSocketController

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @Test
    fun `test controller`() {
        runTest {
            r2dbcEntityTemplate.insert(
                User(
                    id = 1L,
                    nickname = "Nick",
                    profileImage = "Nick Avatar",
                    createdDate = LocalDateTime.now(),
                    modifiedDate = LocalDateTime.now(),
                )
            ).awaitSingle()

            r2dbcEntityTemplate.insert(
                User(
                    id = 2L,
                    nickname = "Lick",
                    profileImage = "Lick Avatar",
                    createdDate = LocalDateTime.now(),
                    modifiedDate = LocalDateTime.now(),
                )
            ).awaitSingle()

            webSocketController.chattingDirect(
                DirectChatMessageRequest(
                    fromUserId = 1L,
                    toUserId = 2L,
                    message = "Hello Nick!",
                    timestamp = LocalDateTime.now()
                ), 1L
            )

            val response = webSocketController.broadcastDirectChat(2L)
                .test {
                    val item = awaitItem()
                    println(item)
                }

            delay(3000)
        }
    }
}