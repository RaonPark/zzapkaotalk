package com.example.chatservice.controller

import app.cash.turbine.test
import com.chatservice.ChatMessageBroadcast
import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.rsocket.server.transport=websocket"]
)
@Import(value = [TestcontainersConfiguration::class])
@Testcontainers
@ActiveProfiles("test")
class WebSocketControllerTest {

    @Autowired
    private lateinit var rsocketBuilder: RSocketRequester.Builder

    @Autowired
    private lateinit var userRepository: UserReactiveRepository

    @Autowired
    private lateinit var chatroomRepository: ChatroomReactiveRepository

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @LocalServerPort
    private var serverPort: Int = 0

    @BeforeEach
    fun setup() {

        runBlocking {
            r2dbcEntityTemplate.insert(
                Chatroom(
                    id = 1L,
                    roomName = "test Hello Room",
                    roomDescription = "test Hello Room Description",
                    roomImage = "test Hello Room Image",
                )
            ).awaitSingle()

            r2dbcEntityTemplate.insert(
                User(
                    id = 1L,
                    nickname = "nick",
                    profileImage = "test nick profile image",
                )
            ).awaitSingle()
        }

    }

    @Test
    @ExperimentalTime
    fun `send chat message`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:$serverPort/rsocket"))

            val result = rSocketRequester
                .route("chat.stream.1")
                .retrieveFlow<ChatMessageBroadcast>()
                .test {
                    rSocketRequester
                        .route("chat.room.1")
                        .data(
                            ChatMessageRequest(
                                fromUserId = 1L,
                                chatRoomId = 1L,
                                content = "Hello World!"
                            )
                        )
                        .send()
                        .awaitSingle()

                    val receivedMessages = awaitItem()

                    assertEquals(1L, receivedMessages.chatRoomId)
                    assertEquals(1L, receivedMessages.userId)
                    assertEquals("Hello World!", receivedMessages.content)

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }
}